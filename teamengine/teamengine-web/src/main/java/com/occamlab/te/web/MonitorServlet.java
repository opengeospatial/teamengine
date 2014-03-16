package com.occamlab.te.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.XdmNode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.occamlab.te.TECore;
import com.occamlab.te.util.DomUtils;

/**
 * A servlet that intercepts a client request and validates it with a registered
 * MonitorCall object. Each MonitorCall object is associated with a service
 * endpoint.
 * 
 */
@SuppressWarnings("serial")
public class MonitorServlet extends HttpServlet {
    private static final Logger LOGR = Logger.getLogger(MonitorServlet.class
            .getPackage().getName());
    public static final String CTL_NS = "http://www.occamlab.com/ctl";

    static DocumentBuilder DB;
    static Transformer identityTransformer;

    static String baseServletURL;
    static String servletName;
    static int monitorCallSeq = 0;
    static int monitorUrlSeq = 0;
    static Map<String, MonitorCall> monitors = new HashMap<String, MonitorCall>();

    static public String allocateMonitorUrl(String url) {
        String monitorUrl = baseServletURL + "/" + servletName + "/"
                + Integer.toString(monitorUrlSeq);
        monitorUrlSeq++;
        MonitorCall mc = new MonitorCall(url);
        monitors.put(monitorUrl, mc);
        return monitorUrl;
    }

    // Monitor without parser that doesn't trigger a test
    static public String createMonitor(String monitorUrl, TECore core) {
        return createMonitor(monitorUrl, null, "", core);
    }

    // Monitor that doesn't trigger a test
    static public String createMonitor(String monitorUrl,
            Node parserInstruction, String modifiesResponse, TECore core) {
        MonitorCall mc = monitors.get(monitorUrl);
        mc.setCore(core);
        if (parserInstruction != null) {
            mc.setParserInstruction(DomUtils.getElement(parserInstruction));
            mc.setModifiesResponse(Boolean.parseBoolean(modifiesResponse));
        }
        LOGR.log(Level.CONFIG, "Configured monitor without test:\n {0}", mc);
        return "";
    }

    // Monitor without parser that triggers a test
    static public String createMonitor(XPathContext context, String url,
            String localName, String namespaceURI, NodeInfo params,
            String callId, TECore core) throws Exception {
        return createMonitor(context, url, localName, namespaceURI, params,
                null, "", callId, core);
    }

    // Monitor that triggers a test
    static public String createMonitor(XPathContext context, String monitorUrl,
            String localName, String namespaceURI, NodeInfo params,
            NodeInfo parserInstruction, String modifiesResponse, String callId,
            TECore core) throws Exception {
        MonitorCall mc = monitors.get(monitorUrl);
        mc.setContext(context);
        mc.setLocalName(localName);
        mc.setNamespaceURI(namespaceURI);
        mc.setCore(core);
        if (params != null) {
            Node node = (Node) NodeOverNodeInfo.wrap(params);
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                mc.setParams(((Document) node).getDocumentElement());
            } else {
                mc.setParams((Element) node);
            }
        }
        if (parserInstruction != null) {
            Node node = (Node) NodeOverNodeInfo.wrap(parserInstruction);
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                mc.setParserInstruction(((Document) node).getDocumentElement());
            } else {
                mc.setParserInstruction((Element) node);
            }
            mc.setModifiesResponse(Boolean.parseBoolean(modifiesResponse));
        }
        mc.setCallId(callId);
        LOGR.log(Level.CONFIG, "Configured monitor with test:\n {0}", mc);
        return "";
    }

    public static String destroyMonitors(TECore core) {
        ArrayList<String> keysToDelete = new ArrayList<String>();
        for (Entry<String, MonitorCall> entry : monitors.entrySet()) {
            MonitorCall mc = entry.getValue();
            if (mc.getCore() == core) {
                if (mc.getTestPath().equals(core.getTestPath())) {
                    keysToDelete.add(entry.getKey());
                    mc.destroy();
                }
            }
        }
        for (String key : keysToDelete) {
            monitors.remove(key);
        }
        return "";
    }

    public void process(HttpServletRequest request,
            HttpServletResponse response, boolean post) throws ServletException {
        try {
            String uri = request.getRequestURL().toString();
            MonitorCall mc = monitors.get(uri);
            if (mc == null) {
                response.sendError(410, "This URL is no longer valid");
                return;
            }
            if (null == request.getContentType()) {
                // check GET requests only
                String query = null;
                query = URLDecoder.decode(request.getQueryString(), "UTF-8");
                mc.checkCoverage(query);
            }
            TECore core = mc.getCore();
            String url = mc.getUrl();
            String queryString = request.getQueryString();
            if (queryString != null) {
                if (url.contains("?")) {
                    url += queryString;
                } else {
                    url += "?" + queryString;
                }
            }

            HttpURLConnection huc = (HttpURLConnection) (new URL(url)
                    .openConnection());
            CachedHttpURLConnection uc = new CachedHttpURLConnection(huc);

            String method = request.getMethod();
            uc.setRequestMethod(method);
            uc.setDoInput(true);
            uc.setDoOutput(post);
            byte[] data = null;
            if (post) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copy_stream(request.getInputStream(), baos);
                data = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                copy_stream(bais, uc.getOutputStream());
            }

            Document doc = DB.newDocument();
            Element eRequest = encodeRequest(request, doc, data);
            Element parserInstruction = mc.getParserInstruction();
            Element eResponse = core.parse(uc, parserInstruction);

            Map<String, List<String>> responseHeaders = uc.getHeaderFields();
            for (Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                String key = entry.getKey();
                // System.out.println(key + ": " + entry.getValue());
                if (key != null) {
                    if (key.length() == 0) {
                        // do nothing
                    } else if (key.equalsIgnoreCase("Transfer-Encoding")) {
                        // do nothing
                    } else {
                        for (String value : entry.getValue()) {
                            response.setHeader(key, value);
                        }
                    }
                }
            }

            if (mc.getModifiesResponse()) {
                LOGR.log(Level.FINE, DomUtils.serializeNode(eResponse));
                Element content = DomUtils.getElementByTagName(eResponse,
                        "content");
                Element root = DomUtils.getChildElement(content);
                identityTransformer.transform(new DOMSource(root),
                        new StreamResult(response.getOutputStream()));
            } else {
                response.setContentLength(uc.getLength());
                copy_stream(uc.getInputStream(), response.getOutputStream());
            }

            if (mc.getCallId() != null) {
                identityTransformer.transform(new DOMSource(mc.getParams()),
                        new DOMResult(doc));
                Element eParams = DomUtils.getElementByTagName(doc, "params");
                Element eReqParam = doc.createElement("param");
                eReqParam.setAttribute("local-name", "request");
                eReqParam.setAttribute("namespace-uri", "");
                eReqParam.setAttribute("prefix", "");
                eReqParam.setAttribute("type", "node()");
                Element eReqValue = doc.createElement("value");
                eReqValue.appendChild(eRequest);
                eReqParam.appendChild(eReqValue);
                eParams.appendChild(eReqParam);
                Element eRespParam = doc.createElement("param");
                eRespParam.setAttribute("local-name", "response");
                eRespParam.setAttribute("namespace-uri", "");
                eRespParam.setAttribute("prefix", "");
                eRespParam.setAttribute("type", "node()");
                Element eRespValue = doc.createElement("value");
                identityTransformer.transform(new DOMSource(eResponse),
                        new DOMResult(eRespValue));
                eRespParam.appendChild(eRespValue);
                eParams.appendChild(eRespParam);
                net.sf.saxon.s9api.DocumentBuilder builder = core.getEngine()
                        .getBuilder();
                XdmNode paramsNode = builder.build(new DOMSource(doc));
                monitorCallSeq++;
                String callId = mc.getCallId() + "_"
                        + Integer.toString(monitorCallSeq);
                core.callTest(mc.getContext(), mc.getLocalName(),
                        mc.getNamespaceURI(), paramsNode.getUnderlyingNode(),
                        callId);
            }
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }

    public void init() throws ServletException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DB = dbf.newDocumentBuilder();
            identityTransformer = TransformerFactory.newInstance()
                    .newTransformer();
            servletName = this.getServletName();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        process(request, response, false);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        process(request, response, true);
    }

    @SuppressWarnings("unchecked")
    Element encodeRequest(HttpServletRequest request, Document doc, byte[] data)
            throws Exception {
        Element eRequest = doc.createElementNS(CTL_NS, "ctl:request");
        Element eURL = doc.createElementNS(CTL_NS, "ctl:url");
        eURL.setTextContent(request.getRequestURL().toString());
        eRequest.appendChild(eURL);
        Element eMethod = doc.createElementNS(CTL_NS, "ctl:method");
        eMethod.setTextContent(request.getMethod());
        eRequest.appendChild(eMethod);
        Enumeration<String> requestHeaders = request.getHeaderNames();
        while (requestHeaders.hasMoreElements()) {
            String key = (String) requestHeaders.nextElement();
            Element eHeader = doc.createElementNS(CTL_NS, "ctl:header");
            eHeader.setAttribute("name", key);
            eHeader.setTextContent(request.getHeader(key));
            eRequest.appendChild(eHeader);
        }
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String key = (String) params.nextElement();
            Element eParam = doc.createElementNS(CTL_NS, "ctl:param");
            eParam.setAttribute("name", key);
            eParam.setTextContent(request.getParameter(key));
            eRequest.appendChild(eParam);
        }
        if (data != null) {
            String mime = request.getContentType();
            if (mime.indexOf("text/xml") == 0
                    || mime.indexOf("application/xml") == 0) {
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                Element eBody = doc.createElementNS(CTL_NS, "ctl:body");
                Transformer t = TransformerFactory.newInstance()
                        .newTransformer();
                t.transform(new StreamSource(bais), new DOMResult(eBody));
                eRequest.appendChild(eBody);
            } else if (mime.indexOf("text/") == 0) {
                Element eBody = doc.createElementNS(CTL_NS, "ctl:body");
                eBody.appendChild(doc.createCDATASection(data.toString()));
                eRequest.appendChild(eBody);
            }
        }
        return eRequest;
    }

    static void copy_stream(InputStream in, OutputStream out)
            throws IOException {
        int i = in.read();
        while (i >= 0) {
            out.write(i);
            i = in.read();
        }
    }

    public static String getBaseServletURL() {
        return baseServletURL;
    }

    public static void setBaseServletURL(String baseServletURL) {
        MonitorServlet.baseServletURL = baseServletURL;
    }
}
