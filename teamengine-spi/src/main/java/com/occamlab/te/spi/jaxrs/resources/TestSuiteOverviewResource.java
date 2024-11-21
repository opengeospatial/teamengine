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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.occamlab.te.spi.jaxrs.ErrorResponseBuilder;
import com.occamlab.te.spi.jaxrs.TestSuiteController;
import com.occamlab.te.spi.jaxrs.TestSuiteRegistry;

/**
 * A document resource that provides an overview of the executable test suite (ETS) and
 * guidance about how to run the tests.
 */
@Path("suites/{etsCode}/")
public class TestSuiteOverviewResource {

	private static final String APPLICATION_TEXT_HTML = "text/html";

	private static final String APPLICATION_XML = "application/xml";

	private static final String APPLICATION_JSON = "application/json";

	/**
	 * Returns a description of the test suite. The representation is a "polyglot" HTML5
	 * document that is also a well-formed XML document.
	 * @param etsCode A code denoting the relevant ETS.
	 * @return An InputStream to read the summary document from the classpath (
	 * <code>/doc/{etsCode}/{etsVersion}/overview.html</code>).
	 *
	 * @see <a href="http://www.w3.org/TR/html-polyglot/">Polyglot Markup: HTML-Compatible
	 * XHTML Documents</a>
	 */
	@GET
	@Produces("text/html; charset=utf-8")
	public InputStream getTestSuiteDescriptionAsHTML(@PathParam("etsCode") String etsCode) {
		InputStream atsStream = getTestSuiteDescription(etsCode, APPLICATION_TEXT_HTML);

		if (null == atsStream) {
			ErrorResponseBuilder builder = new ErrorResponseBuilder();
			Response rsp = builder.buildErrorResponse(404, "Test suite overview not found.");
			throw new WebApplicationException(rsp);
		}
		return atsStream;
	}

	/**
	 * Returns a description of the test suite. The representation is a well-formed XML
	 * document.
	 * @param etsCode A code denoting the relevant ETS.
	 * @return An InputStream to read the summary document from the classpath (
	 * <code>/doc/{etsCode}/{etsVersion}/overview.xml</code>).
	 */
	@GET
	@Produces("application/xml; charset=utf-8")
	public InputStream getTestSuiteDescriptionAsXML(@PathParam("etsCode") String etsCode) {
		InputStream atsStream = getTestSuiteDescription(etsCode, APPLICATION_XML);

		if (null == atsStream) {
			ErrorResponseBuilder builder = new ErrorResponseBuilder();
			Response rsp = builder.buildErrorResponse(404, "Test suite overview not found.");
			throw new WebApplicationException(rsp);
		}
		return atsStream;
	}

	/**
	 * Returns a description of the test suite. The representation is a well-formed JSON
	 * document.
	 * @param etsCode A code denoting the relevant ETS.
	 * @return An InputStream to read the summary document from the environment variable
	 * path java.io.tmpdir.
	 */
	@GET
	@Produces("application/json")
	public InputStream getTestSuiteDescriptionasJSON(@PathParam("etsCode") String etsCode) {
		InputStream atsStream = getTestSuiteDescription(etsCode, APPLICATION_JSON);

		if (null == atsStream) {
			ErrorResponseBuilder builder = new ErrorResponseBuilder();
			Response rsp = builder.buildErrorResponse(404, "Test suite overview not found.");
			throw new WebApplicationException(rsp);
		}
		return atsStream;
	}

	private StringBuilder createPathToDoc(String etsCode, String extension) {
		StringBuilder docPath = new StringBuilder();
		docPath.append("/doc/");
		docPath.append(etsCode);
		docPath.append("/");
		docPath.append(findVersion(etsCode));
		docPath.append("/overview.");
		docPath.append(extension);

		return docPath;
	}

	private String findVersion(String code) throws WebApplicationException {
		TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
		TestSuiteController controller = registry.getController(code);
		if (null == controller) {
			throw new WebApplicationException(404);
		}
		return controller.getVersion();
	}

	private InputStream getTestSuiteDescription(String etsCode, String preferredMediaType) {
		StringBuilder overviewXml = createPathToDoc(etsCode, "xml");
		InputStream atsStream = this.getClass().getResourceAsStream(overviewXml.toString());

		if (null != atsStream) {
			File resultFile = null;
			String resourceFile = null;

			if (preferredMediaType.contains("xml")) {
				return atsStream;
			}
			else if (preferredMediaType.contains("html")) {
				StringBuilder overviewHtml = createPathToDoc(etsCode, "html");
				InputStream atsStream1 = this.getClass().getResourceAsStream(overviewHtml.toString());
				if (null != atsStream1) {
					return atsStream1;
				}
				resultFile = new File(System.getProperty("java.io.tmpdir"), "testsuiteoverview.html");
				resourceFile = "com/occamlab/te/test_suite_overview_html.xsl";
			}
			else if (preferredMediaType.contains("json")) {
				resultFile = new File(System.getProperty("java.io.tmpdir"), "testsuiteoverview.json");
				resourceFile = "com/occamlab/te/test_suite_overview_json.xsl";
			}

			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			String testSuiteOverviewXsl = cl.getResource(resourceFile).toString();

			try {
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer(new StreamSource(testSuiteOverviewXsl));
				transformer.transform(new StreamSource(atsStream), new StreamResult(resultFile));

				atsStream = new FileInputStream(resultFile);

			}
			catch (TransformerException | FileNotFoundException e) {
				throw new RuntimeException(e);
			}

		}
		else {
			overviewXml = createPathToDoc(etsCode, "html");
			atsStream = this.getClass().getResourceAsStream(overviewXml.toString());
		}
		return atsStream;
	}

}
