/**
 * ***************************************************************************
 *
 * Version Date: January 8, 2018
 *
 * Contributor(s):
 *     C. Heazel (WiSC): Modifications to address Fortify issues
 *     C. Heazel (WiSC): Moved vocabulary package from spi to core
 *
 * ***************************************************************************
 */

package com.occamlab.te;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.occamlab.te.vocabulary.CITE;
import com.occamlab.te.vocabulary.CONTENT;
import com.occamlab.te.vocabulary.EARL;
import com.occamlab.te.vocabulary.HTTP;

import static java.lang.Integer.valueOf;

public class CtlEarlReporter {

	private static final Logger LOG = Logger.getLogger(CtlEarlReporter.class.getName());

	private String langCode = "en";

	private Resource testRun;

	private int resultCount = 0;

	private Resource assertor;

	private Resource testSubject;

	private Model earlModel;

	private Seq reqs;

	private int cPassCount;

	private int cFailCount;

	private int cSkipCount;

	private int cContinueCount;

	private int cBestPracticeCount;

	private int cNotTestedCount;

	private int cWarningCount;

	private int cInheritedFailureCount;

	private int totalPassCount;

	private int totalFailCount;

	private int totalSkipCount;

	private int totalContinueCount;

	private int totalBestPracticeCount;

	private int totalNotTestedCount;

	private int totalWarningCount;

	private int totalInheritedFailureCount;

	private Boolean areCoreConformanceClassesPassed = true;

	private String tmpDir = System.getProperty("java.io.tmpdir");

	public CtlEarlReporter() {
		this.earlModel = ModelFactory.createDefaultModel();
		totalPassCount = 0;
		totalFailCount = 0;
		totalSkipCount = 0;
		totalContinueCount = 0;
		totalBestPracticeCount = 0;
		totalNotTestedCount = 0;
		totalWarningCount = 0;
		totalInheritedFailureCount = 0;
	}

	public void generateEarlReport(File outputDirectory, File reportFile, String suiteName, Map params)
			throws UnsupportedEncodingException {
		DocumentBuilderFactory docFactory = createDcumentBuilder();
		Document document;
		try {
			document = docFactory.newDocumentBuilder().parse(reportFile);
		}
		catch (IOException | SAXException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		document.getDocumentElement().normalize();

		Model earlReport = generateEarlReport(suiteName, params, document);

		try {
			writeModel(earlReport, outputDirectory, true);
		}
		catch (IOException iox) {
			throw new RuntimeException("Failed to serialize EARL results", iox);
		}
	}

	public void generateEarlReport(OutputStream targetEarlReport, InputStream report, String suiteName, Map params)
			throws UnsupportedEncodingException {
		DocumentBuilderFactory docFactory = createDcumentBuilder();
		Document document;
		try {
			document = docFactory.newDocumentBuilder().parse(report);
		}
		catch (IOException | SAXException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		document.getDocumentElement().normalize();

		Model earlReport = generateEarlReport(suiteName, params, document);

		try {
			writeModel(earlReport, targetEarlReport, true);
		}
		catch (IOException iox) {
			throw new RuntimeException("Failed to serialize EARL results", iox);
		}
	}

	private DocumentBuilderFactory createDcumentBuilder() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		docFactory.setXIncludeAware(true);
		return docFactory;
	}

	private Model generateEarlReport(String suiteName, Map params, Document document)
			throws UnsupportedEncodingException {
		Model model = initializeModel(suiteName);
		addTestInputs(model, params);
		this.reqs = model.createSeq();

		NodeList executionList = document.getElementsByTagName("execution");

		for (int temp = 0; temp < executionList.getLength(); temp++) {
			Node executionNode = executionList.item(temp);
			Element executionElement = (Element) executionNode;
			NodeList logList = executionElement.getElementsByTagName("log");
			Element logElement = (Element) logList.item(0);
			NodeList testcallList = logElement.getElementsByTagName("testcall");
			getSubtestResult(model, testcallList, logList);
		}

		this.testRun.addProperty(CITE.requirements, this.reqs);
		this.testRun.addLiteral(CITE.testsPassed, valueOf(this.totalPassCount));
		this.testRun.addLiteral(CITE.testsFailed, valueOf(this.totalFailCount));
		this.testRun.addLiteral(CITE.testsSkipped, valueOf(this.totalSkipCount));
		this.testRun.addLiteral(CITE.testsContinue, valueOf(this.totalContinueCount));
		this.testRun.addLiteral(CITE.testsBestPractice, valueOf(this.totalBestPracticeCount));
		this.testRun.addLiteral(CITE.testsNotTested, valueOf(this.totalNotTestedCount));
		this.testRun.addLiteral(CITE.testsWarning, valueOf(this.totalWarningCount));
		this.testRun.addLiteral(CITE.testsInheritedFailure, valueOf(this.totalInheritedFailureCount));
		this.testRun.addLiteral(CITE.testSuiteType, "ctl");
		this.testRun.addLiteral(CITE.areCoreConformanceClassesPassed, areCoreConformanceClassesPassed);

		this.earlModel.add(model);

		return this.earlModel;
	}

	private void getSubtestResult(Model model, NodeList testcallList, NodeList logList)
			throws UnsupportedEncodingException {
		String conformanceClass = "";
		for (int k = 0; k < testcallList.getLength(); k++) {

			Element testcallElement = (Element) testcallList.item(k);
			String testcallPath = testcallElement.getAttribute("path");

			Element logElements = findMatchingLogElement(logList, testcallPath);

			if (logElements != null) {
				TestInfo testInfo = getTestinfo(logElements);

				if (testInfo.isConformanceClass) {
					conformanceClass = testInfo.testName;
					this.cPassCount = 0;
					this.cFailCount = 0;
					this.cSkipCount = 0;
					this.cContinueCount = 0;
					this.cBestPracticeCount = 0;
					this.cNotTestedCount = 0;
					this.cWarningCount = 0;
					this.cInheritedFailureCount = 0;
					addTestRequirements(model, testInfo);
				}

				processTestResults(model, logElements, logList, conformanceClass, null);

				Resource testReq = model.createResource(conformanceClass);
				testReq.addLiteral(CITE.testsPassed, valueOf(this.cPassCount));
				testReq.addLiteral(CITE.testsFailed, valueOf(this.cFailCount));
				testReq.addLiteral(CITE.testsSkipped, valueOf(this.cSkipCount));
				testReq.addLiteral(CITE.testsContinue, valueOf(this.cContinueCount));
				testReq.addLiteral(CITE.testsBestPractice, valueOf(this.cBestPracticeCount));
				testReq.addLiteral(CITE.testsNotTested, valueOf(this.cNotTestedCount));
				testReq.addLiteral(CITE.testsWarning, valueOf(this.cWarningCount));
				testReq.addLiteral(CITE.testsInheritedFailure, valueOf(this.cInheritedFailureCount));
				if (testInfo.isBasic) {
					if (cFailCount > 0 || cInheritedFailureCount > 0) {
						areCoreConformanceClassesPassed = false;
					}
				}
				this.totalPassCount += cPassCount;
				this.totalFailCount += cFailCount;
				this.totalSkipCount += cSkipCount;
				this.totalContinueCount += cContinueCount;
				this.totalBestPracticeCount += cBestPracticeCount;
				this.totalNotTestedCount += cNotTestedCount;
				this.totalWarningCount += cWarningCount;
				this.totalInheritedFailureCount += cInheritedFailureCount;
			}
		}

	}

	private Element findMatchingLogElement(NodeList logList, String testcallPath) throws UnsupportedEncodingException {
		for (int j = 0; j < logList.getLength(); j++) {
			Element logElement = (Element) logList.item(j);
			String decodedBaseURL = java.net.URLDecoder.decode(logElement.getAttribute("xml:base"),
					StandardCharsets.UTF_8);
			String logtestcall = parseLogTestCall("", decodedBaseURL);
			// Check sub-testcall is matching with the <log baseURL="">
			if (testcallPath.equals(logtestcall)) {
				return logElement;
			}

		}
		return null;
	}

	private TestInfo getTestinfo(Element logElements) {
		Element starttestElements = getElementByTagName(logElements, "starttest");
		Element endtestElements = getElementByTagName(logElements, "endtest");
		String assertion = parseTextContent(logElements, "assertion");
		if (assertion == null) {
			assertion = "Null";
		}
		String testName = starttestElements.getAttribute("local-name");
		int result = Integer.parseInt(endtestElements.getAttribute("result"));
		Element ccElement = getElementByTagName(logElements, "conformanceClass");
		boolean isCC = ccElement != null;
		boolean isBasic = false;
		if (ccElement != null && ccElement.hasAttribute("isBasic")
				&& Boolean.valueOf(ccElement.getAttribute("isBasic"))) {
			isBasic = true;
		}
		return new TestInfo(assertion, testName, result, isCC, isBasic);
	}

	private Model initializeModel(String suiteName) {
		Model model = ModelFactory.createDefaultModel();
		Map<String, String> nsBindings = new HashMap<>();
		nsBindings.put("earl", EARL.NS_URI);
		nsBindings.put("dct", DCTerms.NS);
		nsBindings.put("cite", CITE.NS_URI);
		nsBindings.put("http", HTTP.NS_URI);
		nsBindings.put("cnt", CONTENT.NS_URI);
		model.setNsPrefixes(nsBindings);
		this.testRun = model.createResource(CITE.TestRun);
		this.testRun.addProperty(DCTerms.title, suiteName);
		String nowUTC = ZonedDateTime.now(ZoneId.of("Z")).format(DateTimeFormatter.ISO_INSTANT);
		this.testRun.addProperty(DCTerms.created, nowUTC);
		this.assertor = model.createResource("https://github.com/opengeospatial/teamengine", EARL.Assertor);
		this.assertor.addProperty(DCTerms.title, "OGC TEAM Engine", this.langCode);
		this.assertor.addProperty(DCTerms.description,
				"Official test harness of the OGC conformance testing program (CITE).", this.langCode);
		/*
		 * Map<String, String> params = suite.getXmlSuite().getAllParameters(); String iut
		 * = params.get("iut"); if (null == iut) { // non-default parameter refers to test
		 * subject--use first URI value for (Map.Entry<String, String> param :
		 * params.entrySet()) { try { URI uri = URI.create(param.getValue()); iut =
		 * uri.toString(); } catch (IllegalArgumentException e) { continue; } } } if (null
		 * == iut) { throw new
		 * NullPointerException("Unable to find URI reference for IUT in test run parameters."
		 * ); }
		 */

		this.testSubject = model.createResource("", EARL.TestSubject);
		return model;
	}

	private void addTestRequirements(Model earl, TestInfo testInfo) {
		Resource testReq = earl.createResource(testInfo.testName.replaceAll("\\s", "-"), EARL.TestRequirement);
		testReq.addProperty(DCTerms.title, testInfo.testName);
		testReq.addProperty(CITE.isBasic, Boolean.toString(testInfo.isBasic));
		this.reqs.add(testReq);
	}

	/*
	 * Process child tests of Conformance Class and call same method recursively if it has
	 * the child tests.
	 */
	private void processTestResults(Model earl, Element logElement, NodeList logList, String conformanceClass,
			Resource parentTestCase) throws UnsupportedEncodingException {
		NodeList childtestcallList = logElement.getElementsByTagName("testcall");

		for (int l = 0; l < childtestcallList.getLength(); l++) {
			Element childtestcallElement = (Element) childtestcallList.item(l);
			String testcallPath = childtestcallElement.getAttribute("path");

			Element childlogElement = findMatchingLogElement(logList, testcallPath);

			if (childlogElement == null)
				throw new NullPointerException("Failed to get Test-Info due to null log element.");
			TestInfo testDetails = getTestinfo(childlogElement);

			// create earl:Assertion
			GregorianCalendar calTime = new GregorianCalendar(TimeZone.getDefault());
			Resource assertion = earl.createResource("assert-" + ++this.resultCount, EARL.Assertion);
			assertion.addProperty(EARL.mode, EARL.AutomaticMode);
			assertion.addProperty(EARL.assertedBy, this.assertor);
			assertion.addProperty(EARL.subject, this.testSubject);
			// link earl:TestResult to earl:Assertion
			Resource earlResult = earl.createResource("result-" + this.resultCount, EARL.TestResult);
			earlResult.addProperty(DCTerms.date, earl.createTypedLiteral(calTime));

			processTestResult(childlogElement, testDetails, earlResult);
			processRequests(earlResult, childlogElement, earl);

			assertion.addProperty(EARL.result, earlResult);
			// link earl:TestCase to earl:Assertion and earl:TestRequirement
			String testName = testDetails.testName;
			StringBuilder testCaseId = new StringBuilder(testcallPath);
			testCaseId.append('#').append(testName);
			Resource testCase = earl.createResource(testCaseId.toString(), EARL.TestCase);
			testCase.addProperty(DCTerms.title, testName);
			testCase.addProperty(DCTerms.description, testDetails.assertion);
			assertion.addProperty(EARL.test, testCase);

			if (parentTestCase != null)
				parentTestCase.addProperty(DCTerms.hasPart, testCase);
			else
				earl.createResource(conformanceClass).addProperty(DCTerms.hasPart, testCase);
			processTestResults(earl, childlogElement, logList, conformanceClass, testCase);
		}
	}

	private void processTestResult(Element childlogElement, TestInfo testDetails, Resource earlResult) {
		switch (testDetails.result) {
			case 0:
				earlResult.addProperty(EARL.outcome, CITE.Continue);
				this.cContinueCount++;
				break;
			case 2:
				earlResult.addProperty(EARL.outcome, CITE.Not_Tested);
				this.cNotTestedCount++;
				break;
			case 6: // Fail
				earlResult.addProperty(EARL.outcome, EARL.Fail);
				String errorMessage = parseTextContent(childlogElement, "exception");
				if (errorMessage != null) {
					earlResult.addProperty(DCTerms.description, errorMessage);
				}
				this.cFailCount++;
				break;
			case 3:
				earlResult.addProperty(EARL.outcome, EARL.NotTested);
				this.cSkipCount++;
				break;
			case 4:
				earlResult.addProperty(EARL.outcome, CITE.Warning);
				this.cWarningCount++;
				break;
			case 5:
				earlResult.addProperty(EARL.outcome, CITE.Inherited_Failure);
				this.cInheritedFailureCount++;
				break;
			default:
				earlResult.addProperty(EARL.outcome, EARL.Pass);
				this.cPassCount++;
				break;
		}
	}

	private void processRequests(Resource earlResult, Element childlogElements, Model earl) {
		NodeList requestList = childlogElements.getElementsByTagName("request");
		for (int i = 0; i < requestList.getLength(); i++) {
			Element reqElement = (Element) requestList.item(i);
			processRequest(earlResult, earl, reqElement);
		}
	}

	private void processRequest(Resource earlResult, Model earl, Element reqElement) {
		Resource httpReq = createEarlRequest(earl, reqElement);
		if (httpReq == null)
			return;
		String response = parseNodeAsString(reqElement, "response");
		if (response != null) {
			Resource httpRsp = earl.createResource(HTTP.Response);
			Resource rspContent = earl.createResource(CONTENT.ContentAsXML);
			rspContent.addProperty(CONTENT.rest, response);
			httpRsp.addProperty(HTTP.body, rspContent);
			httpReq.addProperty(HTTP.resp, httpRsp);
		}
		earlResult.addProperty(CITE.message, httpReq);
	}

	private Resource createEarlRequest(Model earl, Element reqElement) {
		Element requestNode = getElementByTagName(reqElement, "http://www.occamlab.com/ctl", "request");
		if (requestNode != null) {
			String httpMethod = parseTextContent(requestNode, "http://www.occamlab.com/ctl", "method");
			String url = parseTextContent(requestNode, "http://www.occamlab.com/ctl", "url");
			Resource earlRequest = earl.createResource(HTTP.Request);
			if ("GET".equalsIgnoreCase(httpMethod)) {
				Map<String, String> parameters = parseParameters(requestNode);
				String urlWithQueryString = createUrlWithQueryString(url, parameters);
				earlRequest.addProperty(HTTP.methodName, httpMethod);
				earlRequest.addProperty(HTTP.requestURI, urlWithQueryString);
				return earlRequest;
			}
			if ("POST".equalsIgnoreCase(httpMethod)) {
				try {
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");

					StreamResult result = new StreamResult(new StringWriter());
					DOMSource source = new DOMSource(requestNode);
					transformer.transform(source, result);

					String xmlString = result.getWriter().toString();
					result.getWriter().close();

					Resource reqContent = earl.createResource(CONTENT.ContentAsXML);

					reqContent.addProperty(CONTENT.rest, xmlString);
					earlRequest.addProperty(HTTP.body, reqContent);
					return earlRequest;
				}
				catch (Exception e) {
					new RuntimeException("Request content is not well-formatted. " + e.getMessage());
				}
			}
		}
		return null;
	}

	private String createUrlWithQueryString(String url, Map<String, String> parameters) {
		if (parameters.isEmpty())
			return url;
		StringBuilder urlWithQueryString = new StringBuilder(url);
		if (!url.contains("?"))
			urlWithQueryString.append("?");
		if (!url.endsWith("?") && !url.endsWith("&"))
			urlWithQueryString.append("&");
		boolean isFirst = true;
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			if (!isFirst)
				urlWithQueryString.append("&");
			urlWithQueryString.append(parameter.getKey());
			urlWithQueryString.append("=");
			urlWithQueryString.append(parameter.getValue());
			isFirst = false;
		}
		return urlWithQueryString.toString();
	}

	private Map<String, String> parseParameters(Element requestNode) {
		Map<String, String> parameters = new HashMap<>();
		NodeList paramNodes = requestNode.getElementsByTagNameNS("http://www.occamlab.com/ctl", "param");
		for (int paramNodeIndex = 0; paramNodeIndex < paramNodes.getLength(); paramNodeIndex++) {
			Element paramNode = (Element) paramNodes.item(paramNodeIndex);
			String paramName = paramNode.getAttribute("name");
			String paramValue = paramNode.getTextContent();
			parameters.put(paramName, paramValue);
		}
		return parameters;
	}

	private Element getElementByTagName(Element element, String tagName) {
		return getElementByTagName(element, null, tagName);
	}

	private Element getElementByTagName(Element element, String tagNamespaceUri, String tagName) {
		NodeList elementsByTagName;
		if (tagNamespaceUri != null)
			elementsByTagName = element.getElementsByTagNameNS(tagNamespaceUri, tagName);
		else
			elementsByTagName = element.getElementsByTagName(tagName);

		if (elementsByTagName.getLength() > 0)
			return (Element) elementsByTagName.item(0);
		return null;
	}

	private String parseTextContent(Element element, String tagName) {
		return parseTextContent(element, null, tagName);
	}

	private String parseTextContent(Element element, String tagNamespaceUri, String tagName) {
		Element elementByTagName = getElementByTagName(element, tagNamespaceUri, tagName);
		if (elementByTagName != null)
			return elementByTagName.getTextContent();
		return null;
	}

	private String parseNodeAsString(Element element, String tagName) {
		Element elementByTagName = getElementByTagName(element, tagName);
		if (elementByTagName != null) {
			try {
				TransformerFactory transFactory = TransformerFactory.newInstance();
				Transformer transformer = transFactory.newTransformer();
				StringWriter buffer = new StringWriter();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.transform(new DOMSource(elementByTagName), new StreamResult(buffer));
				return buffer.toString();
			}
			catch (TransformerException e) {
				LOG.warning("Could not parse node as string: " + e.getMessage());
			}
		}
		return null;
	}

	/**
	 * This method is used to add test inputs in to earl report.
	 * @param earl Model object to add the result into it.
	 * @param params The variable is type of Map with all the user input.
	 */
	private void addTestInputs(Model earl, Map<String, String> params) {
		Bag inputs = earl.createBag();
		if (!params.equals("") && params != null) {
			String value = "";
			for (String key : params.keySet()) {
				value = params.get(key);
				Resource testInputs = earl.createResource();
				testInputs.addProperty(DCTerms.title, key);
				testInputs.addProperty(DCTerms.description, value);
				inputs.add(testInputs);
			}
		}
		this.testRun.addProperty(CITE.inputs, inputs);
	}

	private void writeModel(Model earlModel, File outputDirectory, boolean abbreviated) throws IOException {

		File outputFile = new File(outputDirectory, "earl-results.rdf");
		if (!outputFile.createNewFile()) {
			outputFile.delete();
			outputFile.createNewFile();
		}
		String baseUri = "http://example.org/earl/" + outputDirectory.getName() + '/';
		OutputStream outStream = new FileOutputStream(outputFile);
		writeModel(earlModel, outStream, abbreviated, baseUri);

	}

	private void writeModel(Model earlModel, OutputStream targetEarlReport, boolean abbreviated) throws IOException {
		String baseUri = "http://example.org/earl/";
		writeModel(earlModel, targetEarlReport, abbreviated, baseUri);

	}

	private void writeModel(Model earlModel, OutputStream outStream, boolean abbreviated, String baseUri)
			throws IOException {
		String syntax = (abbreviated) ? "RDF/XML-ABBREV" : "RDF/XML";
		try (Writer writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8)) {
			earlModel.write(writer, syntax, baseUri);
		}
	}

	private String parseLogTestCall(String logtestcall, String decodedBaseURL) {
		if (decodedBaseURL.contains("users") && !decodedBaseURL.contains("rest")) {
			String baseUrl = decodedBaseURL.substring(decodedBaseURL.indexOf("users"));
			int first = baseUrl.indexOf(FileSystems.getDefault().getSeparator());
			int second = baseUrl.indexOf(FileSystems.getDefault().getSeparator(), first + 1);
			logtestcall = baseUrl.substring(second + 1, baseUrl.lastIndexOf(FileSystems.getDefault().getSeparator()));
		}
		else if (decodedBaseURL.contains("rest")) {
			String baseUrl = decodedBaseURL.substring(decodedBaseURL.indexOf("users"));
			baseUrl = baseUrl.replace("rest" + FileSystems.getDefault().getSeparator(), "");
			int first = baseUrl.indexOf(FileSystems.getDefault().getSeparator());
			int second = baseUrl.indexOf(FileSystems.getDefault().getSeparator(), first + 1);
			logtestcall = baseUrl.substring(second + 1, baseUrl.lastIndexOf(FileSystems.getDefault().getSeparator()));
		}
		else if (decodedBaseURL.startsWith(tmpDir)) {
			String baseUrl = decodedBaseURL.replace(tmpDir, "");
			logtestcall = baseUrl.substring(0, baseUrl.lastIndexOf(FileSystems.getDefault().getSeparator()));
		}
		else if (decodedBaseURL.contains("unittest")) {
			// for Unit test only
			String baseUrl = decodedBaseURL.substring(decodedBaseURL.indexOf("unittest"));
			logtestcall = baseUrl.substring(baseUrl.indexOf("/") + 1, baseUrl.lastIndexOf("/"));
		}
		if (logtestcall.startsWith(FileSystems.getDefault().getSeparator())) {
			logtestcall = logtestcall.substring(1);
		}
		if (logtestcall.contains("\\")) {
			logtestcall = logtestcall.replace("\\", "/");
		}
		return logtestcall;
	}

	private class TestInfo {

		private String assertion;

		private String testName;

		private int result;

		private boolean isConformanceClass;

		private boolean isBasic;

		public TestInfo(String assertion, String testName, int result, boolean isConformanceClass, boolean isBasic) {
			this.assertion = assertion;
			this.testName = testName;
			this.result = result;
			this.isConformanceClass = isConformanceClass;
			this.isBasic = isBasic;
		}

	}

}
