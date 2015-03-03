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
import java.io.IOException;
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
import org.xml.sax.SAXException;

import com.occamlab.te.ErrorHandlerImpl;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.URLConnectionUtils;

/**
 * Validates an XML resource against a set of W3C XML Schema or DTD documents.
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
        if (nodes.getLength() == 0) {
            return;
        }
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
        if (null == schemaLinks) {
            return;
        }
        jlogger.finer("Received schemaLinks\n"
                + DomUtils.serializeNode(schemaLinks));
        Document configDoc;
        if (schemaLinks instanceof Document) {
            configDoc = (Document) schemaLinks;
        } else {
            configDoc = schemaLinks.getOwnerDocument();
        }
        loadSchemaList(configDoc, schemas, "schema");
        loadSchemaList(configDoc, dtds, "dtd");
        // If instruction body is an embedded xsd:schema, add it to the
        // ArrayList
        NodeList nodes = configDoc.getElementsByTagNameNS(
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
        if (null != schema_links) {
            loadSchemaLists(schema_links, this.schemaList, this.dtdList);
        }
    }

    public Document parse(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {
        jlogger.finer("Received URLConnection object for " + uc.getURL());
        InputStream inStream = URLConnectionUtils.getInputStream(uc);
        return parse(inStream, instruction, logger);
    }

    /**
     * Parses and validates an XML resource using the given schema references.
     * 
     * @param input
     *            The XML input to parse and validate. It must be either an
     *            InputStream or a Document object.
     * @param parserConfig
     *            An Element
     *            ({http://www.occamlab.com/te/parsers}XMLValidatingParser)
     *            containing configuration info. If it is {@code null} or empty
     *            validation will be performed by using location hints in the
     *            input document.
     * @param logger
     *            The PrintWriter to log all results to
     * @return {@code null} If any non-ignorable errors or warnings occurred;
     *         otherwise the resulting Document.
     * 
     */
    Document parse(Object input, Element parserConfig, PrintWriter logger)
            throws Exception {
        jlogger.finer("Received XML resource of type "
                + input.getClass().getName());
        ArrayList<Object> schemas = new ArrayList<Object>();
        ArrayList<Object> dtds = new ArrayList<Object>();
        schemas.addAll(this.schemaList);
        dtds.addAll(this.dtdList);
        loadSchemaLists(parserConfig, schemas, dtds);
        Document result = null;
        ErrorHandlerImpl errHandler = new ErrorHandlerImpl("Parsing", logger);

        if (input instanceof InputStream) {
            DocumentBuilderFactory dbf = nonValidatingDBF;
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(errHandler);
            InputStream xmlInput = (InputStream) input;
            try {
                result = db.parse(xmlInput);
            } catch (Exception e) {
                jlogger.log(Level.SEVERE, "error parsing", e);
            } finally {
                xmlInput.close();
            }
        } else if (input instanceof Document) {
            result = (Document) input;
        } else {
            throw new IllegalArgumentException(
                    "XML input must be an InputStream or a Document object.");
        }

        if (result != null) {
            errHandler.setRole("Validation");
            validateAgainstXMLSchemaList(result, schemas, errHandler);
            validateAgainstDTDList(result, dtds, errHandler);
        }

        int error_count = errHandler.getErrorCount();
        int warning_count = errHandler.getWarningCount();
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
            String s = (null != parserConfig) ? parserConfig
                    .getAttribute("ignoreErrors") : "false";
            if (s.length() == 0 || Boolean.parseBoolean(s) == false) {
                result = null;
            }
        }

        if (warning_count > 0) {
            String s = (null != parserConfig) ? parserConfig
                    .getAttribute("ignoreWarnings") : "true";
            if (s.length() > 0 && Boolean.parseBoolean(s) == false) {
                result = null;
            }
        }
        return result;
    }

    /**
     * A method to validate a pool of schemas outside of the request element.
     * 
     * @param Document
     *            doc The file document to validate
     * @param Document
     *            instruction The xml encapsulated schema information (file
     *            locations)
     * @return false if there were errors, true if none.
     * 
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

    private XmlErrorHandler processValidation(Document doc, Document instruction)
            throws Exception {
        if (doc == null || doc.getDocumentElement() == null) {
            throw new NullPointerException("Input document is null.");
        }
        ArrayList<Object> schemas = new ArrayList<Object>();
        ArrayList<Object> dtds = new ArrayList<Object>();
        schemas.addAll(schemaList);
        dtds.addAll(dtdList);
        loadSchemaLists(instruction, schemas, dtds);

        XmlErrorHandler err = new XmlErrorHandler();
        validateAgainstXMLSchemaList(doc, schemas, err);
        validateAgainstDTDList(doc, dtds, err);
        return err;
    }
    
    public NodeList validate(Document doc, Document instruction)
            throws Exception {
        return processValidation(doc, instruction).toNodeList();
    }

    public Element validateSingleResult(Document doc, Document instruction)
            throws Exception {
        return processValidation(doc, instruction).toRootElement();
    }

    /**
     * Validates an XML resource against a list of XML Schemas. Validation
     * errors are reported to the given handler.
     * 
     * @param doc
     *            The input Document node.
     * @param xsdList
     *            A list of XML schema references. If the list is {@code null}
     *            or empty, validation will be performed by using location hints
     *            found in the input document.
     * @param errHandler
     *            An ErrorHandler that collects validation errors.
     * @throws SAXException
     *             If a schema cannot be read for some reason.
     * @throws IOException
     *             If an I/O error occurs.
     */
    void validateAgainstXMLSchemaList(Document doc, ArrayList<Object> xsdList,
            ErrorHandler errHandler) throws SAXException, IOException {
        jlogger.finer("Validating XML resource from " + doc.getDocumentURI());
        Schema schema = SF.newSchema();
        if (null != xsdList && !xsdList.isEmpty()) {
            Source[] schemaSources = new Source[xsdList.size()];
            for (int i = 0; i < xsdList.size(); i++) {
                Object ref = xsdList.get(i);
                if (ref instanceof File) {
                    schemaSources[i] = new StreamSource((File) ref);
                } else if (ref instanceof URL) {
                    schemaSources[i] = new StreamSource(ref.toString());
                } else if (ref instanceof char[]) {
                    schemaSources[i] = new StreamSource(new CharArrayReader(
                            (char[]) ref));
                } else {
                    throw new IllegalArgumentException(
                            "Unknown schema reference: " + ref.toString());
                }
            }
            schema = SF.newSchema(schemaSources);
        }
        Validator validator = schema.newValidator();
        validator.setErrorHandler(errHandler);
        DOMSource source = new DOMSource(doc, doc.getBaseURI());
        validator.validate(source);
    }

    /**
     * Validates an XML resource against a list of DTD schemas. Validation
     * errors are reported to the given handler.
     * 
     * @param doc
     *            The input Document.
     * @param dtdList
     *            A list of DTD schema references.
     * @param errHandler
     *            An ErrorHandler that collects validation errors.
     * @throws Exception
     */
    void validateAgainstDTDList(Document doc, ArrayList<Object> dtdList,
            ErrorHandler errHandler) throws Exception {
        jlogger.finer("Validating XML resource from " + doc.getDocumentURI());
        for (Object dtd : dtdList) {
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
            db.setErrorHandler(errHandler);
            try {
                db.parse(new ByteArrayInputStream(baos.toByteArray()));
            } catch (Exception e) {
                jlogger.log(Level.SEVERE, "validate", e);
            }
        }
    }
}
