/****************************************************************************
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
the specific language governing rights and limitations under the License.

The Original Code is TEAM Engine.

The Initial Developer of the Original Code is Intecs SPA.  Portions created by
Intecs SPA are Copyright (C) 2008-2009, Intecs SPA. All Rights Reserved.

Contributor(s): No additional contributors to date
 ****************************************************************************/
package com.occamlab.te.util;

import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayOutputStream;

/**
 * 
 * @author simone
 */
public class Soap11MessageBuilder {

    public static final String SOAP_ENVELOPE = "Envelope";
    public static final String SOAP_HEADER = "Header";
    public static final String SOAP_BODY = "Body";
    public static final String SOAP_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP_ADDRESSING_URI = "http://schemas.xmlsoap.org/ws/2003/03/addressing";
    private static final String XMLNS = "xmlns";
    private static final String SOAP_ENV = "soap-env";
    public static final String WSA_URI = "http://schemas.xmlsoap.org/ws/2003/03/addressing";
    public static final String SCHEMA_INSTANCE_URI = "www.w3.org/2001/XMLSchema-instance";
    public static final String SCHEMA_URI = "www.w3.org/2001/XMLSchema";
    public static final String XSD = "xsd";
    public static final String XSI = "xsi";
    public static final String MUST_UNDERSTAND = "mustUnderstand";
    public static final String ZERO = "0";
    public static final String TYPE = "type";
    public static final String STRING = "string";
    public static final String BLOCK = "block";
    public static final String ROLE = "role";
    public static final String ACTOR = "actor";
    public static final String ROLE_NEXT_SC = "next";
    public static final String ROLE_NONE_SC = "none";
    public static final String ROLE_ULTIMATE_RECEIVER_SC = "ultimateReceiver";
    public static final String ROLE_NEXT = "http://schemas.xmlsoap.org/soap/actor/next";
    public static final String TRUE_SC = "true";
    public static final String TRUE = "1";
    public static final String FALSE = "0";

    /**
     * A method to get the SOAP 1.1 message. The message is created and returned
     * as DOM Document.
     * 
     * @param headerBlocks
     *            The list of header blocks to be included in the SOAP message
     *            header
     * 
     * @param i_body
     *            The XML file to be inclued in the SOAP message body.
     * 
     * @return the DOM document representing the SOAP message
     * 
     * @author Simone Gianfranceschi
     */
    public static Document getSoapMessage(List headerBlocks, Element i_body)
            throws Exception {
        Document soapDocument;
        Element envelope;
        Element header;
        Element body;
        Node importedNode;

        soapDocument = DomUtils.createDocument(null);

        // creating envelope node
        envelope = soapDocument.createElementNS(SOAP_NS_URI, SOAP_ENV + ":"
                + SOAP_ENVELOPE);
        // envelope.setAttribute(XMLNS + ":" + SOAP_ENV, SOAP_NS_URI);
        soapDocument.appendChild(envelope);

        // Add the header if the tag is not empty
        NodeList children = null;
        //
        if (headerBlocks.size() > 0) {
            // creating header
            header = soapDocument.createElementNS(SOAP_NS_URI, SOAP_ENV + ":"
                    + SOAP_HEADER);
            envelope.appendChild(header);
            for (int j = 0; j < headerBlocks.size(); j++) {
                NamedNodeMap blockAttributes = ((Node) headerBlocks.get(j))
                        .getAttributes();
                Attr attribute;
                String attributeName;
                children = ((Node) headerBlocks.get(j)).getChildNodes();
                if (children.getLength() > 0) {
                    // Loop on the header elements
                    for (int i = 0; i < children.getLength(); i++) {
                        if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            importedNode = soapDocument.importNode(
                                    children.item(i), true);
                            // Add the SOAP attributes
                            for (int k = 0; k <= blockAttributes.getLength() - 1; k++) {
                                attribute = (Attr) blockAttributes.item(k);
                                attributeName = attribute.getName();
                                if (attributeName.equals(ROLE)) {
                                    String value = attribute.getValue();
                                    // Add the mapping
                                    if (value.equals(ROLE_NEXT_SC)) {
                                        // ((Element)
                                        // importedNode).setAttribute(SOAP_ENV +
                                        // ":" + ROLE, ROLE_NEXT);
                                        ((Element) importedNode)
                                                .setAttributeNS(SOAP_NS_URI,
                                                        SOAP_ENV + ":" + ROLE,
                                                        ROLE_NEXT);

                                    } else if (value.equals(ROLE_NONE_SC)
                                            || value.equals(ROLE_ULTIMATE_RECEIVER_SC)) {
                                        // DO NOTHING
                                    } else {
                                        // ((Element)
                                        // importedNode).setAttribute(SOAP_ENV +
                                        // ":" + attributeName, value);
                                        ((Element) importedNode)
                                                .setAttributeNS(
                                                        SOAP_NS_URI,
                                                        SOAP_ENV + ":"
                                                                + attributeName,
                                                        value);

                                    }
                                } else if (attributeName
                                        .equals(MUST_UNDERSTAND)) {
                                    if (attribute.getValue().equals(TRUE_SC)) {
                                        // ((Element)
                                        // importedNode).setAttribute(SOAP_ENV +
                                        // ":" + MUST_UNDERSTAND, TRUE);
                                        ((Element) importedNode)
                                                .setAttributeNS(
                                                        SOAP_NS_URI,
                                                        SOAP_ENV
                                                                + ":"
                                                                + MUST_UNDERSTAND,
                                                        TRUE);

                                    } else {
                                        // ((Element)
                                        // importedNode).setAttribute(SOAP_ENV +
                                        // ":" + MUST_UNDERSTAND, FALSE);
                                        ((Element) importedNode)
                                                .setAttributeNS(
                                                        SOAP_NS_URI,
                                                        SOAP_ENV
                                                                + ":"
                                                                + MUST_UNDERSTAND,
                                                        FALSE);
                                    }
                                    // ((Element)
                                    // importedNode).setAttribute(SOAP_ENV + ":"
                                    // + attributeName, attribute.getValue());
                                }
                            }
                            header.appendChild(importedNode);
                        }
                    }
                }
            }
        }
        // Add the body element
        body = soapDocument.createElementNS(SOAP_NS_URI, SOAP_ENV + ":"
                + SOAP_BODY);
        envelope.appendChild(body);

        // Import the XML message to be included in the body
        children = i_body.getChildNodes();
        if (children.getLength() > 0) {
            // Loop on the header elements
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Document newSoapDocument = DomUtils
                            .createDocument((Element) children.item(i));
                    importedNode = soapDocument.importNode(
                            newSoapDocument.getDocumentElement(), true);
                    body.appendChild(importedNode);
                }
            }
        }
        return soapDocument;
    }
}
