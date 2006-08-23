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

import java.io.PrintWriter;
import java.net.*;
import org.w3c.dom.*;

import com.occamlab.te.ErrorHandlerImpl;

import java.util.*;
import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
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
	
	public XMLValidatingParser() throws Exception {
		final String property_name = "javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
		String oldprop = System.getProperty(property_name);
	  System.setProperty(property_name, "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
	  SF = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
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
	  DocumentBuilder db = dbf.newDocumentBuilder();
	  ErrorHandlerImpl eh = new ErrorHandlerImpl("Parsing", logger);
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
   	if (error_count > 0 && !b_ignore_errors) return null;

   	boolean b_ignore_warnings = true;
   	String s_ignore_warnings = instruction.getAttribute("ignoreWarnings");
   	if (s_ignore_warnings.length() > 0) b_ignore_warnings = Boolean.parseBoolean(s_ignore_warnings);  
   	if (warning_count > 0 && !b_ignore_warnings) return null;

  	return doc;
  }
}
