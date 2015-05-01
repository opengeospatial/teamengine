/**
 * 
 */
package com.occamlab.te.web;

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

}
