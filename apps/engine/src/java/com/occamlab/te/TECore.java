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

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.Random;

import javax.xml.parsers.*;
import net.sf.saxon.Configuration;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.StringWriter;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.dom.DocumentBuilderImpl;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Provides various utility methods to support test execution and logging.
 *
 */
public class TECore {

    static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
    static final String CTL_NS = "http://www.occamlab.com/ctl";
    static public DocumentBuilderFactory DBF;

    static String SessionId;
    static String SessionDir;

    static public TransformerFactory TF = TransformerFactory.newInstance();
    Transformer FormTransformer;
    DocumentBuilder DB;

    PrintStream Out;
    String formHtml;
    Document formResults;
    boolean Web;

    public HashMap parserInstances = new HashMap();
    public HashMap parserMethods = new HashMap();

    Stack<PrintWriter> loggers = new Stack<PrintWriter>();

    public TECore(PrintStream out, boolean web) throws Exception {
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");
        DBF = DocumentBuilderFactory.newInstance();
        DBF.setNamespaceAware(true);
        DBF.setFeature(
                "http://apache.org/xml/features/xinclude/fixup-base-uris",
                false);
        DB = DBF.newDocumentBuilder();

        TF = TransformerFactory.newInstance();
        TF.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
        Out = out;
        System.setOut(Out); // sets the stdout to go to the appropriate place
        System.setErr(Out);
        Web = web;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream("com/occamlab/te/formfn.xsl");
        StreamSource xsl_source = new StreamSource(is);
        FormTransformer = TF.newTransformer(xsl_source);
    }

    public static String getSessionId() {
        return SessionId;
    }

    public static String getSessionDir() {
        return SessionDir;
    }

    public void setSessionId(String sessionId) {
        SessionId = sessionId;
    }

    public void setSessionDir(String sessionDir) {
        SessionDir = sessionDir;
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

    public static short node_type(Node node) {
        return node.getNodeType();
    }

    public static void exception(String message) throws Exception {
        throw new Exception(message);
    }

    public static Document read_log(String logdir, String callpath)
            throws Exception {
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(
                "http://apache.org/xml/features/xinclude/fixup-base-uris",
                false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.newDocument();
        File dir = new File(logdir, callpath);
        File f = new File(dir, "log.xml");
        if (f.exists()) {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setErrorListener(new com.occamlab.te.NullErrorListener());
            try {
                t.transform(new StreamSource(f), new DOMResult(doc));
            } catch (Exception e) {
                RandomAccessFile raf = new RandomAccessFile(f, "r");
                int l = new Long(raf.length()).intValue();
                byte[] buf = new byte[l + 8];
                raf.read(buf);
                raf.close();
                buf[l] = '\n';
                buf[l + 1] = '<';
                buf[l + 2] = '/';
                buf[l + 3] = 'l';
                buf[l + 4] = 'o';
                buf[l + 5] = 'g';
                buf[l + 6] = '>';
                buf[l + 7] = '\n';
                doc = db.newDocument();
                tf.newTransformer().transform(
                        new StreamSource(new ByteArrayInputStream(buf)),
                        new DOMResult(doc));
            }
        } else {
            Node root = doc.createElement("log");
            doc.appendChild(root);
        }
        return doc;
    }

    /**
     * Creates a test log.
     *
     * @param logdir
     * @param callpath
     * @return
     * @throws Exception
     */
    public Node create_log(String logdir, String callpath) throws Exception {
        PrintWriter logger = null;
        if (logdir.length() > 0) {
            File dir = new File(logdir, callpath);
            dir.mkdir();
            File f = new File(dir, "log.xml");
            f.delete();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(f), "UTF-8"));
            logger = new PrintWriter(writer);
            logger.println("<log>");
        }
        loggers.push(logger);
        return null;
    }

    // Log additional information to the log file
    public Node log_xml(Node xml) throws Exception {
        PrintWriter logger = (PrintWriter) loggers.peek();
        if (logger != null) {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(xml), new StreamResult(logger));
            logger.println();
        }
        return null;
    }

    // Close the log file
    public Node close_log() throws Exception {
        PrintWriter logger = (PrintWriter) loggers.pop();
        if (logger != null) {
            logger.println("</log>");
            logger.close();
        }
        return null;
    }

    public PrintWriter getLogger() {
        if (loggers.empty()) {
            return null;
        } else {
            return (PrintWriter) loggers.peek();
        }
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
                    ClassLoader cl = Thread.currentThread()
                            .getContextClassLoader();
                    file = new File(cl.getResource(e.getTextContent())
                            .getFile());
                } else {
                    System.out
                            .println("Incorrect file reference:  Unknown type!");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return file;
    }

    // Create a URLConnection to the service with the proper headers, etc
    public static URLConnection build_request(Node xml) throws Exception {
        Node body = null;
        ArrayList<String[]> headers = new ArrayList<String[]>();
        ArrayList<Node> parts = new ArrayList<Node>();
        String sUrl = null;
        String sParams = "";
        String method = "GET";
        String charset = "UTF-8";

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
                    if (sParams.length() > 0)
                        sParams += "&";
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
            if (sUrl.indexOf("?") < 0) {
                sUrl += "?";
            } else if (!sUrl.endsWith("&")) {
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
                        mime = "application/xml; charset=" + charset;
                        break;
                    }
                }
                if (bytes == null) {
                    bytes = body.getTextContent().getBytes();
                    mime = "text/plain";
                }

                // Add parts if present (ebrim specific)
                if (parts.size() > 0) {
                    String prefix = "--";
                    String boundary = "7bdc3bba-e2c9-11db-8314-0800200c9a66";
                    String newline = "\r\n";

                    // Set main body
                    String contents = "";
                    contents += prefix + boundary + newline;
                    contents += "Content-Disposition: form-data; name=\"Transaction\""
                            + "; filename=\"\"" + newline;
                    contents += "Content-Type: " + mime + newline + newline;
                    contents += bodyContent;

                    // Global Content-Type and Length to be added after the
                    // parts have been parsed
                    mime = "multipart/form-data; boundary=" + boundary;

                    // Append all parts to the original body, seperated by the
                    // boundary sequence
                    for (int i = 0; i < parts.size(); i++) {
                        String content = "";
                        Element currentPart = (Element) parts.get(i);
                        String partName = currentPart.getAttribute("name");
                        String contentType = currentPart
                                .getAttribute("content-type");

                        // Default encodings and content-type
                        if (contentType.equals("application/xml")) {
                            contentType = "application/xml; charset=" + charset;
                        }
                        if (contentType == null || contentType.equals("")) {
                            contentType = "application/octet-stream";
                        }

                        // Get the fileName, if it exists
                        NodeList files = currentPart.getElementsByTagNameNS(
                                CTL_NS, "file");
                        String fileName = "";
                        if (files.getLength() > 0) {
                            Element e = (Element) files.item(0);
                            fileName = e.getTextContent();
                        }

                        // Set headers for each part
                        content += "Content-Disposition: form-data; name=\""
                                + partName + "\"" + "; filename=\"" + fileName
                                + "\"" + newline;
                        content += "Content-Type: " + contentType + newline
                                + newline;

                        // Get part for a specified file
                        if (files.getLength() > 0) {
                            File contentFile = getFile(files);

                            StringBuffer buff = new StringBuffer();
                            try {
                                BufferedReader rd = new BufferedReader(
                                        new FileReader(contentFile));
                                String line;
                                while ((line = rd.readLine()) != null) {
                                    buff.append(line + "\n");
                                }
                                rd.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            content += buff.toString();
                        }
                        // Get part from inline data (or xi:include)
                        else {
                            // Text
                            if (currentPart.getFirstChild() instanceof Text) {
                                content += currentPart.getTextContent();
                            }
                            // XML
                            else {
                                content += documentToString(currentPart
                                        .getFirstChild());
                            }
                        }

                        contents += newline + prefix + boundary + newline
                                + content;
                    }

                    contents += newline + prefix + boundary + prefix + newline;

                    // System.out.println("Contents:\n"+contents);
                    bytes = contents.getBytes(charset);
                }
            }

            // Set headers
            uc.setRequestProperty("Content-Type", mime);
            uc.setRequestProperty("Content-Length", Integer
                    .toString(bytes.length));

            // Enter the custom headers (overwrites the defaults if present)
            for (int i = 0; i < headers.size(); i++) {
                String[] header = (String[]) headers.get(i);
                uc.setRequestProperty(header[0], header[1]);
            }

            OutputStream os = uc.getOutputStream();
            os.write(bytes);
        }

        return uc;
    }

    public Document serialize_and_parse(Node parse_instruction)
            throws Throwable {
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(
                "http://apache.org/xml/features/xinclude/fixup-base-uris",
                false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = null;
        Node content = null;
        Document parser_instruction = null;

        NodeList children = parse_instruction.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) children.item(i);
                if (e.getNamespaceURI().equals(XSL_NS)
                        && e.getLocalName().equals("output")) {
                    Document doc = db.newDocument();
                    Element transform = doc
                            .createElementNS(XSL_NS, "transform");
                    transform.setAttribute("version", "1.0");
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
        Document doc = parse(uc, null, parser_instruction);
        temp.delete();
        return doc;
    }

    public Node register_parser(String namespace_uri, String local_name,
            String method_name, Object parser) throws Exception {
        String key = namespace_uri + "," + local_name;

        if (parser instanceof String || parser instanceof Node) {
            parserInstances.put(key, null);
        } else {
            parserInstances.put(key, parser);
        }
        parserMethods.put(key, get_parser_method(method_name, parser));
        return null;
    }

    public static Method get_parser_method(String method_name, Object parser)
            throws Exception {
        Class parser_class;
        if (parser instanceof String) {
            parser_class = Class.forName((String) parser);
        } else if (parser instanceof Node) {
            parser_class = Class.forName(((Node) parser).getTextContent());
        } else {
            parser_class = parser.getClass();
        }
        Method method = null;
        try {
            Class[] types = new Class[4];
            types[0] = URLConnection.class;
            types[1] = Element.class;
            types[2] = PrintWriter.class;
            types[3] = TECore.class;
            method = parser_class.getMethod(method_name, types);
        } catch (java.lang.NoSuchMethodException e) {
            Class[] types = new Class[3];
            types[0] = URLConnection.class;
            types[1] = Element.class;
            types[2] = PrintWriter.class;
            method = parser_class.getMethod(method_name, types);
        }
        return method;
    }

    public Document parse(URLConnection uc, String response_id, Node instruction)
            throws Throwable {
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setFeature(
                "http://apache.org/xml/features/xinclude/fixup-base-uris",
                false);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        Document response_doc = db.newDocument();
        Element parser_e = response_doc.createElement("parser");
        Element response_e = response_doc.createElement("response");
        if (response_id != null) {
            response_e.setAttribute("id", response_id);
        }
        Element content_e = response_doc.createElement("content");
        if (instruction == null) {
            try {
                t.transform(new StreamSource(uc.getInputStream()),
                        new DOMResult(content_e));
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
            String key = instruction_e.getNamespaceURI() + ","
                    + instruction_e.getLocalName();
            Object instance = parserInstances.get(key);
            Method method = (Method) parserMethods.get(key);
            ;
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
                throw e.getTargetException();
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
        return response_doc;
    }

    public Document parse(URLConnection uc, String response_id)
            throws Throwable {
        return parse(uc, response_id, null);
    }

    public Node message(int depth, String message) {
        String indent = "";
        for (int i = 0; i < depth; i++) {
            indent += "  ";
        }
        String formatted_message = indent
                + message.trim().replaceAll("\n", "\n" + indent);
        synchronized (Out) {
            Out.println(formatted_message);
        }
        return null;
    }

    public Node copy(Node node) throws Exception {
        if (node instanceof Text) {
            synchronized (Out) {
                Out.println(node.getTextContent());
            }
        } else {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            synchronized (Out) {
                t.transform(new DOMSource(node), new StreamResult(Out));
                Out.println("");
            }
        }
        return null;
    }

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
    public Document form(Document ctlForm) throws Exception {
        String name = Thread.currentThread().getName();
        NamedNodeMap attrs = ctlForm.getElementsByTagName("form").item(0)
                .getAttributes();
        Attr attr = (Attr) attrs.getNamedItem("name");
        if (attr != null)
            name = attr.getValue();

        // Get "method" attribute - "post" or "get"
        attr = (Attr) attrs.getNamedItem("method");
        String method = "";
        if (attr != null)
            method = attr.getValue();

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

        // Set parameters for use by formfn.xsl
        FormTransformer.setParameter("title", name);
        FormTransformer.setParameter("web", Web ? "yes" : "no");
        FormTransformer.setParameter("files", hasFiles ? "yes" : "no");
        FormTransformer.setParameter("thread", Long.toString(Thread
                .currentThread().getId()));
        FormTransformer.setParameter("method", method.toLowerCase().equals(
                "post") ? "post" : "get");

        StringWriter sw = new StringWriter();
        Document formDoc = rebuildDocument(ctlForm);
        FormTransformer.transform(new DOMSource(formDoc), new StreamResult(sw));
        this.formHtml = sw.toString();

        if (!Web) {
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

        while (this.formResults == null) {
            Thread.sleep(250);
        }

        Document doc = this.formResults;
        this.formResults = null;
        return doc;
    }

    /**
     * Save a file on the local file system to the working directory for use
     * in the validation, returns the new local path to the file
     */
    public String saveFileToWorkingDir (String origFilepath) {
    	try {
    		origFilepath = URLDecoder.decode(origFilepath, "UTF-8");
	} catch (Exception e) {
            System.err.print("Error decoding path.  "
                    + e.getMessage());
            return "";
	}

	// Create the working directory	for this file
	String path = makeWorkingDir();

	// Save the file to that path
	File file = null;
	try {
		// Create new file, and get streams to each file
		File origFile = new File(origFilepath);
		file = new File(path, origFile.getName());
		file.createNewFile();

		FileInputStream in = new FileInputStream(origFile);
		FileOutputStream out = new FileOutputStream(file);

		// Transfer bytes from one file to the other
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
	} catch (Exception e) {
            System.err.print("Error creating and writing to file.  "
                    + e.getMessage());
            return "";
	}

	return file.getAbsolutePath();
    }

    /**
     * Gets the proper directory to save working files to
     */
    public String makeWorkingDir () {

	// Create the working directory	for this file
	String path = "";
        if (SessionDir != null) {
            path = SessionDir;
        } else {
            path = System.getProperty("java.io.tmpdir") + "/tecore.temp";
        }

        String randomStr = TECore.randomString(16, new Random());
        path = path + "/work/" + randomStr;
        File dir = new File(path);
        dir.mkdirs();

	return dir.getAbsolutePath();
    }

    /**
     * Converts a DOM Document/Node to a String
     *
     * @param node
     *            the node to convert
     * @return String a string representation of the DOM
     */
    public static String documentToString(Node node) {
        try {
            DOMSource domSource = new DOMSource(node);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            System.err.print("Error coverting Document to String.  "
                    + e.getMessage());
            return null;
        }
    }

    /**
     * Rebuilds an externally supplied DOM Document node using the same
     * configuration as the Saxon form transformer. This is done because it is
     * not possible for any query or transformation to manipulate multiple
     * documents unless they all belong to the same Configuration.
     *
     * @param a
     *            DOM Document the node to be rebuilt.
     * @return Document the new document with the same configuration as the
     *         transformer.
     */
    private Document rebuildDocument(Document sourceDoc) {
        Document newDoc = null;
        DocumentBuilderImpl domBuilder = new DocumentBuilderImpl();
        domBuilder.setConfiguration((Configuration) TF
                .getAttribute(FeatureKeys.CONFIGURATION));
        try {
            newDoc = domBuilder.parse(new InputSource(new StringReader(
                    documentToString(sourceDoc))));
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
        return newDoc;
    }

    // Returns a random string of length len
    public static String randomString(int len, Random random) {
        if (len < 1) {
            return "";
        }

        int start = ' ';
        int end = 'z' + 1;

        StringBuffer buffer = new StringBuffer();
        int gap = end - start;

        while (len-- != 0) {
            char ch;
            ch = (char) (random.nextInt(gap) + start);

            if (Character.isLetterOrDigit(ch)) {
                buffer.append(ch);
            } else {
                len++;
            }
        }
        return buffer.toString();
    }

}
