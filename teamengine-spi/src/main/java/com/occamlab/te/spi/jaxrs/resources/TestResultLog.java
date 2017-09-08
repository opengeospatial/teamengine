package com.occamlab.te.spi.jaxrs.resources;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.spi.jaxrs.ErrorResponseBuilder;
import com.occamlab.te.spi.jaxrs.TestSuiteController;
import com.occamlab.te.spi.jaxrs.TestSuiteRegistry;

@Path("suites/{etsCode}/{etsVersion}/html/run")
@Produces("application/zip, text/html")
public class TestResultLog {

	private static final Logger LOGR = Logger.getLogger(TestRunResource.class.getPackage().getName());
    @Context
    private UriInfo reqUriInfo;

    @Context
    private HttpHeaders headers;
	
	@GET
   // @Consumes({"application/html"})
    public Response handleHtmlGet(@PathParam("etsCode") String etsCode, @PathParam("etsVersion") String etsVersion ) throws IOException {
    	MultivaluedMap<String, String> params = this.reqUriInfo.getQueryParameters();
        Source results = executeTestRun(etsCode, etsVersion, params);
        
        String htmlOutput = results.getSystemId().toString();
        int  count = htmlOutput.split(":", -1).length-1;
		String zipFile = (count > 1) ? htmlOutput.split("file:/")[1] : htmlOutput.split("file:")[1];
        File fileOut = new File(zipFile);
        if (!fileOut.exists()) {
            throw new WebApplicationException(404);
        }
        return Response
                .ok(FileUtils.readFileToByteArray(fileOut))
                .type("application/zip")
                .header("Content-Disposition", "attachment; filename=\"result.zip\";").header("Cache-Control", "no-cache")
                .build();
        //return results;
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
     * @throws WebApplicationException
     *             If an error occurs while executing a test run.
     */
    Source executeTestRun(String etsCode, String etsVersion, Map<String, java.util.List<String>> testRunArgs) {
        MediaType preferredMediaType = this.headers.getAcceptableMediaTypes().get(0);
        testRunArgs.put("acceptMediaType", Arrays.asList(preferredMediaType.toString()));
        if (LOGR.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("Test run arguments - ");
            msg.append(etsCode).append("/").append(etsVersion).append("\n");
            msg.append(testRunArgs.toString());
            if (null != this.headers.getMediaType()) {
                msg.append("Entity media type: " + this.headers.getMediaType());
            }
            LOGR.fine(msg.toString());
        }
        Document xmlArgs = readTestRunArguments(testRunArgs);
        TestSuiteController controller = findController(etsCode, etsVersion);
        Source testResults = null;
        try {
            testResults = controller.doTestRun(xmlArgs);
        } catch (IllegalArgumentException iae) {
            ErrorResponseBuilder builder = new ErrorResponseBuilder();
            Response rsp = builder.buildErrorResponse(400, iae.getMessage());
            throw new WebApplicationException(rsp);
        } catch (Exception ex) {
            LOGR.log(Level.WARNING, ex.getMessage(), ex);
            ErrorResponseBuilder builder = new ErrorResponseBuilder();
            Response rsp = builder.buildErrorResponse(500,
                    String.format("Error executing test suite (%s-%s)", etsCode, etsVersion));
            throw new WebApplicationException(rsp);
        }
        LOGR.fine(String.format("Test results for suite %s-%s: %s", etsCode, etsVersion, testResults.getSystemId()));
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
    TestSuiteController findController(String code, String version) throws WebApplicationException {
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        TestSuiteController controller = registry.getController(code, version);
        if (null == controller) {
            throw new WebApplicationException(404);
        }
        return controller;
    }

    /**
     * Extracts test run arguments from the given Map and inserts them into a
     * DOM Document representing an XML properties file.
     * 
     * @param requestParams
     *            A collection of key-value pairs. Each key can have zero or
     *            more values but only the first value is used.
     * @return A DOM Document node.
     * @see java.util.Properties
     */
    Document readTestRunArguments(Map<String, java.util.List<String>> requestParams) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document propsDoc = null;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            propsDoc = db.newDocument();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestRunResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        Element docElem = propsDoc.createElement("properties");
        docElem.setAttribute("version", "1.0");
        for (Map.Entry<String, List<String>> param : requestParams.entrySet()) {
            Element entry = propsDoc.createElement("entry");
            entry.setAttribute("key", param.getKey());
            StringBuilder values = new StringBuilder();
            for (Iterator<String> itr = param.getValue().iterator(); itr.hasNext();) {
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
