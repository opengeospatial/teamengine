package com.occamlab.te.spi.jaxrs.resources;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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

    @Context
    private HttpHeaders headers;

    /**
     * Processes a request submitted using the GET method. The test run
     * arguments are specified in the query component of the Request-URI as a
     * sequence of key-value pairs.
     * 
     * @param etsCode
     *            A String that identifies the test suite to be run.
     * @param etsVersion
     *            A String specifying the desired test suite version.
     * @return An XML representation of the test results.
     */
    @GET
    public Source handleGet(@PathParam("etsCode") String etsCode,
            @PathParam("etsVersion") String etsVersion) {
        MultivaluedMap<String, String> params = this.reqUriInfo
                .getQueryParameters();
        if (LOGR.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("Test run arguments - ");
            msg.append(etsCode).append("/").append(etsVersion).append("\n");
            msg.append(params.toString());
            LOGR.fine(msg.toString());
        }
        Source results = executeTestRun(etsCode, etsVersion, params);
        return results;
    }

    /**
     * Processes a request submitted using the POST method. The request entity
     * represents the test subject or provides metadata about it. The entity
     * body is written to a local file, the location of which is set as the
     * value of the {@code iut} parameter.
     * 
     * @param etsCode
     *            A String that identifies the test suite to be run.
     * @param etsVersion
     *            A String specifying the desired test suite version.
     * @param entityBody
     *            A File containing the request entity body.
     * @return An XML representation of the test results.
     */
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Source handlePost(@PathParam("etsCode") String etsCode,
            @PathParam("etsVersion") String etsVersion, File entityBody) {
        if (!entityBody.exists() || entityBody.length() == 0) {
            throw new WebApplicationException(400);
        }
        if (LOGR.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("Test run arguments - ");
            msg.append(etsCode).append("/").append(etsVersion).append("\n");
            msg.append("Entity media type: " + this.headers.getMediaType());
            msg.append("File location: " + entityBody.getAbsolutePath());
            LOGR.fine(msg.toString());
        }
        Map<String, java.util.List<String>> args = new HashMap<String, List<String>>();
        args.put("iut", Arrays.asList(entityBody.toURI().toString()));
        Source results = executeTestRun(etsCode, etsVersion, args);
        return results;
    }

    /**
     * Executes a test run using the supplied arguments.
     * 
     * @param etsCode
     *            A String that identifies the test suite to be run.
     * @param etsVersion
     *            A String specifying the desired test suite version.
     * @param testRunArgs
     *            A multi-valued Map containing the test run arguments.
     * @return An XML representation of the test run results.
     */
    Source executeTestRun(String etsCode, String etsVersion,
            Map<String, java.util.List<String>> testRunArgs) {
        TestSuiteController controller = findController(etsCode, etsVersion);
        Document xmlArgs = readTestRunArguments(testRunArgs);
        Source testResults = null;
        try {
            testResults = controller.doTestRun(xmlArgs);
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
     * Extracts test run arguments from the given Map and inserts them into a
     * DOM Document.
     * 
     * @param requestParams
     *            A collection of key-value pairs. Each key can have zero or
     *            more values but only the first value is used.
     * @return A DOM Document representing an XML properties file.
     * @see java.util.Properties
     */
    Document readTestRunArguments(
            Map<String, java.util.List<String>> requestParams) {
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
