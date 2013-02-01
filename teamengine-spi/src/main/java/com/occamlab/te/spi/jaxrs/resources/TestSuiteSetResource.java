package com.occamlab.te.spi.jaxrs.resources;

import java.io.InputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import com.occamlab.te.spi.jaxrs.TestSuiteController;
import com.occamlab.te.spi.jaxrs.TestSuiteRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * A collection resource that provides a listing of all available test suites.
 */
@Path("suites")
@Produces("application/xhtml+xml; charset='utf-8'")
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
            etsURI.append(etsController.getCode()).append("/")
                    .append(etsController.getVersion()).append("/");
            link.setAttribute("href", etsURI.toString());
            link.setAttribute("id", etsController.getCode() + "-"
                    + etsController.getVersion());
            etsURI.setLength(0);
        }
        return new DOMSource(xhtmlDoc);
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
        } catch (Exception ex) {
            Logger.getLogger(TestSuiteSetResource.class.getName()).log(
                    Level.WARNING, "Failed to parse test-suites.html", ex);
        }
        return doc;
    }
}
