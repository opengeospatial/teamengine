/**
 * 
 */
package com.occamlab.te.web;

import java.io.File;

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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		 * The XML file is uploaded 
		 * @param xmlFile
		 * @return
		 */
        public static void transformDocument(Document doc, File xmlFile) {
          try {
            DOMSource source = new DOMSource(doc);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(xmlFile);
            transformer.transform(source, result);
          } catch (Exception e) {
            throw new RuntimeException("Failed to update xml file. " + e.getMessage());
          }
        }

}
