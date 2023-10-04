/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * *********************************************************************************
 *
 * Version Date January 6, 2018
 *
 * Contributor(s):
 *     C. Heazel (WiSC) MOdifications to address Fortify issues
 *
 * *********************************************************************************
 */

package com.occamlab.te.spi.jaxrs.resources;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import com.occamlab.te.spi.jaxrs.TestSuiteController;
import com.occamlab.te.spi.jaxrs.TestSuiteRegistry;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * A collection resource that provides a listing of all available test suites.
 */
@Path("suites")
public class TestSuiteSetResource {

    @Context
    private UriInfo reqUriInfo;
    private static final String HTML_NS = "http://www.w3.org/1999/xhtml";
    private DocumentBuilder docBuilder;

    public TestSuiteSetResource() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            this.docBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestSuiteSetResource.class.getName()).log(
                    Level.WARNING, null, ex);
        }
    }

    /**
     * Presents an XHTML representation containing a listing of registered test
     * suites with links to each.
     *
     * @return A Source object containing the information needed to read the
     *         collection (an HTML5 document represented using the XHTML
     *         syntax).
     */
    @GET
    @Produces("application/xhtml+xml; charset='utf-8'")
    public Source listTestSuites() {
        Document xhtmlDoc = readTemplate();
        if (null == xhtmlDoc) {
            throw new WebApplicationException(Response.serverError()
                    .entity("Failed to parse test-suites.html")
                    .type(MediaType.TEXT_PLAIN).build());
        }
        Element listElem = (Element) xhtmlDoc.getElementsByTagNameNS(HTML_NS,
                "ul").item(0);
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        Set<TestSuiteController> etsControllers = registry.getControllers();
        StringBuilder etsURI = new StringBuilder();
        for (TestSuiteController etsController : etsControllers) {
            Element li = xhtmlDoc.createElementNS(HTML_NS, "li");
            listElem.appendChild(li);
            Element link = xhtmlDoc.createElementNS(HTML_NS, "a");
            li.appendChild(link);
            Text title = xhtmlDoc.createTextNode(etsController.getTitle());
            link.appendChild(title);
            link.setAttribute("type", "text/html");
            if (!reqUriInfo.getPath().endsWith("/")) {
                etsURI.append(this.reqUriInfo.getPath()).append("/");
            }
            etsURI.append(etsController.getCode()).append("/");
            link.setAttribute("href", etsURI.toString());
            link.setAttribute("id", etsController.getCode());
            etsURI.setLength(0);
        }
        return new DOMSource(xhtmlDoc);
    }

    /**
     * Presents an XML representation containing a listing of registered test
     * suites with links to each.
     * 
     * @return A Source object containing the information needed to read the
     *         collection (an XML document).
     */
    @GET
    @Produces("application/xml; charset='utf-8'")
    public Source listTestSuitesAsXML() {
        Document xmlDoc = this.docBuilder.newDocument();

        Element testSuites = xmlDoc.createElement("testSuites");
        xmlDoc.appendChild(testSuites);

        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        Set<TestSuiteController> etsControllers = registry.getControllers();

        for (TestSuiteController etsController : etsControllers) {

            Element testSuite = xmlDoc.createElement("testSuite");
            testSuites.appendChild(testSuite);

            Element testSuiteTitle = xmlDoc.createElement("title");
            testSuiteTitle.setTextContent(etsController.getTitle());
            testSuite.appendChild(testSuiteTitle);
            
            Element testSuiteVersion = xmlDoc.createElement("version");
            testSuiteVersion.setTextContent(etsController.getVersion());
            testSuite.appendChild(testSuiteVersion);

            Element testSuiteRestUri = xmlDoc.createElement("endpoint");

            UriBuilder etsURI = reqUriInfo.getRequestUriBuilder();
            etsURI.path(etsController.getCode());
            testSuiteRestUri.setTextContent(etsURI.build().toString());
            testSuite.appendChild(testSuiteRestUri);
            
            Element testSuiteEtsCode = xmlDoc.createElement("etscode");
            testSuiteEtsCode.setTextContent(etsController.getCode());
            testSuite.appendChild(testSuiteEtsCode);
        }
        return new DOMSource(xmlDoc);
    }

    /**
     * Presents an JSON representation containing a listing of registered test
     * suites with links to each.
     * 
     * @return A Response with object containing the information.
     */
    @GET
    @Produces("application/json")
    public Response listTestSuitesAsJSON() {
        List<JSONObject> testSuiteList = new ArrayList<JSONObject>();
        
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        Set<TestSuiteController> etsControllers = registry.getControllers();
        
        for (TestSuiteController etsController : etsControllers) {
            StringBuilder etsURI = new StringBuilder();
            etsURI.append(reqUriInfo.getBaseUri());
            etsURI.append(this.reqUriInfo.getPath());
            etsURI.append(etsController.getCode()).append("/");
            
            Map<String, String> testSuiteInfoMap = new HashMap<String, String>();
            testSuiteInfoMap.put("title", etsController.getTitle());
            testSuiteInfoMap.put("version", etsController.getVersion());
            testSuiteInfoMap.put("etsCode", etsController.getCode());
            testSuiteInfoMap.put("endpoint", etsURI.toString());
            
            JSONObject testSuiteInfo = new JSONObject(testSuiteInfoMap);
            
            testSuiteList.add(testSuiteInfo);
        }
        Map<String, List<JSONObject>> testSuitesMap = new HashMap<String, List<JSONObject>>();
        testSuitesMap.put("testSuites", testSuiteList);

        JSONObject testSuites = new JSONObject(testSuitesMap);
        
        return Response.status(200).entity(testSuites.toString()).build();
    }

    /**
     * Reads the template document from the classpath. It contains an empty
     * list.
     *
     * @return A DOM Document node.
     */
    Document readTemplate() {
        InputStream inStream = this.getClass().getResourceAsStream(
                "test-suites.html");
        Document doc = null;
        try {
            doc = this.docBuilder.parse(inStream);
            // Fortify Mod: Close the InputStream and release its resources
            inStream.close();
        } catch (Exception ex) {
            Logger.getLogger(TestSuiteSetResource.class.getName()).log(
                    Level.WARNING, "Failed to parse test-suites.html", ex);
        }
        return doc;
    }
}
