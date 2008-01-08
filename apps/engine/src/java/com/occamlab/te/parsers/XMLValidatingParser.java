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

import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.net.URLConnection;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.NodeList;

import com.occamlab.te.ErrorHandlerImpl;

/**
 * Validates an XML resource using a set of W3C XML Schema documents.
 *
 */
public class XMLValidatingParser {
    SchemaFactory SF;

    ArrayList<Object> schemaList = new ArrayList<Object>();

    /**
     * Holds a File or URL object for each schema, used to create a collection of schemas to validate with
     */
    void loadSchemaList(Node schemaLinks, ArrayList<Object> schemas)
            throws Exception {

        // Parse Document for schema elements
        Document d = schemaLinks.getOwnerDocument();

        // If instruction body is in ctlp:schemas form, add each schema to the ArrayList
        NodeList nodes = d.getElementsByTagNameNS(
                "http://www.occamlab.com/te/parsers", "schema");
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
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                schema = new File(cl.getResource(e.getTextContent()).getFile());
            } else {
                System.out.println("Incorrect schema resource:  Unknown type!");
            }

            schemas.add(schema);
        }

        // If instruction body is an embedded xsd:schema, add it to the ArrayList
        nodes = d.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element)nodes.item(i);
            CharArrayWriter caw = new CharArrayWriter();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(e), new StreamResult(caw));
            schemas.add(caw.toCharArray());
        }
    }

    public XMLValidatingParser() throws Exception {
        final String property_name = "javax.xml.validation.SchemaFactory:"
                + XMLConstants.W3C_XML_SCHEMA_NS_URI;
        String oldprop = System.getProperty(property_name);
        System.setProperty(property_name,
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        SF = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        SF.setFeature(
                        "http://apache.org/xml/features/validation/schema-full-checking",
                        false);
        if (oldprop == null) {
            System.clearProperty(property_name);
        } else {
            System.setProperty(property_name, oldprop);
        }
    }

    public XMLValidatingParser(Document schema_links) throws Exception {
        this();
        loadSchemaList(schema_links, schemaList);
    }

    public Document parse(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {
        return parse(uc.getInputStream(), instruction, logger);
    }
/*
    public Document parse(HttpResponse resp, Element instruction,
            PrintWriter logger) throws Exception {
        return parse(resp.getEntity().getContent(), instruction, logger);
    }
*/
    /**
     * A method to validate a pool of schemas within the ctl:request element.
     *
     * @param xml
     *            the xml to parse and validate.  May be an InputStream object
     *            or a Document object.
     * @param instruction
     *            the xml encapsulated schema information (file locations)
     * @param logger
     *            the PrintWriter to log all results to
     * @return null if there were errors, the parse document otherwise
     *
     * @author jparrpearson
     */
    private Document parse(Object xml, Element instruction,
            PrintWriter logger) throws Exception {

        ArrayList<Object> schemas = new ArrayList<Object>();
        schemas.addAll(schemaList);
        loadSchemaList(instruction, schemas);

        Document doc = null;

        ErrorHandlerImpl eh = new ErrorHandlerImpl("Parsing", logger);

        if (xml instanceof InputStream) {
            String property_name = "javax.xml.parsers.DocumentBuilderFactory";
            String oldprop = System.getProperty(property_name);
            System.setProperty(property_name,
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            if (oldprop == null) {
                System.clearProperty(property_name);
            } else {
                System.setProperty(property_name, oldprop);
            }

            dbf.setNamespaceAware(true);

            // if no schemas were supplied, let the parser do the validating.
            // I.e. use the schemaLocation attribute
            if (schemas.size() == 0) {
                eh.setRole("ValidatingParser");
                dbf.setValidating(true);
                dbf.setAttribute(
                        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                        "http://www.w3.org/2001/XMLSchema");
            }

            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(eh);

            try {
                doc = db.parse((InputStream)xml);
            } catch (Exception e) {
                logger.println(e.getMessage());
            }
        } else if (xml instanceof Document) {
            doc = (Document)xml;
        } else {
            throw new Exception("Error: Invalid xml object");
        }

        // Validate against loaded schemas
        if (doc != null && schemas.size() > 0) {
            // Get all the schemas and make them into one
            Source[] schemaSources = new Source[schemas.size()];
            for (int i = 0; i < schemas.size(); i++) {
                Object o = schemas.get(i);
                if (o instanceof File) {
                    schemaSources[i] = new StreamSource((File) o);
                } else if (o instanceof URL) {
                    schemaSources[i] = new StreamSource(o.toString());
                } else if (o instanceof char[]) {
                    schemaSources[i] = new StreamSource(new CharArrayReader((char[])o));
                } else {
                    throw new Exception("Illegal object in schemas list");
                }
            }
            Schema schema = SF.newSchema(schemaSources);
            // Validate with the combined schema
            Validator validator = schema.newValidator();
            eh.setRole("Validation");
            validator.setErrorHandler(eh);
            validator.validate(new DOMSource(doc));
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

	if (doc == null || doc.getDocumentElement() == null) return false;

        Element e = instruction.getDocumentElement();
        PrintWriter logger = new PrintWriter(System.out);
        Document parsedDoc = parse(doc, e, logger);

        return (parsedDoc != null);
    }

    public NodeList validate(Document doc, Document instruction)
            throws Exception {

	if (doc == null || doc.getDocumentElement() == null) return null;

	// Create an empty list to store the errors in
	NodeList errorStrings = null;
	XmlErrorHandler eh = new XmlErrorHandler();

	PrintWriter logger = new PrintWriter(System.out);
        Element e = instruction.getDocumentElement();

	// Load the schemas declared in the XML element
        ArrayList<Object> schemas = new ArrayList<Object>();
        schemas.addAll(schemaList);
        loadSchemaList(e, schemas);

        // Validate against loaded schemas
        if (doc != null && schemas.size() > 0) {
            // Get all the schemas and make them into one
            Source[] schemaSources = new Source[schemas.size()];
            for (int i = 0; i < schemas.size(); i++) {
                Object o = schemas.get(i);
                if (o instanceof File) {
                    schemaSources[i] = new StreamSource((File) o);
                } else if (o instanceof URL) {
                    schemaSources[i] = new StreamSource(o.toString());
                } else if (o instanceof char[]) {
                    schemaSources[i] = new StreamSource(new CharArrayReader((char[])o));
                } else {
                    throw new Exception("Illegal object in schemas list");
                }
            }
            Schema schema = SF.newSchema(schemaSources);
            // Validate with the combined schema
            Validator validator = schema.newValidator();
            validator.setErrorHandler(eh);
            validator.validate(new DOMSource(doc));
            errorStrings = eh.toNodeList();
        }

        // Print error summary
	/*boolean isEmpty = eh.isEmpty();
	if (!isEmpty) {
		int error_count = errorStrings.getLength();
		logger.println(error_count +  " validation error" + (error_count == 1 ? "" : "s") + " detected.");
		logger.flush();
	}*/

        return errorStrings;
    }

}
