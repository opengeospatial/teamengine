/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 ***
 *
 */
package com.occamlab.te.util;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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

		}
		catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * REturns a node list given a document and xpath Expression
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
			return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		}
		catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return null;
	}

}
