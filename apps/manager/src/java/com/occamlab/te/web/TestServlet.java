/****************************************************************************

 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): Paul Daisey (Image Matters LLC) : 
 					enable ViewSessionLog.jsp to find listing files
					add cache mode
					enable execution of profiles in retest and cache modes
					sort profiles with TreeSet()s so they are executed in order
 ****************************************************************************/
package com.occamlab.te.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.SetupOptions;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.Test;
import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.StringUtils;

/**
 * Main request handler.
 * 
 */
public class TestServlet extends HttpServlet {
    public static final String CTL_NS = "http://www.occamlab.com/ctl";

    DocumentBuilder DB;
    Transformer identityTransformer;
    Engine engine;
    Map<String, Index> indexes;
    Config conf;
/*    
    static String testServletURL;
    static int monitorCallSeq = 0;
    static int monitorUrlSeq = 0;
    static Map<String, MonitorCall> monitors = new HashMap<String, MonitorCall>();

    static public String allocateMonitorUrl(String url) {
        String monitorUrl = testServletURL + "/monitor/" + Integer.toString(monitorUrlSeq);
        monitorUrlSeq++;
        MonitorCall mc = new MonitorCall(url);
        monitors.put(monitorUrl, mc);
        return monitorUrl;
    }

    // Monitor without parser that doesn't trigger a test
    static public String createMonitor(TECore core, String monitorUrl) {
        return createMonitor(core, monitorUrl, null, "");
    }

    // Monitor that doesn't trigger a test
    static public String createMonitor(TECore core, String monitorUrl, Node parserInstruction, String passThrough) {
        MonitorCall mc = monitors.get(monitorUrl);
    	mc.setCore(core);
        if (parserInstruction != null) {
        	mc.setParserInstruction(DomUtils.getElement(parserInstruction));
        	mc.setPassThrough(Boolean.parseBoolean(passThrough));
        }
        return "";
    }

    // Monitor without parser that triggers a test
    static public String createMonitor(TECore core, XPathContext context, String url, String localName, String namespaceURI, NodeInfo params, String callId) throws Exception {
        return createMonitor(core, context, url, localName, namespaceURI, params, null, "", callId);
    }

    // Monitor that triggers a test
    static public String createMonitor(TECore core, XPathContext context, String monitorUrl, String localName, String namespaceURI, NodeInfo params, NodeInfo parserInstruction, String passThrough, String callId) throws Exception {
        MonitorCall mc = monitors.get(monitorUrl);
    	mc.setCore(core);
        mc.setContext(context);
        mc.setLocalName(localName);
        mc.setNamespaceURI(namespaceURI);
        if (params != null) {
            Node node = (Node)NodeOverNodeInfo.wrap(params);
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                mc.setParams(((Document)node).getDocumentElement());
            } else {
                mc.setParams((Element)node);
            }
        }
        if (parserInstruction != null) {
            Node node = (Node)NodeOverNodeInfo.wrap(parserInstruction);
            if (node.getNodeType() == Node.DOCUMENT_NODE) {
                mc.setParserInstruction(((Document)node).getDocumentElement());
            } else {
                mc.setParserInstruction((Element)node);
            }
            mc.setPassThrough(Boolean.parseBoolean(passThrough));
        }
        mc.setCallId(callId);
        return "";
    }


//    File getDir(String dirname) throws ServletException {
//        File dir = new File(getInitParameter(dirname + "Dir"));
//        if (!dir.isDirectory()) {
//            dir = new File("WEB-INF", dirname);
//        }
//        if (!dir.isDirectory()) {
//            throw new ServletException("Can't find " + dirname);
//        }
//        return dir;
//    }
*/
    /**
     * Generates executable test suites from available CTL sources.
     */
    public void init() throws ServletException {
        try {
            conf = new Config();

            DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            identityTransformer = TransformerFactory.newInstance().newTransformer();

//            File logsDir = new File(System.getProperty("catalina.base"), "logs");

            indexes = new HashMap<String, Index>();
            
            HashMap<String, TEClassLoader> classLoaders = new HashMap<String, TEClassLoader>();
            
            Processor processor = new Processor(false);
            processor.setConfigurationProperty(FeatureKeys.XINCLUDE, Boolean.TRUE);
            XsltCompiler sourceGeneratorCompiler = processor.newXsltCompiler();
            File sourceGeneratorStylesheet = Misc.getResourceAsFile("com/occamlab/te/generate_source_html.xsl");
            XsltExecutable sourceGeneratorXsltExecutable = sourceGeneratorCompiler.compile(new StreamSource(sourceGeneratorStylesheet));
            XsltTransformer sourceGeneratorTransformer = sourceGeneratorXsltExecutable.load();

            File listings = new File(getServletConfig().getServletContext().getRealPath("/"), "listings");
            listings.mkdir();

            for (Entry<String, List<File>> sourceEntry : conf.getSources().entrySet()) {
                String sourcesName = sourceEntry.getKey();
//              System.out.println("TestServlet: " + sourcesName);
                SetupOptions setupOpts = new SetupOptions();
                setupOpts.setWorkDir(conf.getWorkDir());
                setupOpts.setSourcesName(sourcesName);
                for (File source : sourceEntry.getValue()) {
                    setupOpts.addSource(source);
                }
                Index index = Generator.generateXsl(setupOpts);
                indexes.put(sourcesName, index);
                
                for (File ctlFile: index.getDependencies()) {
                    String encodedName = URLEncoder.encode(ctlFile.getAbsolutePath(), "UTF-8");
                    // encodedName = encodedName.replace('%', '~');  // In Java 5, the Document.parse function has trouble with the URL % encoding
                    // begin 2011-04-06 PwD  replace the following line because ViewSessionLog.jsp cannot find listing files
                    // String basename = encodedName;
                    String basename = encodedName.replace('%', '~');
                    // end 2011-04-06 PwD
                    int i = basename.lastIndexOf('.');
                    if (i > 0) {
                        basename = basename.substring(0, i);
                    }
                    File indexFile = new File(new File(conf.getWorkDir(), encodedName), "index.xml");
                    File htmlFile = new File(listings, basename + ".html");
                    boolean needsGenerating = true;
                    if (htmlFile.exists()) {
                        needsGenerating = (indexFile.lastModified() > htmlFile.lastModified());
                    }
                    if (needsGenerating) {
                        sourceGeneratorTransformer.setSource(new StreamSource(ctlFile));
                        Serializer sourceGeneratorSerializer = new Serializer();
                        sourceGeneratorSerializer.setOutputFile(htmlFile);
                        sourceGeneratorTransformer.setDestination(sourceGeneratorSerializer);
                        sourceGeneratorTransformer.transform();
                    }
                }
                
                classLoaders.put(sourcesName, new TEClassLoader(conf.getResources().get(sourcesName)));
            }
/*
            File scriptsDir = conf.getScriptsDir();
            String sourcesNames[] = scriptsDir.list();
            for (int i = 0; i < sourcesNames.length; i++) {
                String sourcesName = sourcesNames[i];
//System.out.println("TestServlet: " + sourcesName);
                SetupOptions setupOpts = new SetupOptions();
                setupOpts.setWorkDir(conf.getWorkDir());
                setupOpts.setSourcesName(sourcesName);
                File sourcesDir = new File(scriptsDir, sourcesName);
                File configFile = new File(sourcesDir, "config.xml");
                if (configFile.canRead()) {
                    // TODO: process config file
                    // For now, presence of config.xml means load ctl/main.xml or ctl/main.ctl
                    File ctlDir = new File(sourcesDir, "ctl");
                    File source = new File(ctlDir, "main.xml");
                    if (source.canRead()) {
                        setupOpts.addSource(source);
                    } else {
                        setupOpts.addSource(new File(ctlDir, "main.ctl"));
                    }
                } else {
                    setupOpts.addSource(new File(sourcesDir, "ctl"));
                }
                Index index = Generator.generateXsl(setupOpts);
                indexes.put(sourcesName, index);
            }
*/
            int cacheSize = 0;
            String s = getServletConfig().getInitParameter("cacheSize");
            if (s != null) {
                cacheSize = Integer.parseInt(s);
                System.out.println("Set cacheSize to " + s);
            }
            engine = new Engine(indexes.values(), classLoaders, cacheSize);
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
/*
    	String pathinfo = request.getPathInfo();
        if (pathinfo != null && request.getPathInfo().indexOf("/monitor/") >= 0) {
            processMonitor(request, response, false);
        } else {
            process(request, response);
        }
*/
        process(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
/*
    	if (request.getPathInfo().indexOf("/monitor/") >= 0) {
            processMonitor(request, response, true);
        } else {
            process(request, response);
        }
*/
        process(request, response);
    }
/*
    public void processMonitor(HttpServletRequest request, HttpServletResponse response, boolean post) throws ServletException {
        try {
            String uri = request.getRequestURL().toString();
            MonitorCall mc = monitors.get(uri);
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
            
            HttpURLConnection huc = (HttpURLConnection)(new URL(url).openConnection());
            CachedHttpURLConnection uc = new CachedHttpURLConnection(huc);
            
            String method = request.getMethod();
            uc.setRequestMethod(method);
            uc.setDoInput(true);
            uc.setDoOutput(post);

            Enumeration requestHeaders = request.getHeaderNames();
            while (requestHeaders.hasMoreElements()) {
                String key = (String)requestHeaders.nextElement();
                uc.setRequestProperty(key, request.getHeader(key));
            }
            
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
                if (entry.getKey() != null && !entry.getKey().equals("")) {
                    for (String value: entry.getValue()) {
                        response.setHeader(entry.getKey(), value);
                    }
                }
            }
            
            if (mc.isPassThrough()) {
            	identityTransformer.transform(new DOMSource(eResponse), new StreamResult(response.getOutputStream()));
            } else {
            	copy_stream(uc.getInputStream(), response.getOutputStream());
            }

            if (mc.getCallId() != null) {
            	identityTransformer.transform(new DOMSource(mc.getParams()), new DOMResult(doc));
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
                identityTransformer.transform(new DOMSource(eResponse), new DOMResult(eRespValue));
                eRespParam.appendChild(eRespValue);
                eParams.appendChild(eRespParam);
                net.sf.saxon.s9api.DocumentBuilder builder = core.getEngine().getBuilder();
                XdmNode paramsNode = builder.build(new DOMSource(doc));
                monitorCallSeq++;
                String callId = mc.getCallId() + "_" + Integer.toString(monitorCallSeq);
                core.callTest(mc.getContext(), mc.getLocalName(), mc.getNamespaceURI(), paramsNode.getUnderlyingNode(), callId);
            }
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }
*/
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            FileItemFactory ffactory;
            ServletFileUpload upload;
            List /* FileItem */ items = null;
            HashMap<String, String> params = new HashMap<String, String>();
            boolean multipart = ServletFileUpload.isMultipartContent(request);
            if (multipart) {
                ffactory = new DiskFileItemFactory();
                upload = new ServletFileUpload(ffactory);
                items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (item.isFormField()) {
                        params.put(item.getFieldName(), item.getString());
                    }
                }
            } else {
                Enumeration paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String name = (String)paramNames.nextElement();
                    params.put(name, request.getParameter(name));
                }
            }
            HttpSession session = request.getSession();
            ServletOutputStream out = response.getOutputStream();
            String operation = params.get("te-operation");
            if (operation.equals("Test")) {
                TestSession s = new TestSession();
                String user = request.getRemoteUser();
                File logdir = new File(conf.getUsersDir(), user);
                String mode = params.get("mode");
                RuntimeOptions opts = new RuntimeOptions();
                opts.setWorkDir(conf.getWorkDir());
                opts.setLogDir(logdir);
                if (mode.equals("retest")) {
                    opts.setMode(Test.RETEST_MODE);
                    String sessionid = params.get("session");
                    String test = params.get("test");
                    if (sessionid == null) {
                        int i = test.indexOf("/");
                        sessionid = i > 0 ? test.substring(0, i) : test;
                    }
                    opts.setSessionId(sessionid);
                    if (test == null) {
                        opts.addTestPath(sessionid);
                    } else {
                        opts.addTestPath(test);
                    }
                    /* begin 2011-12-21 PwD
                    for (Entry<String,String> entry : params.entrySet()) {
                        if(entry.getKey().startsWith("profile_")) {
                            String profileId = entry.getValue();
                    */
                    for (String key : new java.util.TreeSet<String>(params.keySet())) {
                    	if (key.startsWith("profile_")) {
                    		String profileId = params.get(key);
                    // end 2011-12-21 PwD
                            int i = profileId.indexOf("}");
                            opts.addTestPath(sessionid + "/" + profileId.substring(i + 1));
                        }
                    }

                    s.load(logdir, sessionid);
                    opts.setSourcesName(s.getSourcesName());
                } else if (mode.equals("resume")) {
                    opts.setMode(Test.RESUME_MODE);
                    String sessionid = params.get("session");
                    opts.setSessionId(sessionid);
                    s.load(logdir, sessionid);
                    opts.setSourcesName(s.getSourcesName());
// begin 2011-06-10 PwD
                } else if (mode.equals("cache")) {
                    opts.setMode(Test.REDO_FROM_CACHE_MODE);
                    String sessionid = params.get("session");
                    opts.setSessionId(sessionid);
                    s.load(logdir, sessionid);
                    opts.setSourcesName(s.getSourcesName());
// end 2011-06-10 PwD
// begin 2011-12-10 PwD
                    ArrayList<String> profiles = new ArrayList<String>();
                    /* begin 2011-12-21 PwD
                    for (Entry<String,String> entry : params.entrySet()) {
                        if(entry.getKey().startsWith("profile_")) {
                            profiles.add(entry.getValue());
                            opts.addProfile(entry.getValue());
                        }
                    }                    
                    */
                    for (String key : new java.util.TreeSet<String>(params.keySet())) {
                    	if (key.startsWith("profile_")) {
                    		String profileId = params.get(key);
                    		profiles.add(profileId);
                    		opts.addProfile(profileId);
                    	}
                    }
                    // end 2011-12-21 PwD
                    s.setProfiles(profiles);
// end 2011-12-10 PwD
                } else {
                    opts.setMode(Test.TEST_MODE);
                    String sessionid = LogUtils.generateSessionId(logdir);
                    s.setSessionId(sessionid);
                    String sources = params.get("sources");
                    s.setSourcesName(sources);
                    SuiteEntry suite = conf.getSuites().get(sources);
                    s.setSuiteName(suite.getId());
//                    String suite = params.get("suite");
//                    s.setSuiteName(suite);
                    String description = params.get("description");
                    s.setDescription(description);
                    opts.setSessionId(sessionid);
                    opts.setSourcesName(sources);
                    opts.setSuiteName(suite.getId());
                    ArrayList<String> profiles = new ArrayList<String>();
                    /* begin 2011-12-21 PwD
                    for (Entry<String,String> entry : params.entrySet()) {
                        if(entry.getKey().startsWith("profile_")) {
                            profiles.add(entry.getValue());
                            opts.addProfile(entry.getValue());
                        }
                    }
                    */
                    for (String key : new java.util.TreeSet<String>(params.keySet())) {
                    	if (key.startsWith("profile_")) {
                    		String profileId = params.get(key);
                    		profiles.add(profileId);
                    		opts.addProfile(profileId);
                    	}
                    }
                    // end 2011-12-21 PwD
                    s.setProfiles(profiles);
                    s.save(logdir);
                }
                String webdir = conf.getWebDirs().get(s.getSourcesName());
//                String requestURI = request.getRequestURI();
//                String contextPath = requestURI.substring(0, requestURI.indexOf(request.getServletPath()) + 1);
//                URI contextURI = new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), contextPath, null, null);
                URI contextURI = new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), request.getRequestURI(), null, null);
                if (webdir == null) {
                    webdir = ".";
                }
                opts.setBaseURI(new URL(contextURI.toURL(), webdir + "/").toString());
//                URI baseURI = new URL(contextURI.toURL(), webdir).toURI();
//                String base = baseURI.toString() + URLEncoder.encode(webdir, "UTF-8") + "/";
//                opts.setBaseURI(base);
//System.out.println(opts.getSourcesName());
                TECore core = new TECore(engine, indexes.get(opts.getSourcesName()), opts);
                String servletURL = request.getRequestURL().toString();
                core.setTestServletURL(servletURL);
                MonitorServlet.setBaseServletURL(servletURL.substring(0, servletURL.lastIndexOf('/')));
//System.out.println(indexes.get(opts.getSourcesName()).toString());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                core.setOut(ps);
                core.setWeb(true);
                Thread thread = new Thread(core);
                session.setAttribute("testsession", core);
                thread.start();
                response.setContentType("text/xml");
                out.println("<thread id=\"" + thread.getId()
                        + "\" sessionId=\"" + s.getSessionId() + "\"/>");
            } else if (operation.equals("Stop")) {
                response.setContentType("text/xml");
                TECore core = (TECore)session.getAttribute("testsession");
                if (core != null) {
                    core.stopThread();
                    session.removeAttribute("testsession");
                    out.println("<stopped/>");
                } else {
                    out.println("<message>Could not retrieve core object</message>");
                }
            } else if (operation.equals("GetStatus")) {
                TECore core = (TECore)session.getAttribute("testsession");
                response.setContentType("text/xml");
                out.print("<status");
                if (core.getFormHtml() != null) {
                    out.print(" form=\"true\"");
                }
                if (core.isThreadComplete()) {
                    out.print(" complete=\"true\"");
                    session.removeAttribute("testsession");
                }
                out.println(">");
                out.print("<![CDATA[");
//                out.print(core.getOutput());
                out.print(URLEncoder.encode(core.getOutput(), "UTF-8").replace('+', ' '));
                out.println("]]>");
                out.println("</status>");
            } else if (operation.equals("GetForm")) {
                TECore core = (TECore)session.getAttribute("testsession");
                String html = core.getFormHtml();
                core.setFormHtml(null);
                response.setContentType("text/html");
                out.print(html);
            } else if (operation.equals("SubmitForm")) {
                TECore core = (TECore)session.getAttribute("testsession");
                Document doc = DB.newDocument();
                Element root = doc.createElement("values");
                doc.appendChild(root);
                for (String key : params.keySet()) {
                    if (!key.startsWith("te-")) {
                        Element valueElement = doc.createElement("value");
                        valueElement.setAttribute("key", key);
                        valueElement.appendChild(doc.createTextNode(params.get(key)));
                        root.appendChild(valueElement);
                    }
                }
                if (multipart) {
                    Iterator iter = items.iterator();
                    while (iter.hasNext()) {
                        FileItem item = (FileItem) iter.next();
                        if (!item.isFormField() && !item.getName().equals("")) {
                            File uploadedFile = new File(core.getLogDir(),
                                StringUtils.getFilenameFromString(item.getName()));
                            item.write(uploadedFile);
                            Element valueElement = doc.createElement("value");
                            String key = item.getFieldName();
                            valueElement.setAttribute("key", key);
                            if (core.getFormParsers().containsKey(key)) {
                                Element parser = core.getFormParsers().get(key); 
                                URL url = uploadedFile.toURI().toURL();
                                Element resp = core.parse(url.openConnection(), parser, doc);
                                Element content = DomUtils.getElementByTagName(resp, "content");
                                if (content != null) {
                                    Element child = DomUtils.getChildElement(content);
                                    if (child != null) {
                                        valueElement.appendChild(child);
                                    }
                                }
                            } else {
                                Element fileEntry = doc.createElementNS(CTL_NS, "file-entry");
                                fileEntry.setAttribute("full-path", uploadedFile.getAbsolutePath().replace('\\','/'));
                                fileEntry.setAttribute("media-type", item.getContentType());
                                fileEntry.setAttribute("size", String.valueOf(item.getSize()));
                                valueElement.appendChild(fileEntry);
                            }
                            root.appendChild(valueElement);
                        }
                    }
                }
                core.setFormResults(doc);
                response.setContentType("text/html");
                out.println("<html>");
                out.println("<head><title>Form Submitted</title></head>");
                out.print("<body onload=\"window.parent.formSubmitted()\"></body>");
                out.println("</html>");
            }
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }
/*
    Element encodeRequest(HttpServletRequest request, Document doc, byte[] data) throws Exception {
        Element eRequest = doc.createElementNS(CTL_NS, "ctl:request");
        Element eURL = doc.createElementNS(CTL_NS, "ctl:url");
        eURL.setTextContent(request.getRequestURL() + request.getPathInfo());
        eRequest.appendChild(eURL);
        Element eMethod = doc.createElementNS(CTL_NS, "ctl:method");
        eMethod.setTextContent(request.getMethod());
        eRequest.appendChild(eMethod);
        Enumeration requestHeaders = request.getHeaderNames();
        while (requestHeaders.hasMoreElements()) {
            String key = (String)requestHeaders.nextElement();
            Element eHeader = doc.createElementNS(CTL_NS, "ctl:header");
            eHeader.setAttribute("name", key);
            eHeader.setTextContent(request.getHeader(key));
            eRequest.appendChild(eHeader);
        }
        Enumeration params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String key = (String)params.nextElement();
            Element eParam = doc.createElementNS(CTL_NS, "ctl:param");
            eParam.setAttribute("name", key);
            eParam.setTextContent(request.getParameter(key));
            eRequest.appendChild(eParam);
        }
        if (data != null) {
            String mime = request.getContentType();
            if (mime.indexOf("text/xml") == 0 || mime.indexOf("application/xml") == 0) {
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                Element eBody = doc.createElementNS(CTL_NS, "ctl:body");
                Transformer t = TransformerFactory.newInstance().newTransformer();
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

    static void copy_stream(InputStream in, OutputStream out) throws IOException {
        int i = in.read();
        while (i >= 0) {
                out.write(i);
                i = in.read();
        }
//      in.close();
//      out.close();
    }
*/
}
