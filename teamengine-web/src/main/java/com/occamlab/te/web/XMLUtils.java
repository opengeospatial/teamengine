/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 ***
 *
 */
package com.occamlab.te.web;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.xerces.impl.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * @author lbermudez
 *
 */
public class XMLUtils {

	 /**
		 * Get first node on a Document doc, give a xpath Expression
		 *
		 * @param doc
		 * @param xPathExpression
		 * @return a Node or null if not found
		 */
		public static Node getFirstNode(Document doc, String xPathExpression) {
			try {

				XPathFactory xPathfactory = XPathFactory.newInstance();
				XPath xpath = xPathfactory.newXPath();
				XPathExpression expr;
				expr = xpath.compile(xPathExpression);
				NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				return nl.item(0);

			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * REturns a node list given a document and xpath Expression
		 *
		 * @param doc
		 * @param xPathExpression
		 * @return ull of expression is not found
		 */
		public static NodeList getAllNodes(Document doc, String xPathExpression) {
			try {

				XPathFactory xPathfactory = XPathFactory.newInstance();
				XPath xpath = xPathfactory.newXPath();
				XPathExpression expr;
				expr = xpath.compile(xPathExpression);
				NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				return nl;

			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

			return null;
		}
		
		/**
		 * This method is used to parse xml document and will return 
		 * document object.
		 * 
		 * @param xmlFile
		 *    Input should XML file with File object.
		 * @return doc 
		 *    Return document object.
		 */
		public static Document parseDocument(File xmlFile) {
		  try {
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    dbf.setNamespaceAware(true);
		    dbf.setExpandEntityReferences(false);
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Document doc = db.parse(xmlFile);
		    return doc;
		  } catch (Exception e) {
		    throw new RuntimeException("Failed to parse xml file: " + xmlFile
		        + " Error: " + e.getMessage());
		  }
		}
		
		/**
		 * This method is used to write the DOM object to XML file. 
		 * @param xmlFile
		 * @return
		 */
        public static void transformDocument(Document doc, File xmlFile) {
          try {
            DOMImplementationRegistry domRegistry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS lsFactory = (DOMImplementationLS) domRegistry.getDOMImplementation("LS 3.0");
            
            LSSerializer serializer = lsFactory.createLSSerializer();
            serializer.getDomConfig().setParameter(Constants.DOM_XMLDECL, Boolean.FALSE);
            serializer.getDomConfig().setParameter(Constants.DOM_FORMAT_PRETTY_PRINT, Boolean.TRUE);
            LSOutput output = lsFactory.createLSOutput();
            output.setEncoding("UTF-8");
            
            FileOutputStream os = new FileOutputStream(xmlFile, false);
            output.setByteStream(os);
            serializer.write(doc, output);
            os.close();
          } catch (Exception e) {
            throw new RuntimeException("Failed to update user details. " + e.getMessage());
          }
        }
        
        /**
         * This method removes the element from the document.
         * 
         * @param doc
         * @param element 
         *      Object of root element
         * @param elementName
         *      The name of element to remove.
         * @return
         */
        public static Document removeElement(Document doc, Element element, String elementName){
          NodeList elementList = element.getElementsByTagName(elementName);
          if (elementList.getLength() != 0) {
            Element elementToRemove = (Element) doc.getElementsByTagName(elementName)
                .item(0);
            Node parent = elementToRemove.getParentNode();
            parent.removeChild(elementToRemove);
          }
          return doc;
        }

}
