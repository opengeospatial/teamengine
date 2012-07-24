package com.occamlab.te.spi.jaxrs.resources;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import com.occamlab.te.spi.jaxrs.ErrorResponseBuilder;
import com.occamlab.te.spi.jaxrs.TestSuiteController;
import com.occamlab.te.spi.jaxrs.TestSuiteRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A controller resource that provides the results of a test run. An XML
 * representation of the results is obtained using HTTP/1.1 methods in accord
 * with the JAX-RS 1.1 specification (JSR 311).
 * 
 * @see <a href="http://jcp.org/en/jsr/detail?id=311">JSR 311</a>
 */
@Path("suites/{etsCode}/{etsVersion}/run")
@Produces("application/xml; charset='utf-8'")
public class TestRunResource {

    private static final Logger LOGR = Logger.getLogger(TestRunResource.class
            .getPackage().getName());
    @Context
    private UriInfo reqUriInfo;

    @GET
    public Source processGetRequest(@PathParam("etsCode") String etsCode,
            @PathParam("etsVersion") String etsVersion) {
        TestSuiteController controller = findController(etsCode, etsVersion);
        MultivaluedMap<String, String> requestParams = reqUriInfo
                .getQueryParameters();
        if (LOGR.isLoggable(Level.CONFIG)) {
            StringBuilder msg = new StringBuilder("Test run parameters - ");
            msg.append(etsCode).append("/").append(etsVersion).append("\n");
            msg.append(requestParams.toString());
            LOGR.config(msg.toString());
        }
        Document testRunArgs = extractTestRunArguments(requestParams);
        Source testResults = null;
        try {
            testResults = controller.doTestRun(testRunArgs);
        } catch (RuntimeException re) {
            LOGR.log(Level.WARNING, re.getMessage(), re);
            ErrorResponseBuilder builder = new ErrorResponseBuilder();
            Response rsp = builder.buildErrorResponse(500,
                    "Internal Server Error");
            throw new WebApplicationException(rsp);
        } catch (Exception ex) {
            ErrorResponseBuilder builder = new ErrorResponseBuilder();
            Response rsp = builder.buildErrorResponse(400, ex.getMessage());
            throw new WebApplicationException(rsp);
        }
        return testResults;
    }

    /**
     * Obtains a <code>TestSuiteController</code> for a particular executable
     * test suite (ETS) identified by code and version.
     * 
     * @param code
     *            A <code>String</code> identifying the ETS to execute.
     * @param version
     *            A <code>String</code> indicating the version of the ETS.
     * @return The <code>TestSuiteController</code> for the requested ETS.
     * @throws WebApplicationException
     *             If a corresponding controller cannot be found.
     */
    TestSuiteController findController(String code, String version)
            throws WebApplicationException {
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        TestSuiteController controller = registry.getController(code, version);
        if (null == controller) {
            throw new WebApplicationException(404);
        }
        return controller;
    }

    /**
     * Extracts test run arguments from the submitted request parameters and
     * puts them into a DOM Document.
     * 
     * @param requestParams
     *            A map of key-value pairs. Each key can have zero or more
     *            values; only the first value is used.
     * @return A DOM Document representing an XML properties file.
     * @see Properties
     */
    private Document extractTestRunArguments(
            MultivaluedMap<String, String> requestParams) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document propsDoc = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            propsDoc = db.newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestRunResource.class.getName()).log(Level.SEVERE,
                    null, ex);
        }
        Element docElem = propsDoc.createElement("properties");
        docElem.setAttribute("version", "1.0");
        for (Map.Entry<String, List<String>> param : requestParams.entrySet()) {
            Element entry = propsDoc.createElement("entry");
            entry.setAttribute("key", param.getKey());
            StringBuilder values = new StringBuilder();
            for (Iterator<String> itr = param.getValue().iterator(); itr
                    .hasNext();) {
                values.append(itr.next());
                if (itr.hasNext()) {
                    values.append(",");
                }
            }
            entry.setTextContent(values.toString());
            docElem.appendChild(entry);
        }
        propsDoc.appendChild(docElem);
        return propsDoc;
    }
}
