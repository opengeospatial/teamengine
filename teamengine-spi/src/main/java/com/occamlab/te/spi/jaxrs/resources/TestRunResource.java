/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.jaxrs.resources;

/*-
 * #%L
 * TEAM Engine - Service Providers
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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
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
import com.occamlab.te.spi.util.TestRunUtils;
import com.occamlab.te.util.LogUtils;

import org.glassfish.jersey.internal.util.collection.ImmutableMultivaluedMap;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 * A controller resource that provides the results of a test run. An XML representation of
 * the results is obtained using HTTP/1.1 methods in accord with the JAX-RS 1.1
 * specification (JSR 311).
 *
 * @see <a href="http://jcp.org/en/jsr/detail?id=311">JSR 311</a>
 */
@Path("suites/{etsCode}/run")
public class TestRunResource {

	private static final Logger LOGR = Logger.getLogger(TestRunResource.class.getPackage().getName());

	private static final String TEST_RUN_ARGUMENTS = "Test run arguments - ";

	private static final String ENTITY_MEDIA_TYPE = "Entity media type: ";

	private static final String FILE_LOCATION = "File location: ";

	private static final String APPLICATION_RDF_XML = "application/rdf+xml";

	private static final String APPLICATION_ZIP = "application/zip";

	private static final String APPLICATION_XML = "application/xml";

	@Context
	private UriInfo reqUriInfo;

	@Context
	private HttpHeaders headers;

	/**
	 * Processes a request submitted using the GET method with. The test run arguments are
	 * specified in the query component of the Request-URI as a sequence of key-value
	 * pairs.
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @return An RDF (EARL) representation of the test results.
	 */
	@GET
	@Produces("application/rdf+xml;qs=0.75;charset=utf-8")
	public Source handleGetRdf(@PathParam("etsCode") String etsCode, @PathParam("etsVersion") String etsVersion) {
		return handleGet(etsCode, etsVersion, APPLICATION_RDF_XML);
	}

	/**
	 * Processes a request submitted using the GET method with. The test run arguments are
	 * specified in the query component of the Request-URI as a sequence of key-value
	 * pairs.
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @return An XML representation of the test results.
	 */
	@GET
	@Produces("application/xml;qs=0.5;charset=utf-8")
	public Source handleGetXml(@PathParam("etsCode") String etsCode, @PathParam("etsVersion") String etsVersion) {
		return handleGet(etsCode, etsVersion, APPLICATION_XML);
	}

	/**
	 * Processes a request submitted using the GET method with. The test run arguments are
	 * specified in the query component of the Request-URI as a sequence of key-value
	 * pairs.
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @return An zip archive containing the HTML representation of the test results.
	 */
	@GET
	@Produces("application/zip;qs=0.25;charset=utf-8")
	public Response handleGetZip(@PathParam("etsCode") String etsCode, @PathParam("etsVersion") String etsVersion)
			throws IOException {
		MultivaluedMap<String, String> params = this.reqUriInfo.getQueryParameters();
		params = toMutableMultivaluedMap(params);
		Source results = executeTestRun(etsCode, params, APPLICATION_ZIP);

		String htmlOutput = results.getSystemId().toString();
		int count = htmlOutput.split(":", -1).length - 1;
		String zipFile = (count > 1) ? htmlOutput.split("file:/")[1] : htmlOutput.split("file:")[1];
		File fileOut = new File(zipFile);
		if (!fileOut.exists()) {
			throw new WebApplicationException(404);
		}
		return Response.ok(FileUtils.readFileToByteArray(fileOut))
			.type(APPLICATION_ZIP)
			.header("Content-Disposition", "attachment; filename=\"result.zip\";")
			.header("Cache-Control", "no-cache")
			.build();
	}

	/**
	 * Processes a request submitted using the POST method. The request entity represents
	 * the test subject or provides metadata about it. The entity body is written to a
	 * local file, the location of which is set as the value of the {@code iut} parameter.
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @param entityBody A File containing the request entity body.
	 * @return An RDF (EARL) representation of the test results.
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	@Produces("application/rdf+xml;qs=0.75;charset=utf-8")
	public Source handlePostRdf(@PathParam("etsCode") String etsCode, @PathParam("etsVersion") String etsVersion,
			File entityBody) {
		return handlePost(etsCode, etsVersion, entityBody, APPLICATION_RDF_XML);
	}

	/**
	 * Processes a request submitted using the POST method. The request entity represents
	 * the test subject or provides metadata about it. The entity body is written to a
	 * local file, the location of which is set as the value of the {@code iut} parameter.
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @param entityBody A File containing the request entity body.
	 * @return An XML representation of the test results.
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	@Produces("application/xml;qs=0.5;charset=utf-8")
	public Source handlePostXml(@PathParam("etsCode") String etsCode, @PathParam("etsVersion") String etsVersion,
			File entityBody) {
		return handlePost(etsCode, etsVersion, entityBody, APPLICATION_XML);
	}

	/**
	 * Processes a request submitted using the POST method. The request entity represents
	 * the test subject or provides metadata about it. The entity body is written to a
	 * local file, the location of which is set as the value of the {@code iut} parameter.
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @param entityBody A File containing the request entity body.
	 * @return An zip archive containing the HTML representation of the test results.
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	@Produces("application/zip;qs=0.25;charset=utf-8")
	public Source handlePostZip(@PathParam("etsCode") String etsCode, @PathParam("etsVersion") String etsVersion,
			File entityBody) {
		return handlePost(etsCode, etsVersion, entityBody, APPLICATION_ZIP);
	}

	/**
	 * Processes a request containing a multipart (multipart/form-data) entity. The entity
	 * is expected to consist of two parts:
	 * <ol>
	 * <li>The (required) "iut" part represents the test subject or provides metadata
	 * about it; the entity body is written to a local file, the location of which is set
	 * as the value of the {@code iut } argument.</li>
	 * <li>The "sch" part defines supplementary constraints defined in a Schematron
	 * schema; it is also written to a local file, the location of which is set as the
	 * value of the {@code sch} argument.</li>
	 * </ol>
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @param entityBody A File containing a representation of the test subject.
	 * @param schBody A File containing supplementary constraints (e.g. a Schematron
	 * schema).
	 * @return An RDF (EARL) representation of the test results.
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc7578" target="_blank">RFC 7578:
	 * Returning Values from Forms: multipart/form-data</a>
	 * @see <a href=
	 * "http://standards.iso.org/ittf/PubliclyAvailableStandards/c040833_ISO_IEC_19757-3_2006(E).zip"
	 * target="_blank">ISO 19757-3: Schematron</a>
	 */
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces("application/rdf+xml;qs=0.75;charset=utf-8")
	public Source handleMultipartFormDataRdf(@PathParam("etsCode") String etsCode,
			@PathParam("etsVersion") String etsVersion, @FormDataParam("iut") File entityBody,
			@FormDataParam("sch") File schBody) {
		return handleMultipartFormDataPost(etsCode, etsVersion, entityBody, schBody, APPLICATION_RDF_XML);
	}

	/**
	 * Processes a request containing a multipart (multipart/form-data) entity. The entity
	 * is expected to consist of two parts:
	 * <ol>
	 * <li>The (required) "iut" part represents the test subject or provides metadata
	 * about it; the entity body is written to a local file, the location of which is set
	 * as the value of the {@code iut } argument.</li>
	 * <li>The "sch" part defines supplementary constraints defined in a Schematron
	 * schema; it is also written to a local file, the location of which is set as the
	 * value of the {@code sch} argument.</li>
	 * </ol>
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @param entityBody A File containing a representation of the test subject.
	 * @param schBody A File containing supplementary constraints (e.g. a Schematron
	 * schema).
	 * @return An XML representation of the test results.
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc7578" target="_blank">RFC 7578:
	 * Returning Values from Forms: multipart/form-data</a>
	 * @see <a href=
	 * "http://standards.iso.org/ittf/PubliclyAvailableStandards/c040833_ISO_IEC_19757-3_2006(E).zip"
	 * target="_blank">ISO 19757-3: Schematron</a>
	 */
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces("application/xml;qs=0.5;charset=utf-8")
	public Source handleMultipartFormDataXml(@PathParam("etsCode") String etsCode,
			@PathParam("etsVersion") String etsVersion, @FormDataParam("iut") File entityBody,
			@FormDataParam("sch") File schBody) {
		return handleMultipartFormDataPost(etsCode, etsVersion, entityBody, schBody, APPLICATION_XML);
	}

	/**
	 * Processes a request containing a multipart (multipart/form-data) entity. The entity
	 * is expected to consist of two parts:
	 * <ol>
	 * <li>The (required) "iut" part represents the test subject or provides metadata
	 * about it; the entity body is written to a local file, the location of which is set
	 * as the value of the {@code iut } argument.</li>
	 * <li>The "sch" part defines supplementary constraints defined in a Schematron
	 * schema; it is also written to a local file, the location of which is set as the
	 * value of the {@code sch} argument.</li>
	 * </ol>
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param etsVersion A String specifying the desired test suite version.
	 * @param entityBody A File containing a representation of the test subject.
	 * @param schBody A File containing supplementary constraints (e.g. a Schematron
	 * schema).
	 * @return An zip archive containing the HTML representation of the test results.
	 *
	 * @see <a href="http://tools.ietf.org/html/rfc7578" target="_blank">RFC 7578:
	 * Returning Values from Forms: multipart/form-data</a>
	 * @see <a href=
	 * "http://standards.iso.org/ittf/PubliclyAvailableStandards/c040833_ISO_IEC_19757-3_2006(E).zip"
	 * target="_blank">ISO 19757-3: Schematron</a>
	 */
	@POST
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces("application/zip;qs=0.25;charset=utf-8")
	public Source handleMultipartFormDataZip(@PathParam("etsCode") String etsCode,
			@PathParam("etsVersion") String etsVersion, @FormDataParam("iut") File entityBody,
			@FormDataParam("sch") File schBody) {
		return handleMultipartFormDataPost(etsCode, etsVersion, entityBody, schBody, APPLICATION_ZIP);
	}

	private Source handleGet(String etsCode, String etsVersion, String preferredMediaType) {
		MultivaluedMap<String, String> params = this.reqUriInfo.getQueryParameters();
		params = toMutableMultivaluedMap(params);
		if (LOGR.isLoggable(Level.FINE)) {
			StringBuilder msg = new StringBuilder(TEST_RUN_ARGUMENTS);
			msg.append(etsCode).append("/").append(etsVersion).append("\n");
			msg.append(params.toString());
			LOGR.fine(msg.toString());
		}
		return executeTestRun(etsCode, params, preferredMediaType);
	}

	private Source handlePost(String etsCode, String etsVersion, File entityBody, String preferredMediaType) {
		if (!entityBody.exists() || entityBody.length() == 0) {
			throw new WebApplicationException(400);
		}
		if (LOGR.isLoggable(Level.FINE)) {
			StringBuilder msg = new StringBuilder(TEST_RUN_ARGUMENTS);
			msg.append(etsCode).append("/").append(etsVersion).append("\n");
			msg.append(ENTITY_MEDIA_TYPE + this.headers.getMediaType());
			msg.append(FILE_LOCATION + entityBody.getAbsolutePath());
			LOGR.fine(msg.toString());
		}
		Map<String, List<String>> args = new HashMap<>();
		args.put("iut", Collections.singletonList(entityBody.toURI().toString()));
		return executeTestRun(etsCode, args, preferredMediaType);
	}

	private Source handleMultipartFormDataPost(String etsCode, String etsVersion, File entityBody, File schBody,
			String preferredMediaType) {
		Map<String, List<String>> args = new HashMap<>();
		if (!entityBody.exists() || entityBody.length() == 0) {
			throw new WebApplicationException(400);
		}
		if (LOGR.isLoggable(Level.FINE)) {
			StringBuilder msg = new StringBuilder(TEST_RUN_ARGUMENTS);
			msg.append(etsCode).append("/").append(etsVersion).append("\n");
			msg.append(ENTITY_MEDIA_TYPE + this.headers.getMediaType());
			msg.append(FILE_LOCATION + entityBody.getAbsolutePath());
			LOGR.fine(msg.toString());
		}
		args.put("iut", Collections.singletonList(entityBody.toURI().toString()));
		if (null != schBody) {
			if (!schBody.exists() || schBody.length() == 0) {
				throw new WebApplicationException(400);
			}
			if (LOGR.isLoggable(Level.FINE)) {
				StringBuilder msg = new StringBuilder(TEST_RUN_ARGUMENTS);
				msg.append(etsCode).append("/").append(etsVersion).append("\n");
				msg.append(ENTITY_MEDIA_TYPE + this.headers.getMediaType());
				msg.append(FILE_LOCATION + schBody.getAbsolutePath());
				LOGR.fine(msg.toString());
			}
			args.put("sch", Collections.singletonList(schBody.toURI().toString()));
		}
		return executeTestRun(etsCode, args, preferredMediaType);
	}

	/**
	 * Executes a test run using the supplied arguments.
	 * @param etsCode A String that identifies the test suite to be run.
	 * @param testRunArgs A multi-valued Map containing the test run arguments.
	 * @return An XML representation of the test run results.
	 * @throws WebApplicationException If an error occurs while executing a test run.
	 */
	private Source executeTestRun(String etsCode, Map<String, List<String>> testRunArgs, String preferredMediaType) {

		List<String> authCredentials = this.headers.getRequestHeader("Authorization");
		String logDir = System.getProperty("TE_BASE") + FileSystems.getDefault().getSeparator() + "users"
				+ FileSystems.getDefault().getSeparator() + TestRunUtils.getUserName(authCredentials)
				+ FileSystems.getDefault().getSeparator() + "rest";

		if (null != logDir) {
			String sessionId = LogUtils.generateSessionId(new File(logDir));

			testRunArgs.put("logDir", List.of(logDir));
			testRunArgs.put("sessionId", Collections.singletonList(sessionId));
		}

		testRunArgs.put("acceptMediaType", Collections.singletonList(preferredMediaType));
		if (LOGR.isLoggable(Level.FINE)) {
			StringBuilder msg = new StringBuilder("Test run arguments - ");
			msg.append(etsCode).append("/");
			msg.append(testRunArgs.toString());
			if (null != this.headers.getMediaType()) {
				msg.append("Entity media type: " + this.headers.getMediaType());
			}
			LOGR.fine(msg.toString());
		}
		TestSuiteController controller = findController(etsCode);

		testRunArgs.put("sourcesId", List.of(TestRunUtils.getSourcesId(controller)));
		Document xmlArgs = readTestRunArguments(testRunArgs);
		Source testResults = null;
		try {
			testResults = controller.doTestRun(xmlArgs);
		}
		catch (IllegalArgumentException iae) {
			ErrorResponseBuilder builder = new ErrorResponseBuilder();
			Response rsp = builder.buildErrorResponse(400, iae.getMessage());
			throw new WebApplicationException(rsp);
		}
		catch (Exception ex) {
			LOGR.log(Level.WARNING, ex.getMessage(), ex);
			ErrorResponseBuilder builder = new ErrorResponseBuilder();
			String error_msg = "Error executing test suite (" + etsCode + "): " + "Error message: " + ex.getMessage();
			Response rsp = builder.buildErrorResponse(500, error_msg);
			throw new WebApplicationException(rsp);
		}
		LOGR.fine(String.format("Test results for suite %s: %s", etsCode, testResults.getSystemId()));
		return testResults;
	}

	/**
	 * Obtains a <code>TestSuiteController</code> for a particular executable test suite
	 * (ETS) identified by code and version.
	 * @param code A <code>String</code> identifying the ETS to execute.
	 * @return The <code>TestSuiteController</code> for the requested ETS.
	 * @throws WebApplicationException If a corresponding controller cannot be found.
	 */
	private TestSuiteController findController(String code) throws WebApplicationException {
		TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
		TestSuiteController controller = registry.getController(code);
		if (null == controller) {
			throw new WebApplicationException(404);
		}
		return controller;
	}

	/**
	 * Extracts test run arguments from the given Map and inserts them into a DOM Document
	 * representing an XML properties file.
	 * @param requestParams A collection of key-value pairs. Each key can have zero or
	 * more values but only the first value is used.
	 * @return A DOM Document node.
	 * @see java.util.Properties
	 */
	private Document readTestRunArguments(Map<String, java.util.List<String>> requestParams) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document propsDoc = null;
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			propsDoc = db.newDocument();
		}
		catch (ParserConfigurationException ex) {
			LOGR.log(Level.SEVERE, null, ex);
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

	/**
	 * Checks if the input MulivalueMap is immutable. If this is the case, a new mutable
	 * MultivaluedHashMap is created containing the values of the input map.
	 * @param multivaluedMap A MultivalueMap
	 * @return A mutable MultivalueMap
	 */
	private MultivaluedMap<String, String> toMutableMultivaluedMap(MultivaluedMap<String, String> multivaluedMap) {
		if (multivaluedMap instanceof ImmutableMultivaluedMap<?, ?>) {
			MultivaluedMap<String, String> newMultivaluedMap = new MultivaluedHashMap<>();
			for (String key : multivaluedMap.keySet()) {
				newMultivaluedMap.put(key, multivaluedMap.get(key));
			}
			multivaluedMap = newMultivaluedMap;
		}
		return multivaluedMap;
	}

}
