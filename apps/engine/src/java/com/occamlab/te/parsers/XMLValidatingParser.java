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

import java.net.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.occamlab.te.ErrorHandlerImpl;

import java.util.*;

import java.io.File;
import java.io.PrintWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;

public class XMLValidatingParser {
	SchemaFactory SF;
	ArrayList Schemas = new ArrayList();

	void load_schemas(Node schema_links, ArrayList schemas) throws Exception {
		Document d = schema_links.getOwnerDocument();
		NodeList nodes = d.getElementsByTagNameNS("http://www.occamlab.com/te/parsers", "schema");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element)nodes.item(i);
			Schema schema = null;
			String type = e.getAttribute("type");
			if (type.equals("url")) {
				schema = SF.newSchema(new URL(e.getTextContent()));
			} else if (type.equals("file")) {
				schema = SF.newSchema(new File(e.getTextContent()));
			} else if (type.equals("resource")) {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				schema = SF.newSchema(new File(cl.getResource(e.getTextContent()).getFile()));
			} else {
				schema = SF.newSchema();
			}
			schemas.add(schema);
		}
	}

	// Similar to load_schemas, but in this case hold the File pointer for each schema, not the schema itself (for use in pooling schema validation)
	void loadSchemaList(Node schemaLinks, ArrayList schemas) throws Exception {
		
		// Parse Document for schema elements
		Document d = schemaLinks.getOwnerDocument();
		NodeList nodes = d.getElementsByTagNameNS("http://www.occamlab.com/te/parsers", "schema");
		
		// Add schema information to ArrayList for loading
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element)nodes.item(i);
			File schema = null;
			String type = e.getAttribute("type");
			
			// URL, File, or Resource
			if (type.equals("url")) {
				URL url = new URL(e.getTextContent());
				schema = new File(url.toURI());
			} else if (type.equals("file")) {
				schema = new File(e.getTextContent());
			} else if (type.equals("resource")) {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				schema = new File(cl.getResource(e.getTextContent()).getFile());
			} else {
				System.out.println("Incorrect schema resource file:  Unknown type!");
			}
			
			schemas.add(schema);
		}
	}

	public XMLValidatingParser() throws Exception {
		final String property_name = "javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
		String oldprop = System.getProperty(property_name);
		System.setProperty(property_name, "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		SF = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		SF.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
		if (oldprop == null) {
			System.clearProperty(property_name);
		} else {
			System.setProperty(property_name, oldprop);
		}
	}

	public XMLValidatingParser(Document schema_links) throws Exception {
		this();
		load_schemas(schema_links, Schemas);
	}

	public Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
		ArrayList schemas = new ArrayList();
		schemas.addAll(Schemas);
		load_schemas(instruction, schemas);
		String property_name = "javax.xml.parsers.DocumentBuilderFactory";
		String oldprop = System.getProperty(property_name);
		System.setProperty(property_name, "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		if (oldprop == null) {
			System.clearProperty(property_name);
		} else {
			System.setProperty(property_name, oldprop);
		}
		dbf.setNamespaceAware(true);
		ErrorHandlerImpl eh = new ErrorHandlerImpl("Parsing", logger);
		if (schemas.size() == 0) {
			eh.setRole("ValidatingParser");
			dbf.setValidating(true);
//			dbf.setFeature("http://xml.org/sax/features/validation", true);
//			dbf.setFeature("http://apache.org/xml/features/validation/schema", true);
			dbf.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
		}
		DocumentBuilder db = dbf.newDocumentBuilder();
		db.setErrorHandler(eh);

		Document doc = null;
		try {
			doc = db.parse(uc.getInputStream());
		} catch (Exception e) {
			logger.println(e.getMessage());
		}

		if (doc != null) {
			Iterator it = schemas.iterator();
			while (it.hasNext()) {
				Schema schema = (Schema)it.next();
				Validator validator = schema.newValidator();
				eh.setRole("Validation");
				validator.setErrorHandler(eh);
				validator.validate(new DOMSource(doc));
			}
		}

		int error_count = eh.getErrorCount();
		int warning_count = eh.getWarningCount();
		if (error_count > 0 || warning_count > 0) {
			String msg = "";
			if (error_count > 0) {
				msg += error_count + " validation error" + (error_count == 1 ? "" : "s");
				if (warning_count > 0) msg += " and ";
			}
			if (warning_count > 0) {
				msg += warning_count + " warning" + (warning_count == 1 ? "" : "s");
			}
			msg += " detected.";
			logger.println(msg);
		}

		//   	Element instruction_e = (Element)instruction.getFirstChild();

		boolean b_ignore_errors = false;
		String s_ignore_errors = instruction.getAttribute("ignoreErrors");
		if (s_ignore_errors.length() > 0) b_ignore_errors = Boolean.parseBoolean(s_ignore_errors);
		if (error_count > 0 && !b_ignore_errors) doc = null;

		boolean b_ignore_warnings = true;
		String s_ignore_warnings = instruction.getAttribute("ignoreWarnings");
		if (s_ignore_warnings.length() > 0) b_ignore_warnings = Boolean.parseBoolean(s_ignore_warnings);
		if (warning_count > 0 && !b_ignore_warnings) doc = null;

		return doc;
	}
	
	/**
	* A method to validate a pool of schemas outside of the request element.
	*
	* @param Document doc
	*	The file document to validate
	* @param Document instruction
	*	The xml encapsulated schema information (file locations)
	* @return false if there were errors, true if none
	*
	* @author jparrpearson
	*/
	public boolean checkXMLRules(Document doc, Document instruction) throws Exception {
		
		boolean isValid = true;
		
		// Load schemas
		ArrayList schemas = new ArrayList();
		schemas.addAll(Schemas);
		loadSchemaList(instruction.getDocumentElement(), schemas);
		
		PrintWriter logger = new PrintWriter(System.out);
		ErrorHandlerImpl eh = new ErrorHandlerImpl("Parsing", logger);

		// Validate against loaded schemas
		if (doc != null) {
			// Get all the schemas and make them into one
			Source[] schemaSources = new Source[schemas.size()];
			for (int i = 0; i < schemas.size(); i++) {
				schemaSources[i] = new StreamSource((File) schemas.get(i));
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
				msg += error_count + " validation error" + (error_count == 1 ? "" : "s");
				if (warning_count > 0) msg += " and ";
			}
			if (warning_count > 0) {
				msg += warning_count + " warning" + (warning_count == 1 ? "" : "s");
			}
			msg += " detected.";
			logger.println(msg);
		}

		//Element instruction_e = (Element)instruction.getFirstChild();

		// TEMP
		//System.out.println("Warning/error count: "+warning_count+"/"+error_count);

		// If there were errors return null, otherwise the repsonse document
		boolean b_ignore_errors = false;
		String s_ignore_errors = instruction.getDocumentElement().getAttribute("ignoreErrors");
		if (s_ignore_errors.length() > 0) b_ignore_errors = Boolean.parseBoolean(s_ignore_errors);
		if (error_count > 0 && !b_ignore_errors) isValid = false;

		boolean b_ignore_warnings = true;
		String s_ignore_warnings = instruction.getDocumentElement().getAttribute("ignoreWarnings");
		if (s_ignore_warnings.length() > 0) b_ignore_warnings = Boolean.parseBoolean(s_ignore_warnings);
		if (warning_count > 0 && !b_ignore_warnings) isValid = false;

		return isValid;
	}
}
