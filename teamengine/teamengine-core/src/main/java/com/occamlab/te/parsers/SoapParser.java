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

package com.occamlab.te.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.net.URLConnection;
import java.io.PrintWriter;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;

import com.occamlab.te.ErrorHandlerImpl;
import com.occamlab.te.util.*;
import java.net.HttpURLConnection;
import javax.xml.transform.dom.DOMSource;

/**
 * Parses a SOAP message entity and returns the SOAP message itself or the
 * content of the SOAP Body element.
 * 
 */
public class SoapParser {

    public static final String SOAP_11_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAP_12_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";

    /**
     * A method to parse and valdate the response of a SOAP server.
     * 
     * @param uc
     *            the URL Connection to be used to retrieve the SOAP message.
     * @param instruction
     *            the SOAP Parser CTL excerpt
     * @param logger
     *            the PrintWriter to log all results to
     * 
     * @return null if there were errors, the parsed document otherwise: it can
     *         be the SOAP message, the SOAP message body content or a SOAP fult
     * 
     * @author Simone Gianfranceschi
     */
    public Document parse(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {

        HttpURLConnection huc = (HttpURLConnection) uc;
        int responsecode = huc.getResponseCode();
        InputStream soapMessage = null;

        if (responsecode == 200) {
            // The response message follows in the HTTP response entity body.
            soapMessage = huc.getInputStream();
        } else if (responsecode == 202) {
            /*
             * The request has been accepted, but either (a) no response
             * envelope is provided or (b) an envelope representing information
             * related to the request is provided
             */
            soapMessage = huc.getInputStream();
        } else if (responsecode == 301 || responsecode == 302
                || responsecode == 307) {
            /*
             * The requested resource has moved. In the case of unsafe HTTP
             * method, like POST or PUT, explicit confirmation is required
             * before proceeding as follow. In the case of a safe method, like
             * GET, or if the redirection has been approved, the HTTP request
             * SHOULD be retried using the URI carried in the associated
             * Location header field as the new value for the
             * http://www.w3.org/2003/05/soap/mep/ImmediateDestination property.
             */
            soapMessage = huc.getErrorStream();
        } else if (responsecode == 303) {
            /*
             * The requested resource has moved and the HTTP request SHOULD be
             * retried using the URI carried in the associated Location header
             * field as the new value for the
             * http://www.w3.org/2003/05/soap/mep/ImmediateDestination property.
             * The value of
             * http://www.w3.org/2003/05/soap/features/web-method/Method is
             * changed to ""GET"", the value of
             * http://www.w3.org/2003/05/soap/mep /OutboundMessage is set to
             * "null". [Note: Status code 303 MUST NOT be sent unless the
             * request SOAP envelope has been processed according to the SOAP
             * processing model and the SOAP response is to be made available by
             * retrieval from the URI provided with the 303.]
             */
            soapMessage = huc.getErrorStream();
        } else if (responsecode >= 400) {
            /*
             * Client or Server errors. The SOAP Fault has to be handled.
             */
            soapMessage = huc.getErrorStream();
        }
        return this.parse(soapMessage, instruction, logger);
    }

    /**
     * A method to parse and valdate the response of a SOAP server.
     * 
     * @param xml
     *            the SOAP message to retrieve and validate. May be an
     *            InputStream object or a Document object.
     * @param instruction
     *            the SOAP Parser CTL excerpt
     * @param logger
     *            the PrintWriter to log all results to
     * 
     * @return null if there were errors, the parsed document otherwise: it can
     *         be the SOAP message, the SOAP message body content or a SOAP fult
     * 
     * @author Simone Gianfranceschi
     */
    private Document parse(Object xml, Element instruction, PrintWriter logger)
            throws Exception {
        Document soapMessage = null;
        String returnType = instruction.getAttribute("return");// envelope or
                                                               // content

        ErrorHandlerImpl eh = new ErrorHandlerImpl("Parsing", logger);

        if (xml instanceof InputStream) {
            soapMessage = SoapUtils.getSOAPMessage((InputStream) xml);
        } else if (xml instanceof Document) {
            soapMessage = (Document) xml;
        } else {
            throw new Exception("Error: Invalid xml object");
        }

        if (soapMessage != null && isSoapFault(soapMessage)) {
            return parseSoapFault(soapMessage, logger);
        }

        eh.setRole("Validation");
        this.validateSoapMessage(soapMessage, eh);

        // Print errors
        int error_count = eh.getErrorCount();
        int warning_count = eh.getWarningCount();
        if (error_count > 0 || warning_count > 0) {
            String msg = "";
            if (error_count > 0) {
                msg += error_count + " validation error"
                        + (error_count == 1 ? "" : "s");
                if (warning_count > 0) {
                    msg += " and ";
                }
            }
            if (warning_count > 0) {
                msg += warning_count + " warning"
                        + (warning_count == 1 ? "" : "s");
            }
            msg += " detected.";
            logger.println(msg);
        }

        if (error_count > 0) {
            soapMessage = null;
        }

        if (soapMessage != null && returnType.equals("content")) {
            return SoapUtils.getSoapBody(soapMessage);
        }

        return soapMessage;
    }

    /**
     * A method to validate the SOAP message received. The message is validated
     * against the propoer SOAP Schema (1.1 or 1.2 depending on the namespace of
     * the incoming message)
     * 
     * @param soapMessage
     *            the SOAP message to validate.
     * @param eh
     *            the error handler.
     * 
     * @author Simone Gianfranceschi
     */
    private void validateSoapMessage(Document soapMessage, ErrorHandler eh)
            throws Exception {
        String namespace = soapMessage.getDocumentElement().getNamespaceURI();

        if (namespace == null) {
            throw new Exception(
                    "Error: SOAP message cannot be validated. The returned response may be an HTML response: "
                            + DomUtils.serializeNode(soapMessage));
        }

        // Create SOAP validator
        SchemaFactory sf = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema soap_schema = null;
        if (namespace.equals(SOAP_12_NAMESPACE)) {
            soap_schema = sf.newSchema(Misc
                    .getResourceAsFile("com/occamlab/te/schemas/soap12.xsd"));
        } else /* if (namespace.equals(SOAP_11_NAMESPACE)) */{
            soap_schema = sf.newSchema(Misc
                    .getResourceAsFile("com/occamlab/te/schemas/soap11.xsd"));
        }

        Validator soap_validator = soap_schema.newValidator();
        soap_validator.setErrorHandler(eh);
        soap_validator.validate(new DOMSource(soapMessage));
    }

    /**
     * A method to check if the message received is a SOAP fault.
     * 
     * @param soapMessage
     *            the SOAP message to check.
     * 
     * @author Simone Gianfranceschi
     */
    private boolean isSoapFault(Document soapMessage) throws Exception {
        Element faultElement = DomUtils.getElementByTagNameNS(soapMessage,
                SOAP_12_NAMESPACE, "Fault");

        if (faultElement != null) {
            return true;
        } else {
            faultElement = DomUtils.getElementByTagNameNS(soapMessage,
                    SOAP_11_NAMESPACE, "Fault");
            if (faultElement != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * A method to parse a SOAP fault. It checks the namespace and invoke the
     * correct SOAP 1.1 or 1.2 Fault parser.
     * 
     * @param soapMessage
     *            the SOAP fault message to parse
     * 
     * @param logger
     *            the PrintWriter to log all results to
     * 
     * @return the parsed document otherwise
     * 
     * @author Simone Gianfranceschi
     */
    private Document parseSoapFault(Document soapMessage, PrintWriter logger)
            throws Exception {
        String namespace = soapMessage.getDocumentElement().getNamespaceURI();

        if (namespace.equals(SOAP_12_NAMESPACE)) {
            parseSoap12Fault(soapMessage, logger);
        } else /* if (namespace.equals(SOAP_11_NAMESPACE)) */{
            parseSoap11Fault(soapMessage, logger);
        }
        return soapMessage;
    }

    /**
     * A method to parse a SOAP 1.1 fault message.
     * 
     * @param soapMessage
     *            the SOAP 1.1 fault message to parse
     * 
     * @param logger
     *            the PrintWriter to log all results to
     * 
     * @return void
     * 
     * @author Simone Gianfranceschi
     */
    private void parseSoap11Fault(Document soapMessage, PrintWriter logger)
            throws Exception {
        Element envelope = soapMessage.getDocumentElement();
        Element element = DomUtils.getElementByTagName(envelope, "faultcode");
        if (element == null) {
            element = DomUtils.getElementByTagNameNS(envelope,
                    SOAP_11_NAMESPACE, "faultcode");
        }
        String faultcode = element.getTextContent();

        element = DomUtils.getElementByTagName(envelope, "faultstring");
        if (element == null) {
            element = DomUtils.getElementByTagNameNS(envelope,
                    SOAP_11_NAMESPACE, "faultstring");
        }

        String faultstring = element.getTextContent();

        String msg = "SOAP Fault received - [code:" + faultcode
                + "][fault string:" + faultstring + "]";
        logger.println(msg);
    }

    /**
     * A method to parse a SOAP 1.2 fault message.
     * 
     * @param soapMessage
     *            the SOAP 1.2 fault message to parse
     * 
     * @param logger
     *            the PrintWriter to log all results to
     * 
     * @return void
     * 
     * @author Simone Gianfranceschi
     */
    private void parseSoap12Fault(Document soapMessage, PrintWriter logger)
            throws Exception {
        Element envelope = soapMessage.getDocumentElement();
        Element code = DomUtils.getElementByTagNameNS(envelope,
                SOAP_12_NAMESPACE, "Code");
        String value = DomUtils.getElementByTagNameNS(code, SOAP_12_NAMESPACE,
                "Value").getTextContent();
        String msg = "SOAP Fault received - [code:" + value + "]";
        Element subCode = DomUtils.getElementByTagNameNS(code,
                SOAP_12_NAMESPACE, "Subcode");
        if (subCode != null) {
            value = DomUtils.getElementByTagNameNS(subCode, SOAP_12_NAMESPACE,
                    "Value").getTextContent();
            msg += "[subcode:" + value + "]";
        }
        Element reason = DomUtils.getElementByTagNameNS(envelope,
                SOAP_12_NAMESPACE, "Reason");
        Element text = DomUtils.getElementByTagNameNS(reason,
                SOAP_12_NAMESPACE, "Text");
        if (text != null) {
            value = text.getTextContent();
            msg += "[reason:" + value + "]";
        }

        logger.println(msg);
    }
}