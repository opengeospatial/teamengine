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
package com.occamlab.te.parsers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import com.occamlab.te.ErrorHandlerImpl;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.URLConnectionUtils;

/**
 * Validates an XML resource using a set of W3C XML Schema documents.
 * 
 */
public class XMLValidatingParser {
    static SchemaFactory SF = null;
    static TransformerFactory TF = null;
    static DocumentBuilderFactory nonValidatingDBF = null;
    static DocumentBuilderFactory schemaValidatingDBF = null;
    static DocumentBuilderFactory dtdValidatingDBF = null;
    ArrayList<Object> schemaList = new ArrayList<Object>();
    ArrayList<Object> dtdList = new ArrayList<Object>();
    private static Logger jlogger = Logger
            .getLogger("com.occamlab.te.parsers.XMLValidatingParser");

    private void loadSchemaList(Document schemaLinks,
            ArrayList<Object> schemas, String schemaType) throws Exception {
        NodeList nodes = schemaLinks.getElementsByTagNameNS(
                "http://www.occamlab.com/te/parsers", schemaType);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            Object schema = null;
            String type = e.getAttribute("type");
            // URL, File, or Resource
            if (type.equals("url")) {
                schema = new URL(e.getTextContent());
            } else if (type.equals("file")) {
                schema = new File(e.getTextContent());
            } else if (type.equals("resource")) {
                ClassLoader cl = getClass().getClassLoader();
                String resource = e.getTextContent();
                URL url = cl.getResource(resource);
                if (url == null) {
                    String msg = "Can't find schema resource on classpath at "
                            + resource;
                    jlogger.warning(msg);
                    throw new Exception(msg);
                }
                schema = new File(url.getFile());
            } else {
                throw new Exception("Unknown schema resource type " + type);
            }
            jlogger.finer("Adding schema reference " + schema.toString());
            schemas.add(schema);
        }
    }

    private void loadSchemaLists(Node schemaLinks, ArrayList<Object> schemas,
            ArrayList<Object> dtds) throws Exception {
        jlogger.finer("Received schemaLinks\n"
                + DomUtils.serializeNode(schemaLinks));
        // Parse Document for schema elements
        Document d;
        if (schemaLinks instanceof Document) {
            d = (Document) schemaLinks;
        } else {
            d = schemaLinks.getOwnerDocument();
        }
        loadSchemaList(d, schemas, "schema");
        loadSchemaList(d, dtds, "dtd");
        // If instruction body is an embedded xsd:schema, add it to the
        // ArrayList
        NodeList nodes = d.getElementsByTagNameNS(
                "http://www.w3.org/2001/XMLSchema", "schema");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            CharArrayWriter caw = new CharArrayWriter();
            Transformer t = TF.newTransformer();
            t.transform(new DOMSource(e), new StreamResult(caw));
            schemas.add(caw.toCharArray());
        }
    }

    public XMLValidatingParser() {
        if (SF == null) {
            String property_name = "javax.xml.validation.SchemaFactory:"
                    + XMLConstants.W3C_XML_SCHEMA_NS_URI;
            String oldprop = System.getProperty(property_name);
            System.setProperty(property_name,
                    "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
            SF = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                SF.setFeature(
                        "http://apache.org/xml/features/validation/schema-full-checking",
                        false);
            } catch (Exception e) {
                jlogger.warning("Unable to set feature '*/schema-full-checking'");
            }
            if (oldprop == null) {
                System.clearProperty(property_name);
            } else {
                System.setProperty(property_name, oldprop);
            }
        }

        if (nonValidatingDBF == null) {
            String property_name = "javax.xml.parsers.DocumentBuilderFactory";
            String oldprop = System.getProperty(property_name);
            System.setProperty(property_name,
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            nonValidatingDBF = DocumentBuilderFactory.newInstance();
            nonValidatingDBF.setNamespaceAware(true);
            schemaValidatingDBF = DocumentBuilderFactory.newInstance();
            schemaValidatingDBF.setNamespaceAware(true);
            schemaValidatingDBF.setValidating(true);
            schemaValidatingDBF.setAttribute(
                    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            dtdValidatingDBF = DocumentBuilderFactory.newInstance();
            dtdValidatingDBF.setNamespaceAware(true);
            dtdValidatingDBF.setValidating(true);
            if (oldprop == null) {
                System.clearProperty(property_name);
            } else {
                System.setProperty(property_name, oldprop);
            }
        }

        if (TF == null) {
            TF = TransformerFactory.newInstance();
        }
    }

    public XMLValidatingParser(Document schema_links) throws Exception {
        this();
        loadSchemaLists(schema_links, schemaList, dtdList);
    }

    public Document parse(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {
        jlogger.finer("Received URLConnection object for " + uc.getURL());
        InputStream inStream = URLConnectionUtils.getInputStream(uc);
        return parse(inStream, instruction, logger);
    }

    /**
     * A method to validate a pool of schemas within the ctl:request element.
     * 
     * @param xml
     *            the xml to parse and validate. May be an InputStream object or
     *            a Document object.
     * @param instruction
     *            the xml encapsulated schema information (file locations)
     * @param logger
     *            the PrintWriter to log all results to
     * @return null if there were errors, the parse document otherwise
     * 
     * @author jparrpearson
     */
    private Document parse(Object xml, Element instruction, PrintWriter logger)
            throws Exception {
        jlogger.finer("Received XML resource of type "
                + xml.getClass().getName());
        ArrayList<Object> schemas = new ArrayList<Object>();
        ArrayList<Object> dtds = new ArrayList<Object>();
        schemas.addAll(schemaList);
        dtds.addAll(dtdList);
        loadSchemaLists(instruction, schemas, dtds);

        Document doc = null;

        ErrorHandlerImpl eh = new ErrorHandlerImpl("Parsing", logger);

        if (xml instanceof InputStream) {
            DocumentBuilderFactory dbf = nonValidatingDBF;
            // if no schemas were supplied, let the parser do the validating.
            // I.e. use the schemaLocation attribute
            if (schemas.size() == 0 && dtds.size() == 0) {
                eh.setRole("ValidatingParser");
                NodeList nl = instruction.getElementsByTagNameNS(
                        "http://www.occamlab.com/te/parsers", "schemas");
                if (nl != null && nl.getLength() > 0) {
                    dbf = schemaValidatingDBF;
                } else {
                    dbf = dtdValidatingDBF;
                }
            }
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(eh);
            InputStream xmlInput = (InputStream) xml;
            try {
                doc = db.parse(xmlInput);
            } catch (Exception e) {
                jlogger.log(Level.SEVERE, "error parsing", e);
            } finally {
                xmlInput.close();
            }
        } else if (xml instanceof Document) {
            doc = (Document) xml;
        } else {
            throw new Exception("Error: Invalid xml object");
        }

        if (doc != null) {
            eh.setRole("Validation");
            validate(doc, schemas, dtds, eh);
        }

        // Print errors
        int error_count = eh.getErrorCount();
        int warning_count = eh.getWarningCount();
        if (error_count > 0 || warning_count > 0) {
            String msg = "";
            if (error_count > 0) {
                msg += error_count + " validation error"
                        + (error_count == 1 ? "" : "s");
                if (warning_count > 0)
                    msg += " and ";
            }
            if (warning_count > 0) {
                msg += warning_count + " warning"
                        + (warning_count == 1 ? "" : "s");
            }
            msg += " detected.";
            logger.println(msg);
        }

        if (error_count > 0) {
            String s = instruction.getAttribute("ignoreErrors");
            if (s.length() == 0 || Boolean.parseBoolean(s) == false) {
                doc = null;
            }
        }

        if (warning_count > 0) {
            String s = instruction.getAttribute("ignoreWarnings");
            if (s.length() > 0 && Boolean.parseBoolean(s) == false) {
                doc = null;
            }
        }

        return doc;
    }

    /**
     * A method to validate a pool of schemas outside of the request element.
     * 
     * @param Document
     *            doc The file document to validate
     * @param Document
     *            instruction The xml encapsulated schema information (file
     *            locations)
     * @return false if there were errors, true if none
     * 
     * @author jparrpearson
     */
    public boolean checkXMLRules(Document doc, Document instruction)
            throws Exception {

        if (doc == null || doc.getDocumentElement() == null)
            return false;

        Element e = instruction.getDocumentElement();
        PrintWriter logger = new PrintWriter(System.out);
        Document parsedDoc = parse(doc, e, logger);

        return (parsedDoc != null);
    }

    public NodeList validate(Document doc, Document instruction)
            throws Exception {

        if (doc == null || doc.getDocumentElement() == null) {
            return null;
        }

        // Load the schemas declared in the XML element
        ArrayList<Object> schemas = new ArrayList<Object>();
        ArrayList<Object> dtds = new ArrayList<Object>();
        schemas.addAll(schemaList);
        dtds.addAll(dtdList);
        loadSchemaLists(instruction, schemas, dtds);

        // Create an empty list to store the errors in
        NodeList errorStrings = null;
        XmlErrorHandler eh = new XmlErrorHandler();
        validate(doc, schemas, dtds, eh);
        errorStrings = eh.toNodeList();
        return errorStrings;
    }

    private void validate(Document doc, ArrayList<Object> schemas,
            ArrayList<Object> dtds, ErrorHandler eh) throws Exception {
        jlogger.finer("Validating XML resource against " + schemas.toString());
        if (schemas.size() > 0) {
            Source[] schemaSources = new Source[schemas.size()];
            for (int i = 0; i < schemas.size(); i++) {
                Object o = schemas.get(i);
                if (o instanceof File) {
                    schemaSources[i] = new StreamSource((File) o);
                } else if (o instanceof URL) {
                    schemaSources[i] = new StreamSource(o.toString());
                } else if (o instanceof char[]) {
                    schemaSources[i] = new StreamSource(new CharArrayReader(
                            (char[]) o));
                } else {
                    throw new Exception("Illegal object in schemas list");
                }
            }
            Schema schema = SF.newSchema(schemaSources);
            Validator validator = schema.newValidator();
            validator.setErrorHandler(eh);
            validator.validate(new DOMSource(doc));
        }

        // Validate against each dtd
        for (Object dtd : dtds) {
            StringBuffer transform = new StringBuffer();
            transform
                    .append("<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">");
            transform.append("<xsl:output doctype-system=\"");
            transform.append(dtd.toString());
            transform.append("\"/>");
            transform.append("<xsl:template match=\"/\">");
            transform.append("<xsl:copy-of select=\"*\"/>");
            transform.append("</xsl:template>");
            transform.append("</xsl:transform>");
            Transformer t = TF.newTransformer(new StreamSource(
                    new CharArrayReader(transform.toString().toCharArray())));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            t.transform(new DOMSource(doc), new StreamResult(baos));
            DocumentBuilder db = dtdValidatingDBF.newDocumentBuilder();
            db.setErrorHandler(eh);
            try {
                db.parse(new ByteArrayInputStream(baos.toByteArray()));
            } catch (Exception e) {
                jlogger.log(Level.SEVERE, "validate", e);
            }
        }
    }
}
