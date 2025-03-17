/****************************************************************************
 *
 * The Original Code is TEAM Engine.
 *
 * The Initial Developer of the Original Code is Intecs SPA.  Portions created by
 * Intecs SPA are Copyright (C) 2008-2009, Intecs SPA. All Rights Reserved.
 *
 * Contributor(s):
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ****************************************************************************/
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

import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.XMLConstants; // Addition for Fortify modifications

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.transform.OutputKeys;

/**
 * @author Simone Gianfranceschi
 */
public class SoapUtils {

	public static final String SOAP_V_1_1 = "1.1";

	public static final String SOAP_V_1_2 = "1.2";

	/**
	 * A method to get the SOAP message from the input stream. author Simone
	 * Gianfranceschi
	 * @param in the input stream to be used to get the SOAP message.
	 * @return the SOAP message
	 *
	 */
	public static Document getSOAPMessage(InputStream in) throws Exception {
		/*
		 * ByteArrayOutputStream out = new ByteArrayOutputStream(); int b; while ((b =
		 * in.read()) != -1) { out.write(b); }
		 *
		 * System.out.println("OUT:" + out.toString());
		 */
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		// Fortify Mod: Disable entity expansion to foil External Entity Injections
		dbf.setExpandEntityReferences(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document soapMessage = db.newDocument();
		// Fortify Mod: prevent external entity injection
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer t = tf.newTransformer();
		// End Fortify Mod
		t.transform(new StreamSource(in), new DOMResult(soapMessage));
		return soapMessage;
	}

	/**
	 * A method to extract the content of the SOAP body. author Simone Gianfranceschi
	 * @param soapMessage the SOAP message.
	 * @return the content of the body of the input SOAP message.
	 *
	 */
	public static Document getSoapBody(Document soapMessage) throws Exception {
		Element envelope = soapMessage.getDocumentElement();
		Element body = DomUtils.getChildElement(DomUtils.getElementByTagName(envelope, envelope.getPrefix() + ":Body"));
		Document content = DomUtils.createDocument(body);

		Element documentRoot = content.getDocumentElement();
		addNSdeclarations(envelope, documentRoot);
		addNSdeclarations(body, documentRoot);
		return content;
	}

	/**
	 * A method to copy namespaces declarations. author Simone Gianfranceschi
	 * @param source the source message containing the namespaces to be copied on the
	 * target message.
	 * @param target the target message.
	 *
	 */
	public static void addNSdeclarations(Element source, Element target) throws Exception {
		NamedNodeMap sourceAttributes = source.getAttributes();
		Attr attribute;
		String attributeName;
		for (int i = 0; i <= sourceAttributes.getLength() - 1; i++) {
			attribute = (Attr) sourceAttributes.item(i);
			attributeName = attribute.getName();
			if (attributeName.startsWith("xmlns") && !attributeName.startsWith("xmlns:soap-env")) {
				// System.out.println("XMLNS:" +
				// attributeName+":"+attribute.getValue());
				target.setAttribute(attributeName, attribute.getValue());
			}
		}
	}

	/**
	 * A method to create a SOAP message and retrieve it as byte. author Simone
	 * Gianfranceschi
	 * @param version the SOAP version to be used (1.1 or 1.2).
	 * @param headerBlocks the list of Header Blocks to be included in the SOAP Header .
	 * @param body the XML message to be included in the SOAP BODY element.
	 * @param encoding the encoding to be used for the message creation.
	 * @return The created SOAP message as byte.
	 *
	 */
	public static byte[] getSoapMessageAsByte(String version, List headerBlocks, Element body, String encoding)
			throws Exception {
		Document message = createSoapMessage(version, headerBlocks, body);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		TransformerFactory tf = TransformerFactory.newInstance();
		// Fortify Mod: prevent external entity injection
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer t = tf.newTransformer();
		// End Fortify Mod
		t.setOutputProperty(OutputKeys.ENCODING, encoding);
		t.transform(new DOMSource(message), new StreamResult(baos));

		// System.out.println("SOAP MESSAGE : " + baos.toString());

		return baos.toByteArray();
	}

	/**
	 * A method to create a SOAP message. The SOAP message, including the Header is
	 * created and returned as DOM Document. author Simone Gianfranceschi
	 * @param version the SOAP version to be used (1.1 or 1.2).
	 * @param headerBlocks the list of Header Blocks to be included in the SOAP Header .
	 * @param body the XML message to be included in the SOAP BODY element.
	 * @return The created SOAP message as document.
	 *
	 */
	public static Document createSoapMessage(String version, List headerBlocks, Element body) throws Exception {
		Document message = null;
		NodeList children = body.getChildNodes();
		// Loop in order to remove dummy nodes (spaces, CR)
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() != Node.ELEMENT_NODE
					&& children.item(i).getNodeType() == Node.TEXT_NODE) {
				Document bodyDoc = DomUtils.convertToElementNode(body.getTextContent().trim());
				if (bodyDoc.getFirstChild().getNodeType() == Node.ELEMENT_NODE) {
					body = (Element) bodyDoc.getFirstChild();
				}
			}
			if (version.equals(SOAP_V_1_1)) {
				message = Soap11MessageBuilder.getSoapMessage(headerBlocks, body);
			}
			else {
				message = Soap12MessageBuilder.getSoapMessage(headerBlocks, body);
			}
			break;
		}
		return message;
	}

}
