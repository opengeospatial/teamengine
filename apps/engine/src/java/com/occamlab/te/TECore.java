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

import java.net.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

import java.io.*;
import java.lang.reflect.*;
import net.sf.saxon.FeatureKeys;
import java.util.*;

public class TECore {
  static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
	
  DocumentBuilderFactory DBF;
  DocumentBuilder DB;
  TransformerFactory TF = TransformerFactory.newInstance();
  Transformer FormTransformer;
  PrintStream Out;
  String FormHtml;
  Document FormResults;
  boolean Web;
  public HashMap ParserInstances = new HashMap();
  public HashMap ParserMethods = new HashMap();
  
  Stack Loggers = new Stack();

  public TECore(PrintStream out, boolean web) throws Exception {
    DBF = DocumentBuilderFactory.newInstance();
    DB = DBF.newDocumentBuilder();
    TF = TransformerFactory.newInstance();
    TF.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
    Out = out;
    Web = web;
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    InputStream is = cl.getResourceAsStream("com/occamlab/te/formfn.xsl");
    StreamSource xsl_source = new StreamSource(is);
    FormTransformer = TF.newTransformer(xsl_source);
	}
  
  public String getFormHtml() {
    return FormHtml;
  }
  
  public void setFormHtml(String html) {
    FormHtml = html;
  }

  public Document getFormResults() {
    return FormResults;
  }
  
  public void setFormResults(Document doc) {
    FormResults = doc;
  }

  public static short node_type(Node node) {
		return node.getNodeType();
  }
  
  public static void exception(String message) throws Exception {
  	throw new Exception(message);
  }

  public static Document read_log(String logdir, String callpath) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
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
        buf [l] = '\n';
        buf [l+1] = '<';
        buf [l+2] = '/';
        buf [l+3] = 'l';
        buf [l+4] = 'o';
        buf [l+5] = 'g';
        buf [l+6] = '>';
        buf [l+7] = '\n';
        doc = db.newDocument();
        tf.newTransformer().transform(new StreamSource(new ByteArrayInputStream(buf)), new DOMResult(doc));
      }
    } else {
      Node root = doc.createElement("log");
      doc.appendChild(root);
    }
    return doc;
  }
  
  public Node create_log(String logdir, String callpath) throws Exception {
    PrintWriter logger = null;
    if (logdir.length() > 0) {
      File dir = new File(logdir, callpath);
      dir.mkdir();
      File f = new File(dir, "log.xml");
      f.delete();
  	  logger = new PrintWriter(new BufferedWriter(new FileWriter(f)));
  	  logger.println("<log>");
    }
    Loggers.push(logger);
  	return null;
  }

  public Node log_xml(Node xml) throws Exception {
  	PrintWriter logger = (PrintWriter)Loggers.peek();
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

  public Node close_log() throws Exception {
  	PrintWriter logger = (PrintWriter)Loggers.pop();
   	if (logger != null) {
    	logger.println("</log>");
    	logger.close();
    }
    return null;
  }
  
  public PrintWriter getLogger() {
  	if (Loggers.empty()) {
  		return null;
  	} else {
    	return (PrintWriter)Loggers.peek();
  	}
  }

  public static Node reset_log(String logdir, String callpath) throws Exception {
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

  public static URLConnection build_request(Node xml) throws Exception {
    Node body = null;
    String sUrl = null;
    String sParams = "";
    String method = "GET";

    NodeList nl = xml.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node n = (Node) nl.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        if (n.getLocalName().equals("url")) {
          sUrl = n.getTextContent();
        }
        else if (n.getLocalName().equals("method")) {
        	method = n.getTextContent().toUpperCase();
        }
        else if (n.getLocalName().equals("param")) {
          if (sParams.length() > 0) sParams += "&";
          sParams += ((Element)n).getAttribute("name") + "=" + n.getTextContent();
        }
        else if (n.getLocalName().equals("body")) {
          body = n;
        }
      }
    }
    
    if (method.equals("GET") && sParams.length() > 0) {
      if (sUrl.indexOf("?") < 0) {
        sUrl += "?";
      } else if (!sUrl.endsWith("&")) {
        sUrl += "&";
      }
      sUrl += sParams;
    }
    
//System.out.println(sUrl);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();

    URLConnection uc = new URL(sUrl).openConnection();
    if (uc instanceof HttpURLConnection) {
    	((HttpURLConnection)uc).setRequestMethod(method);
    }
    if (method.equals("POST") || method.equals("PUT")) {
      uc.setDoOutput(true);
      byte[] bytes = null;
      String mime = null;
      if (body == null) {
        bytes = sParams.getBytes();
        mime = "application/x-www-form-urlencoded";
      } else {
        NodeList children = body.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
          if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            t.transform(new DOMSource(children.item(i)), new StreamResult(baos));
            bytes = baos.toByteArray(); 
            mime = "text/xml";
            break;
          }
        }
        if (bytes == null) {
          bytes = body.getTextContent().getBytes();
          mime = "text/plain";
        }
      }
      uc.setRequestProperty("Content-Type", mime);
      uc.setRequestProperty("Content-Length", Integer.toString(bytes.length));
      OutputStream os = uc.getOutputStream();
      os.write(bytes);
    }
    
    return uc;
  }

  public Document serialize_and_parse(Node parse_instruction) throws Throwable {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = null;
    Node content = null;
    Document parser_instruction = null;
    
    NodeList children = parse_instruction.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element)children.item(i);
        if (e.getNamespaceURI().equals(XSL_NS) && e.getLocalName().equals("output")) {
          Document doc = db.newDocument();
          Element transform = doc.createElementNS(XSL_NS, "transform");
          transform.setAttribute("version", "1.0");
          doc.appendChild(transform);
          Element output = doc.createElementNS(XSL_NS, "output");
          NamedNodeMap atts = e.getAttributes();
          for (int j = 0; j < atts.getLength(); j++) {
            Attr a = (Attr)atts.item(i);
            output.setAttribute(a.getName(), a.getValue());
          }
          transform.appendChild(output);
          Element template = doc.createElementNS(XSL_NS, "template");
          template.setAttribute("match", "node()|@*");
          transform.appendChild(template);
          Element copy = doc.createElementNS(XSL_NS, "copy");
          template.appendChild(copy);
          Element apply = doc.createElementNS(XSL_NS, "apply-templates");
          apply.setAttribute("select", "node()|@*");
          copy.appendChild(apply);
          t = tf.newTransformer(new DOMSource(doc));
        } else if (e.getLocalName().equals("content")) {
          content = e;
        } else {
          parser_instruction = db.newDocument();
          tf.newTransformer().transform(new DOMSource(e), new DOMResult(parser_instruction));
        }
      }
    }
    if (t == null) {
      t = tf.newTransformer();
    }
    File temp = File.createTempFile("$te_", ".xml");
    if (content.getNodeType() == Node.TEXT_NODE) {
      RandomAccessFile raf = new RandomAccessFile(temp, "w");
      raf.writeBytes(((Text)temp).getTextContent());
      raf.close();
    } else {
      t.transform(new DOMSource((Node)content), new StreamResult(temp));
    }
    URLConnection uc = temp.toURL().openConnection();
    Document doc = parse(uc, null, parser_instruction);
    temp.delete();
    return doc;
  }
  
  public Node register_parser(String namespace_uri, String local_name, String method_name, Object parser) throws Exception {
    String key = namespace_uri + "," + local_name;
//System.out.println("Registerd " + key);
    if (parser instanceof String || parser instanceof Node) {
      ParserInstances.put(key, null);
    } else {
      ParserInstances.put(key, parser);
    }
    ParserMethods.put(key, get_parser_method(method_name, parser));
    return null;
  }
  
  public static Method get_parser_method(String method_name, Object parser) throws Exception {
  	Class parser_class;
  	if (parser instanceof String) {
  		parser_class = Class.forName((String)parser);
  	} else if (parser instanceof Node) {
  		parser_class = Class.forName(((Node)parser).getTextContent());
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
    } catch(java.lang.NoSuchMethodException e) {
      Class[] types = new Class[3];
      types[0] = URLConnection.class;
      types[1] = Element.class;
      types[2] = PrintWriter.class;
      method = parser_class.getMethod(method_name, types);
    }
  	return method;
  }

  public Document parse(URLConnection uc, String response_id, Node instruction) throws Throwable {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document response_doc = db.newDocument();
    Element response_e = response_doc.createElement("response");
    if (response_id != null) {
      response_e.setAttribute("id", response_id);
    }
    Element content_e = response_doc.createElement("content");
    if (instruction == null) {
      TransformerFactory.newInstance().newTransformer().transform(new StreamSource(uc.getInputStream()), new DOMResult(content_e));
    } else {
      Element instruction_e;
      if (instruction instanceof Element) {
        instruction_e = (Element)instruction;
      } else {
        instruction_e = ((Document)instruction).getDocumentElement();
      }
      String key = instruction_e.getNamespaceURI() + "," + instruction_e.getLocalName();
      Object instance = ParserInstances.get(key);
      Method method = (Method)ParserMethods.get(key);;
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
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(new DOMSource((Node)return_object), new DOMResult(content_e));
      } else if (return_object != null) {
        content_e.appendChild(response_doc.createTextNode(return_object.toString()));
      }
//System.out.println(content_e.getTextContent());

      Element parser_e = response_doc.createElement("parser");
      parser_e.setAttribute("prefix", instruction_e.getPrefix());
      parser_e.setAttribute("local-name", instruction_e.getLocalName());
      parser_e.setAttribute("namespace-uri", instruction_e.getNamespaceURI());
      parser_e.setTextContent(swLogger.toString());
      response_e.appendChild(parser_e);
    }
    response_e.appendChild(content_e);
    response_doc.appendChild(response_e);
    return response_doc;   
  }

  public Document parse(URLConnection uc, String response_id) throws Throwable {
    return parse(uc, response_id, null);
  }

  public Node message(int depth, String message) {
    String indent = "";
    for (int i = 0; i < depth; i++) {
    	indent += "  ";
    }
    String formatted_message = indent + message.trim().replaceAll("\n", "\n" + indent);
    synchronized(Out) {
      Out.println(formatted_message);
    }
    return null;
  }

  public Node copy(Node node) throws Exception {
    if (node instanceof Text) {
      synchronized(Out) {
        Out.println(node.getTextContent());
      }
    } else {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer t = tf.newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      synchronized(Out) {
        t.transform(new DOMSource(node), new StreamResult(Out));
        Out.println("");
      }
    }
    return null;
  }

  public Document form(Document xhtml) throws Exception {
    String name = Thread.currentThread().getName();
    NamedNodeMap attrs = xhtml.getElementsByTagName("form").item(0).getAttributes();
    Attr attr = (Attr)attrs.getNamedItem("name"); 
    if (attr != null) name = attr.getValue();
    
    FormTransformer.setParameter("title", name);
    FormTransformer.setParameter("web", Web ? "yes" : "no");
    FormTransformer.setParameter("thread", Long.toString(Thread.currentThread().getId()));
    
    //FormTransformer.transform(new DOMSource(xhtml), new StreamResult(System.out));
    StringWriter sw = new StringWriter();
    FormTransformer.transform(new DOMSource(xhtml), new StreamResult(sw));
    FormHtml = sw.toString();

    if (!Web) {
      int width = 700;
      int height = 500;
      attr = (Attr)attrs.getNamedItem("width"); 
      if (attr != null) width = Integer.parseInt(attr.getValue());
      attr = (Attr)attrs.getNamedItem("height"); 
      if (attr != null) height = Integer.parseInt(attr.getValue());
      new SwingForm(name, width, height, this);
    }
    
    while (FormResults == null) {
      Thread.sleep(250);
    }
    
    Document doc = FormResults;
    FormResults = null;
    return doc;
  }
}
