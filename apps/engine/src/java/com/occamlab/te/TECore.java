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

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.S9APIUtils;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.type.Type;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.index.ParserEntry;
import com.occamlab.te.index.TemplateEntry;
import com.occamlab.te.index.TestEntry;
import com.occamlab.te.saxon.ObjValue;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.StringUtils;

/**
 * Provides various utility methods to support test execution and logging.
 *
 */
public class TECore {
    String sessionId;
    File logDir = null;
    PrintStream out;
    boolean web = false;
    int mode = Test.TEST_MODE;
    String testPath;
    String indent = "";
//    long memThreshhold;
    int result;
    Document prevLog = null;
    PrintWriter logger = null;
    String formHtml;
    Document formResults;
    Map<String, Object>functionInstances = new HashMap<String, Object>();
    Map<String, Object>parserInstances = new HashMap<String, Object>();
    Map<String, Method>parserMethods = new HashMap<String, Method>();
    
    static final int PASS = 0;
    static final int WARNING = 1;
    static final int INHERITED_FAILURE = 2;
    static final int FAIL = 3;

    static final String XSL_NS = Test.XSL_NS;
    static final String CTL_NS = Test.CTL_NS;
    static final String TE_NS = Test.TE_NS;
    
    static final QName TECORE_QNAME = new QName("te", TE_NS, "core");
    static final QName TEPARAMS_QNAME = new QName("te", TE_NS, "params");
    static final QName LOCALNAME_QNAME = new QName("local-name");
    static final QName LABEL_QNAME = new QName("label");
    
    public TECore(String sessionId) {
        this.sessionId = sessionId;
        
        testPath = sessionId;
        out = System.out;
            }
    
//    static boolean freeExecutable() {
//        Set<String> keys = Globals.loadedExecutables.keySet();
//        synchronized(Globals.loadedExecutables) {
//            Iterator<String> it = keys.iterator();
//            if (it.hasNext()) {
//                Globals.loadedExecutables.remove(it.next());
//                return true;
//            }
//        }
//        return false;
//    }
    
    public XdmNode executeTemplate(TemplateEntry template, XdmNode params, XPathContext context) throws SaxonApiException {
        XsltExecutable executable = template.loadExecutable();
//        String key = template.getId();
//        XsltExecutable executable = Globals.loadedExecutables.get(key);
//        while (executable == null) {
//            try {
////                System.out.println(template.getTemplateFile().getAbsolutePath());
//                Source source = new StreamSource(template.getTemplateFile());
//                executable = Globals.compiler.compile(source);
//                Globals.loadedExecutables.put(key, executable);
//            } catch (OutOfMemoryError e) {
//                boolean freed = freeExecutable();
//                if (!freed) {
//                    throw e;
//                }
//            }
//        }
//        
//        Runtime rt = Runtime.getRuntime();
//        while (rt.totalMemory() - rt.freeMemory() > memThreshhold) {
//            boolean freed = freeExecutable();
//            if (!freed) {
//                break;
//            }
//        }

        XsltTransformer xt = executable.load();
        XdmDestination dest = new XdmDestination();
        xt.setDestination(dest);
        if (template.usesContext() && context != null) {
//            XdmNode n = S9APIUtils.makeNode((NodeInfo)context.getContextItem());
//            xt.setSource(n.asSource());
            xt.setSource((NodeInfo)context.getContextItem());
//            System.out.println(n);
        } else {
            xt.setSource(new StreamSource(new StringReader("<nil/>")));
        }
        xt.setParameter(TECORE_QNAME, new ObjValue(this));
        if (params != null) {
            xt.setParameter(TEPARAMS_QNAME, params);
        }
        xt.transform();
        XdmNode ret = dest.getXdmNode();
//        if (ret != null) System.out.println(ret.toString());
        return ret;
//        XdmNode ret = dest.getXdmNode();
//        if (ret == null) {
//            return null;
//        } else {
//            System.out.println(ret.toString());
//            return ret.getTypedValue();
//        }
    }
    
    static String getLabel(XdmNode n) {
        String label = n.getAttributeValue(LABEL_QNAME);
        if (label == null) {
            XdmNode value = (XdmNode)n.axisIterator(Axis.CHILD).next();
            XdmItem childItem = value.axisIterator(Axis.CHILD).next();
            if (childItem == null) {
                XdmSequenceIterator it = value.axisIterator(Axis.ATTRIBUTE);
                if (it.hasNext()) {
                    label = it.next().getStringValue();
                } else {
                    label = "";
                }
            } else if (childItem.isAtomicValue()) {
                label = childItem.getStringValue();
            } else if (childItem instanceof XdmNode) {
                XdmNode n2 = (XdmNode)childItem;
                if (n2.getNodeKind() == XdmNodeKind.ELEMENT ) {
                    label = "<" + n2.getNodeName().toString() + ">";
                } else {
                    label = n2.toString();
                }
            }
        }
        return label;
    }
    
    static String getAssertionValue(String text, XdmNode paramsVar) {
        if (text.indexOf("$") < 0) {
            return text;
        }
        
        String newText = text;
        XdmNode params = (XdmNode)paramsVar.axisIterator(Axis.CHILD).next();
        XdmSequenceIterator it = params.axisIterator(Axis.CHILD);
        while (it.hasNext()) {
            XdmNode n = (XdmNode)it.next();
            String tagname = n.getNodeName().getLocalName(); 
            if (tagname.equals("param")) {
                String name = n.getAttributeValue(LOCALNAME_QNAME);
                String label = getLabel(n);
                newText = StringUtils.replaceAll(newText, "{$" + name + "}", label);
            }
            if (tagname.equals("context")) {
                String label = getLabel(n);
                newText = StringUtils.replaceAll(newText, "{$context}", label);
            }
        }
        return newText;
    }
    
    static String getResultDescription(int result) {
        if (result == PASS) {
            return "Passed";
        } else if (result == WARNING) {
            return ("generated a Warning.");
        } else if (result == INHERITED_FAILURE){
            return "Failed (Inherited failure)";
        } else {
            return "Failed";
        }
    }

    static int getResultFromLog(Document log) throws Exception {
        if (log != null) {
            NodeList nl = log.getElementsByTagName("endtest");
            if (nl != null) {
                Element endtest = (Element) nl.item(0);
                return Integer.parseInt(endtest.getAttribute("result"));
            }
        }
        return -1;
    }

    
    public void executeTest(TestEntry test, XdmNode params, XPathContext context) throws Exception {
        String assertion = getAssertionValue(test.getAssertion(), params);
        out.println(indent + "Testing " + test.getName() + " (" + testPath + ")...");
        out.println(indent + "   " + assertion);

        Document oldPrevLog = prevLog;
        if (mode == Test.RESUME_MODE) {
            prevLog = readLog();
        } else {
            prevLog = null;
        }
        
        PrintWriter oldLogger = logger;
        if (logDir != null) {
            logger = createLog();
            logger.println("<log>");
            logger.println("<starttest local-name=\"" + test.getLocalName() + "\" " + 
                                      "prefix=\"" + test.getPrefix() + "\" " +
                                      "namespace-uri=\"" + test.getNamespaceURI() + "\">");
            logger.println("<assertion>" + assertion + "</assertion>");
            if (params != null) {
                logger.println(params.toString());
            }
            if (test.usesContext()) {
                logger.println("<context>");
                NodeInfo contextNode = (NodeInfo)context.getContextItem();
                int kind = contextNode.getNodeKind(); 
                if (kind == Type.ATTRIBUTE) {
                    logger.print("<value " + contextNode.getDisplayName() + "=\""
                                           + contextNode.getStringValue() + "\"");
                    // TODO: set namespace
                    logger.println("/>");
                } else if (kind == Type.ELEMENT || kind == Type.DOCUMENT) {
                    logger.println("<value>");
                    logger.println(Globals.builder.build(contextNode).toString());
                    logger.println("</value>");
                }
                logger.println("</context>");
                
            }
            logger.println("</starttest>");
        }
        
        result = PASS;
        try {
            executeTemplate(test, params, context);
        } catch (SaxonApiException e) {
            out.println(e.getMessage());
            if (logger != null) {
                logger.println("<exception><![CDATA[" + e.getMessage() + "]]></exception>");
                result = FAIL;
            }
        }

        if (logger != null) {
            logger.println("<endtest result=\"" + Integer.toString(result) + "\"/>");
            logger.println("</log>");
            logger.close();
        }
        logger = oldLogger;

        prevLog = oldPrevLog;

        out.println(indent + "Test " + test.getName() + " " + getResultDescription(result));
    }
    
    public void callTest(XPathContext context, String localName, String NamespaceURI, NodeInfo params, String callId) throws Exception {
//        System.out.println("call_test");
//        System.out.println(params.getClass().getName());
        String key = "{" + NamespaceURI + "}" + localName;
        TestEntry test = Globals.masterIndex.getTest(key);

//        PrintWriter logger = getLogger();
        if (logger != null) {
            logger.println("<testcall path=\"" + testPath + "/" + callId + "\"/>");
        }
        if (mode == Test.RESUME_MODE) {
            Document doc = LogUtils.readLog(logDir, testPath + "/" + callId);
            int result = getResultFromLog(doc);
            if (result >= 0) {
                out.println(indent + "   " + "Test " + test.getName() + " " + getResultDescription(result));
                if (result == WARNING) {
                    warning();
                } else if (result != PASS){
                    inheritedFailure();
                }
                return;
            }
//            File logFile = new File(new File(new File(logDir, testPath), callId), "log.xml");
//            if (logFile.exists()) {
//                Processor processor = new Processor(false);
//                net.sf.saxon.s9api.XPathCompiler compiler = processor.newXPathCompiler();
//                net.sf.saxon.s9api.XPathExecutable xpe = compiler.compile("/log/endtest/@result");
//                net.sf.saxon.s9api.XPathSelector selector = xpe.load();
//                net.sf.saxon.s9api.DocumentBuilder db = processor.newDocumentBuilder();
//                net.sf.saxon.s9api.XdmNode node = db.build(logFile);
//                selector.setContextItem(node);
//                XdmItem value = selector.evaluateSingle();
//                if (value != null) {
//                    out.println(indent + "   " + "Test " + test.getName() + " " + getResultDescription(result));
//                    if (result == WARNING) {
//                        warning();
//                    } else if (result != PASS){
//                        inheritedFailure();
//                    }
//                    return;
//                }
//            }
        }

        String oldIndent = indent;
        String oldTestPath = testPath;
        int oldResult = result;
        indent += "   ";
        testPath += "/" + callId;
        executeTest(test, S9APIUtils.makeNode(params), context);
        testPath = oldTestPath;
        indent = oldIndent;
        if (result < oldResult) {
            // Restore parent result if the child results aren't worse
            result = oldResult;
        } else if (result == FAIL && oldResult != FAIL) { 
            // If the child result was FAIL and parent hasn't directly failed, set parent result to INHERITED_FAILURE
            result = INHERITED_FAILURE;
        } else {
            // Keep child result as parent result
        }
    }
    
    public NodeInfo callFunction(XPathContext context, String localName, String NamespaceURI, NodeInfo params) throws Exception {
//        System.out.println("callFunction {" + NamespaceURI + "}" + localName);
        String key = "{" + NamespaceURI + "}" + localName;
        FunctionEntry entry = Globals.masterIndex.getFunction(key);

        if (entry.isJava()) {
            //TODO: implement
            System.out.println("Attempt to call a java function with call-function");
            return null;
        } else {
            XdmNode n = executeTemplate(entry, S9APIUtils.makeNode(params), context);
            return n.getUnderlyingNode();
//System.out.println(S9APIUtils.makeNode(params));
//XdmValue v = executeTemplate(entry, S9APIUtils.makeNode(params), null);
//XdmSequenceIterator it = v.iterator(); 
//XdmItem item = it.next();
//if (item == null) {
//} else if (item.isAtomicValue()) {
//boolean b = it.hasNext();
//System.out.println(item.getStringValue());
//} else {
//XdmNode n = (XdmNode)item;
//System.out.println(n.toString());
//}
//return v;
//System.out.println(n.toString());
//DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//Document doc = db.newDocument();
//Element root = doc.createElement("values");
//doc.appendChild(root);
//return doc;
//            return Globals.builder.build(new StreamSource(new StringReader(n.toString()))).getUnderlyingNode();
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            dbf.setNamespaceAware(true);
//            Document doc = dbf.newDocumentBuilder().newDocument();
//            doc.appendChild(doc.createElementNS("blah", "blah:test"));
////            TransformerFactory.newInstance().newTransformer().transform(n.asSource(), new DOMResult(doc));
//            return doc;
//System.out.println(S9APIUtils.makeNode(n.getUnderlyingNode()).toString());            
        }
    }
  
//    public Document dummyFunction() throws Exception {
//DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//Document doc = db.newDocument();
//Element root = doc.createElement("values");
//root.appendChild(doc.createTextNode("x"));
//doc.appendChild(root);
//return doc;
//  }

    public void warning() {
        if (result < WARNING) {
            result = WARNING;
        }
    }

    public void inheritedFailure() {
        if (result < INHERITED_FAILURE) {
            result = INHERITED_FAILURE;
        }
    }
    
    public void fail() {
        if (result < FAIL) {
            result = FAIL;
        }
    }
    
    public void setContextLabel(String label) {
        // TODO: implement
        System.out.println("setcontextLabel(" + label + ")");
    }

    public String getFormHtml() {
        return formHtml;
    }

    public void setFormHtml(String html) {
        this.formHtml = html;
    }

    public Document getFormResults() {
        return formResults;
    }

    public void setFormResults(Document doc) {
        this.formResults = doc;
    }

    public Document readLog() throws Exception {
        return LogUtils.readLog(logDir, testPath);
    }

    public PrintWriter createLog() throws Exception {
//        PrintWriter logger = null;
        if (logDir != null) {
            File dir = new File(logDir, testPath);
            dir.mkdir();
            File f = new File(dir, "log.xml");
            f.delete();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f), "UTF-8"));
            logger = new PrintWriter(writer);
//            logger.println("<log>");
        }
//        loggers.push(logger);
        return logger;
    }

    public static Node reset_log(String logdir, String callpath)
            throws Exception {
        if (logdir.length() > 0) {
            File dir = new File(logdir, callpath);
            dir.mkdir();
            File f = new File(dir, "log.xml");
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            raf.setLength(0);
            raf.writeBytes("<log>\n</log>\n");
            raf.close();
        }
        return null;
    }

    // Get a File pointer to a file reference (in XML)
    public static File getFile(NodeList fileNodes) {
        File file = null;
        for (int i = 0; i < fileNodes.getLength(); i++) {
            Element e = (Element) fileNodes.item(i);
            String type = e.getAttribute("type");

            try {
                // URL, File, or Resource
                if (type.equals("url")) {
                    URL url = new URL(e.getTextContent());
                    file = new File(url.toURI());
                } else if (type.equals("file")) {
                    file = new File(e.getTextContent());
                } else if (type.equals("resource")) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    file = new File(cl.getResource(e.getTextContent()).getFile());
                } else {
                    System.out.println("Incorrect file reference:  Unknown type!");
                }
            } catch (Exception exception) {
            	System.err.println("Error getting file. " + exception.getMessage());
            	return null;
            }
        }
        return file;
    }
    
    public Node request(Document ctlRequest, String id) throws Throwable {
        //TODO: ID won't necessarily be unique if called from a function
//        String id = Long.toString(System.currentTimeMillis()) + "_" + Integer.toString(seqno);
        Element request = (Element)ctlRequest.getElementsByTagNameNS(Test.CTL_NS, "request").item(0);
        if (mode == Test.RESUME_MODE) {
            NodeList nl = prevLog.getElementsByTagName("request");
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element)nl.item(i);
                if (e.getAttribute("id").equals(id)) {
                    NodeList nl2 = e.getElementsByTagName("response"); 
                    if (nl2.getLength() == 1) {
                        logger.println(DomUtils.serializeNode(e));
                        NodeList nl3 = nl2.item(0).getChildNodes();
                        for (int j = 0; j < nl3.getLength(); j++) {
                            Node n = nl3.item(j); 
                            if (n.getNodeType() == Node.ELEMENT_NODE) {
                                return n;
                            }
                        }
                    }
                }
            }
        }

        if (logger != null) {
            logger.println("<request id=\"" + id + "\">");
            logger.println(DomUtils.serializeNode(request));
        }
        Exception ex = null;
        Element response = null;
        Element parserInstruction = null;
        NodeList nl = request.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i); 
            if (n.getNodeType() == Node.ELEMENT_NODE && !n.getNamespaceURI().equals(CTL_NS)) {
                parserInstruction = (Element)n;
            }
        }
        try {
            URLConnection uc = build_request(request);
            response = parse(uc, parserInstruction);
            if (logger != null) {
                logger.println(DomUtils.serializeNode(response));
            }
        } catch (Exception e) {
            ex = e;
        }
        if (logger != null) {
            logger.println("</request>");
        }
        if (ex == null) {
            Node n = response.getElementsByTagName("content").item(0);
//            System.out.println(DomUtils.serializeNode(n.getFirstChild()));
            return n.getFirstChild();
        } else {
            throw ex;
        }
    }

    // Create and send an HttpRequest then return an HttpResponse (HttpResponse)
    static URLConnection build_request(Node xml) throws Exception {
        Node body = null;
        ArrayList<String[]> headers = new ArrayList<String[]>();
        ArrayList<Node> parts = new ArrayList<Node>();
        String sUrl = null;
        String sParams = "";
        String method = "GET";
        String charset = "UTF-8";
	boolean multipart = false;

        // Read in the test information (from CTL)
        NodeList nl = xml.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = (Node) nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getLocalName().equals("url")) {
                    sUrl = n.getTextContent();
                } else if (n.getLocalName().equals("method")) {
                    method = n.getTextContent().toUpperCase();
                } else if (n.getLocalName().equals("header")) {
                    headers.add(new String[] {
                            ((Element) n).getAttribute("name"),
                            n.getTextContent() });
                } else if (n.getLocalName().equals("param")) {
                    if (sParams.length() > 0) sParams += "&";
                    sParams += ((Element) n).getAttribute("name") + "="
                            + n.getTextContent();
                } else if (n.getLocalName().equals("body")) {
                    body = n;
                } else if (n.getLocalName().equals("part")) {
                    parts.add(n);
                }
            }
        }

        // Complete GET KVP syntax
        if (method.equals("GET") && sParams.length() > 0) {
            if (sUrl.indexOf("?") == -1) {
                sUrl += "?";
            }
            else if (!sUrl.endsWith("?") && !sUrl.endsWith("&")) {
                sUrl += "&";
            }
            sUrl += sParams;
        }

        // System.out.println(sUrl);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();

        // Open the URLConnection
        URLConnection uc = new URL(sUrl).openConnection();
        if (uc instanceof HttpURLConnection) {
            ((HttpURLConnection) uc).setRequestMethod(method);
        }

        // POST setup (XML payload and header information)
        if (method.equals("POST") || method.equals("PUT")) {
            uc.setDoOutput(true);
            byte[] bytes = null;
            String mime = null;

            // KVP over POST
            if (body == null) {
                bytes = sParams.getBytes();
                mime = "application/x-www-form-urlencoded";
            }
            // XML POST
            else {
                String bodyContent = "";

                NodeList children = body.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        t.transform(new DOMSource(children.item(i)),
                                new StreamResult(baos));
                        bodyContent = baos.toString();
                        bytes = baos.toByteArray();

/*
                        // Determine if we need to set a different Content-Type value (SOAP)
			for (int j = 0; j < headers.size(); j++) {
				String[] header = (String[]) headers.get(j);
				if (header[0].toLowerCase().equals("content-type")) {
					mime = header[1];
				}
			}
*/

                        if (mime == null) {
                        	mime = "application/xml; charset=" + charset;
                	}
                        break;
                    }
                }
                if (bytes == null) {
                    bytes = body.getTextContent().getBytes();
                    mime = "text/plain";
                }

                // Add parts if present
                if (parts.size() > 0) {
                    String prefix = "--";
                    String boundary = "7bdc3bba-e2c9-11db-8314-0800200c9a66";
                    String newline = "\r\n";
		    multipart = true;

                    // Set main body and related headers
                    ByteArrayOutputStream contentBytes = new ByteArrayOutputStream();
                    String bodyPart = prefix + boundary + newline;
                    bodyPart += "Content-Type: " + mime + newline + newline;
                    bodyPart += bodyContent;
		    writeBytes(contentBytes, bodyPart.getBytes(charset));

                    // Append all parts to the original body, seperated by the
                    // boundary sequence
                    for (int i = 0; i < parts.size(); i++) {
                        Element currentPart = (Element) parts.get(i);
                        String cid = currentPart.getAttribute("cid");
                        if (cid.indexOf("cid:") != -1) cid = cid.substring(cid.indexOf("cid:")+"cid:".length());
                        String contentType = currentPart.getAttribute("content-type");

                        // Default encodings and content-type
                        if (contentType.equals("application/xml")) {
                            contentType = "application/xml; charset=" + charset;
                        }
                        if (contentType == null || contentType.equals("")) {
                            contentType = "application/octet-stream";
                        }

                        // Set headers for each part
                    	ByteArrayOutputStream partBytes = new ByteArrayOutputStream();
                        String partHeaders = newline + prefix + boundary + newline;
                        partHeaders += "Content-Type: " + contentType + newline;
                        partHeaders += "Content-ID: <" + cid + ">" + newline + newline;
			writeBytes(contentBytes, partHeaders.getBytes(charset));

                        // Get the fileName, if it exists
                        NodeList files = currentPart.getElementsByTagNameNS(
                                CTL_NS, "file");

                        // Get part for a specified file
                        if (files.getLength() > 0) {
                            File contentFile = getFile(files);

			    InputStream is = new FileInputStream(contentFile);
			    long length = contentFile.length();
			    byte[] fileBytes = new byte[(int)length];
			    int offset = 0;
			    int numRead = 0;
			    while (offset < fileBytes.length && (numRead=is.read(fileBytes, offset, fileBytes.length-offset)) >= 0) {
			    	offset += numRead;
			    }
			    is.close();

			    writeBytes(contentBytes, fileBytes);
                        }
                        // Get part from inline data (or xi:include)
                        else {
                            // Text
                            if (currentPart.getFirstChild() instanceof Text) {
				writeBytes(contentBytes, currentPart.getTextContent().getBytes(charset));
                            }
                            // XML
                            else {
                                writeBytes(contentBytes, DomUtils.serializeNode(currentPart.getFirstChild()).getBytes(charset));
                            }
                        }
                    }

                    String endingBoundary = newline + prefix + boundary + prefix + newline;
		    writeBytes(contentBytes, endingBoundary.getBytes(charset));

                    bytes = contentBytes.toByteArray();

                    // Global Content-Type and Length to be added after the
                    // parts have been parsed
                    mime = "multipart/related; type=\""+ mime
                    	    + "\"; boundary=\"" + boundary + "\"";

		    //String contentsString = new String(bytes, charset);
                    //System.out.println("Content-Type: "+mime+"\n"+contentsString);
                }
            }

            // Set headers
            if (body != null) {
	        String mid = ((Element) body).getAttribute("mid");
                if (mid != null && !mid.equals("")) {
	    	    if (mid.indexOf("mid:") != -1) mid = mid.substring(mid.indexOf("mid:")+"mid:".length());
                    uc.setRequestProperty("Message-ID", "<" + mid + ">");
                }
            }
            uc.setRequestProperty("Content-Type", mime);
            uc.setRequestProperty("Content-Length", Integer
                    .toString(bytes.length));

            // Enter the custom headers (overwrites the defaults if present)
            for (int i = 0; i < headers.size(); i++) {
                String[] header = (String[]) headers.get(i);
		if (multipart && header[0].toLowerCase().equals("content-type")) {
		}
		else {
                	uc.setRequestProperty(header[0], header[1]);
        	}
            }

            OutputStream os = uc.getOutputStream();
            os.write(bytes);
        }
/*
    	// Get URLConnection values
	InputStream is = uc.getInputStream();
	byte[] respBytes = IOUtils.inputStreamToBytes(is);
	int respCode = ((HttpURLConnection) uc).getResponseCode();
	String respMess = ((HttpURLConnection) uc).getResponseMessage();
    	Map respHeaders = ((HttpURLConnection) uc).getHeaderFields();

	// Construct the HttpResponse (BasicHttpResponse) to send to parsers
	HttpVersion version = new HttpVersion(1,1);
	BasicStatusLine statusLine = new BasicStatusLine(version, respCode, respMess);
	BasicHttpResponse resp = new BasicHttpResponse(statusLine);
	Set respHeadersSet = respHeaders.keySet();
	for( Iterator it = respHeadersSet.iterator(); it.hasNext(); ) {
		String name = (String) it.next();
		List valueList = (List) respHeaders.get(name);
		String value = (String) valueList.get(0);
		if (name == null) continue;
		resp.addHeader(name, value);
	}
	HttpEntity entity = new ByteArrayEntity(respBytes);
	resp.setEntity(entity);

        return resp;
*/
        return uc;
    }

    public static void writeBytes(ByteArrayOutputStream baos, byte[] bytes) {
        baos.write(bytes, 0, bytes.length);
    }

    public Element parse(Document parse_instruction, String xsl_version) throws Throwable {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        Node content = null;
        Document parser_instruction = null;
        
        Element parse_element = (Element)parse_instruction.getElementsByTagNameNS(CTL_NS, "parse").item(0);

        NodeList children = parse_element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) children.item(i);
                if (e.getNamespaceURI().equals(XSL_NS)
                        && e.getLocalName().equals("output")) {
                    Document doc = db.newDocument();
                    Element transform = doc
                            .createElementNS(XSL_NS, "transform");
                    transform.setAttribute("version", xsl_version);
                    doc.appendChild(transform);
                    Element output = doc.createElementNS(XSL_NS, "output");
                    NamedNodeMap atts = e.getAttributes();
                    for (int j = 0; j < atts.getLength(); j++) {
                        Attr a = (Attr) atts.item(i);
                        output.setAttribute(a.getName(), a.getValue());
                    }
                    transform.appendChild(output);
                    Element template = doc.createElementNS(XSL_NS, "template");
                    template.setAttribute("match", "node()|@*");
                    transform.appendChild(template);
                    Element copy = doc.createElementNS(XSL_NS, "copy");
                    template.appendChild(copy);
                    Element apply = doc.createElementNS(XSL_NS,
                            "apply-templates");
                    apply.setAttribute("select", "node()|@*");
                    copy.appendChild(apply);
                    t = tf.newTransformer(new DOMSource(doc));
                } else if (e.getLocalName().equals("content")) {
                    NodeList children2 = e.getChildNodes();
                    for (int j = 0; j < children2.getLength(); j++) {
                        if (children2.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            content = (Element) children2.item(j);
                        }
                    }
                    if (content == null) {
                        content = (Node) children2.item(0);
                    }
                } else {
                    parser_instruction = db.newDocument();
                    tf.newTransformer().transform(new DOMSource(e),
                            new DOMResult(parser_instruction));
                }
            }
        }
        if (t == null) {
            t = tf.newTransformer();
        }
        File temp = File.createTempFile("$te_", ".xml");
        if (content.getNodeType() == Node.TEXT_NODE) {
            RandomAccessFile raf = new RandomAccessFile(temp, "rw");
            raf.writeBytes(((Text) content).getTextContent());
            raf.close();
        } else {
            t.transform(new DOMSource((Node) content), new StreamResult(temp));
        }
        URLConnection uc = temp.toURL().openConnection();
/*
    	// Get URLConnection values
	InputStream is = uc.getInputStream();
	byte[] respBytes = IOUtils.inputStreamToBytes(is);

	// Construct the HttpResponse (BasicHttpResponse) to send to parsers
	HttpVersion version = new HttpVersion(1,1);
	int respCode = 200;
	String respMess = "OK";
	BasicStatusLine statusLine = new BasicStatusLine(version, respCode, respMess);
	BasicHttpResponse resp = new BasicHttpResponse(statusLine);
	HttpEntity entity = new ByteArrayEntity(respBytes);
	resp.setEntity(entity);

        Document doc = parse(resp, null, parser_instruction);
*/
//        Document doc = parse(uc, parser_instruction);
        Element result = parse(uc, parser_instruction);
        temp.delete();
        return result;
    }

    public Element parse(URLConnection uc, Node instruction) throws Throwable {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        Transformer t = TransformerFactory.newInstance().newTransformer();
        Document response_doc = db.newDocument();
        Element parser_e = response_doc.createElement("parser");
        Element response_e = response_doc.createElement("response");
        Element content_e = response_doc.createElement("content");
        if (instruction == null) {
            try {
                InputStream is = uc.getInputStream();
                t.transform(new StreamSource(is), new DOMResult(content_e));
            } catch (Exception e) {
                parser_e.setTextContent(e.getMessage());
            }
        } else {
            Element instruction_e;
            if (instruction instanceof Element) {
                instruction_e = (Element) instruction;
            } else {
                instruction_e = ((Document) instruction).getDocumentElement();
            }
            String key = "{" + instruction_e.getNamespaceURI() + "}"
                    + instruction_e.getLocalName();
            ParserEntry pe = Globals.masterIndex.getParser(key);
            Object instance = null;
            if (pe.isInitialized()) {
                instance = parserInstances.get(key);
                if (instance == null) {
                    instance = Misc.makeInstance(pe.getClassName(), pe.getClassParams());
                    parserInstances.put(key, instance);
                }
            }
            Method method = parserMethods.get(key);
            if (method == null) {
                method = Misc.getMethod(pe.getClassName(), pe.getMethod(), 3, 4);
                parserMethods.put(key, method);
            }
            StringWriter swLogger = new StringWriter();
            PrintWriter pwLogger = new PrintWriter(swLogger);
            int arg_count = method.getParameterTypes().length;
            Object[] args = new Object[arg_count];
            args[0] = uc;
            args[1] = instruction_e;
            args[2] = pwLogger;
            if (arg_count > 3) {
                args[3] = this;
            }
            Object return_object;
            try {
                return_object = method.invoke(instance, args);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                String msg = "Error invoking parser " + pe.getId() + "\n" + cause.getClass().getName();
                if (cause.getMessage() != null) {
                    msg += ": " + cause.getMessage();
                }
                throw new Exception(msg, cause);
            }
            pwLogger.close();
            if (return_object instanceof Node) {
                t.transform(new DOMSource((Node) return_object), new DOMResult(
                        content_e));
            } else if (return_object != null) {
                content_e.appendChild(response_doc.createTextNode(return_object
                        .toString()));
            }
        
            parser_e.setAttribute("prefix", instruction_e.getPrefix());
            parser_e.setAttribute("local-name", instruction_e.getLocalName());
            parser_e.setAttribute("namespace-uri", instruction_e
                    .getNamespaceURI());
            parser_e.setTextContent(swLogger.toString());
        }
        response_e.appendChild(parser_e);
        response_e.appendChild(content_e);
        response_doc.appendChild(response_e);
        return response_e;
    }

    public Node message(String message) {
        String formatted_message = indent + message.trim().replaceAll("\n", "\n" + indent);
        out.println(formatted_message);
        return null;
    }

//    public Node copy(Node node) throws Exception {
//        if (node instanceof Text) {
//            synchronized (Out) {
//                Out.println(node.getTextContent());
//            }
//        } else {
//            TransformerFactory tf = TransformerFactory.newInstance();
//            Transformer t = tf.newTransformer();
//            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//            synchronized (Out) {
//                t.transform(new DOMSource(node), new StreamResult(Out));
//                Out.println("");
//            }
//        }
//        return null;
//    }

    /**
     * Converts CTL input form data to generate a Swing-based or XHTML form and
     * reports the results of processing the submitted form. The results document
     * is produced in {@link TestServlet#processFormData} (web context) or
     * {@link SwingForm.CustomFormView#submitData}.
     *
     * @param ctlForm
     *            a DOM Document representing a &lt;ctl:form&gt; element.
     * @throws java.lang.Exception
     * @return a DOM Document containing the resulting &lt;values&gt; element
     *        as the document element.
     */
    public Document form(Document ctlForm, String id) throws Exception {
        String name = Thread.currentThread().getName();
        Element form = (Element)ctlForm.getElementsByTagNameNS(Test.CTL_NS, "form").item(0);
        NamedNodeMap attrs = form.getAttributes();
        Attr attr = (Attr) attrs.getNamedItem("name");
        if (attr != null) {
            name = attr.getValue();
        }

        // Get "method" attribute - "post" or "get"
        attr = (Attr) attrs.getNamedItem("method");
        String method = "";
        if (attr != null) {
            method = attr.getValue();
        }

	// Determine if there are file widgets or not
	boolean hasFiles = false;
	NodeList inputs = ctlForm.getElementsByTagName("input");
	for (int i = 0; i < inputs.getLength(); i++) {
		NamedNodeMap inputAttrs = inputs.item(i).getAttributes();
		Attr typeAttr = (Attr) inputAttrs.getNamedItem("type");
		if (typeAttr != null) {
			if (typeAttr.getValue().toLowerCase().equals("file")) {
				hasFiles = true;
			}
		}
	}
        
        XsltTransformer xt = Globals.formExecutable.load();
        xt.setSource(new DOMSource(ctlForm));
        xt.setParameter(new QName("title"), new XdmAtomicValue(name));
        xt.setParameter(new QName("web"), new XdmAtomicValue(web ? "yes" : "no"));
        xt.setParameter(new QName("files"), new XdmAtomicValue(hasFiles ? "yes" : "no"));
        xt.setParameter(new QName("thread"), new XdmAtomicValue(Long.toString(Thread.currentThread().getId())));
        xt.setParameter(new QName("method"), new XdmAtomicValue(method.toLowerCase().equals("post") ? "post" : "get"));
        StringWriter sw = new StringWriter();
        Serializer serializer = new Serializer();
        serializer.setOutputWriter(sw);
        serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
        xt.setDestination(serializer);
        xt.transform();
        formHtml = sw.toString();
//System.out.println(this.formHtml);

        if (!web) {
            int width = 700;
            int height = 500;
            attr = (Attr) attrs.getNamedItem("width");
            if (attr != null)
                width = Integer.parseInt(attr.getValue());
            attr = (Attr) attrs.getNamedItem("height");
            if (attr != null)
                height = Integer.parseInt(attr.getValue());
            new SwingForm(name, width, height, this);
        }

        while (formResults == null) {
            Thread.sleep(250);
        }

        Document doc = formResults;
//        System.out.println(DomUtils.serializeNode(doc));
        formResults = null;

        if (logger != null) {
            logger.println("<formresults id=\"" + id + "\">");
            logger.println(DomUtils.serializeNode(doc));
            logger.println("</formresults>");
        }

        return doc;
    }

    public File getLogDir() {
        return logDir;
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public PrintStream getOut() {
        return out;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public String getTestPath() {
        return testPath;
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public boolean isWeb() {
        return web;
    }

    public void setWeb(boolean web) {
        this.web = web;
    }
    
    public Object getFunctionInstance(String key) {
        return functionInstances.get(key);
    }

    public Object putFunctionInstance(String key, Object instance) {
        return functionInstances.put(key, instance);
    }
}
