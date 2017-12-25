package com.occamlab.te.spi.executors.testng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.HttpMethod;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.occamlab.te.spi.vocabulary.CITE;
import com.occamlab.te.spi.vocabulary.CONTENT;
import com.occamlab.te.spi.vocabulary.Config;
import com.occamlab.te.spi.vocabulary.EARL;
import com.occamlab.te.spi.vocabulary.HTTP;


/**
 * A reporter that creates and serializes an RDF graph containing the test
 * results expressed using the W3C Evaluation and Report Language (EARL)
 * vocabulary. The graph is serialized as RDF/XML to a file in the output
 * directory (earl-results.rdf).
 * 
 * @see <a href="https://www.w3.org/TR/EARL10-Schema/" target="_blank">
 *      Evaluation and Report Language (EARL) 1.0 Schema</a>
 * @see <a href="https://www.w3.org/TR/HTTP-in-RDF10/" target="_blank">HTTP
 *      Vocabulary in RDF 1.0</a>
 * @see <a href="https://www.w3.org/TR/Content-in-RDF10/" target=
 *      "_blank">Representing Content in RDF 1.0</a>
 */
public class EarlReporter implements IReporter {

    private static final Logger LOGR = Logger.getLogger(EarlReporter.class.getPackage().getName());
    /** ISO 639 language code (2-3 letter, possibly with region subtag). */
    private String langCode = "en";
    private static final String TEST_RUN_ID = "uuid";
    private Resource testRun;
    private int resultCount = 0;
    private Resource assertor;
    private Resource testSubject;
    private static final String REQ_ATTR = "request";
    private static final String RSP_ATTR = "response";
    private Model earlModel;
    private String suiteName;
    private Config config;

    public EarlReporter() {
        this.earlModel = ModelFactory.createDefaultModel();
        config = new Config();
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
            String outputDirectory) {
        for (ISuite suite : suites) {
            Model model = initializeModel(suite);
            addTestRequirements(model, suite.getXmlSuite().getTests());
            addTestInputs(model, suite.getXmlSuite().getAllParameters());
            TestRunSummary summary = new TestRunSummary(suite);
            this.testRun.addLiteral(CITE.testsPassed, new Integer(summary.getTotalPassed()));
            this.testRun.addLiteral(CITE.testsFailed, new Integer(summary.getTotalFailed()));
            this.testRun.addLiteral(CITE.testsSkipped, new Integer(summary.getTotalSkipped()));
            Literal duration = model.createTypedLiteral(summary.getTotalDuration(),
                    XSDDatatype.XSDduration);
            this.testRun.addLiteral(DCTerms.extent, duration);
            this.testRun.addLiteral(CITE.testSuiteType, "testng");
            processSuiteResults(model, suite.getResults());
            this.earlModel.add(model);
        }
        File outputDir = new File(outputDirectory);
        if (!outputDir.isDirectory()) {
            outputDir = new File(System.getProperty("java.io.tmpdir"));
        }
        try {
            writeModel(this.earlModel, outputDir, true);
        } catch (IOException iox) {
            throw new RuntimeException(
                    "Failed to serialize EARL results to " + outputDir.getAbsolutePath(), iox);
        }
    }

    /**
     * Creates EARL statements for the entire collection of test suite results.
     * Each test subset is defined by a {@literal <test>} tag in the suite
     * definition; these correspond to earl:TestRequirement resources in the
     * model.
     * 
     * @param model
     *            An RDF Model containing EARL statements.
     * @param results
     *            A Map containing the actual test results, where the key is the
     *            name of a test subset (conformance class).
     */
    void processSuiteResults(Model model, Map<String, ISuiteResult> results) {
        for (Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
            String testReqName = entry.getKey().replaceAll("\\s", "-");
            // can return existing resource in model
            Resource testReq = model.createResource(testReqName);
            ITestContext testContext = entry.getValue().getTestContext();
            int nPassed = testContext.getPassedTests().size();
            int nSkipped = testContext.getSkippedTests().size();
            int nFailed = testContext.getFailedTests().size();
            testReq.addLiteral(CITE.testsPassed, new Integer(nPassed));
            testReq.addLiteral(CITE.testsFailed, new Integer(nFailed));
            testReq.addLiteral(CITE.testsSkipped, new Integer(nSkipped));
            if (nPassed + nFailed == 0) {
                testReq.addProperty(DCTerms.description,
                        "A precondition was not met. All tests in this set were skipped.");
            }
            processTestResults(model, testContext.getFailedTests());
            processTestResults(model, testContext.getSkippedTests());
            processTestResults(model, testContext.getPassedTests());
        }
    }

    /**
     * Initializes the test results graph with basic information about the
     * assertor (earl:Assertor) and test subject (earl:TestSubject).
     * 
     * @param suite
     *            Information about the test suite.
     * @return An RDF Model containing EARL statements.
     */
    Model initializeModel(ISuite suite) {
        Model model = ModelFactory.createDefaultModel();
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put("earl", EARL.NS_URI);
        nsBindings.put("dct", DCTerms.NS);
        nsBindings.put("cite", CITE.NS_URI);
        nsBindings.put("http", HTTP.NS_URI);
        nsBindings.put("cnt", CONTENT.NS_URI);
        model.setNsPrefixes(nsBindings);
        suiteName = suite.getName();
        this.testRun = model.createResource(CITE.TestRun);
        this.testRun.addProperty(DCTerms.title, suite.getName());
        String nowUTC = ZonedDateTime.now(ZoneId.of("Z")).format(DateTimeFormatter.ISO_INSTANT);
        this.testRun.addProperty(DCTerms.created, nowUTC);
        this.assertor = model.createResource("https://github.com/opengeospatial/teamengine",
                EARL.Assertor);
        this.assertor.addProperty(DCTerms.title, "OGC TEAM Engine", this.langCode);
        this.assertor.addProperty(DCTerms.description,
                "Official test harness of the OGC conformance testing program (CITE).",
                this.langCode);
        Map<String, String> params = suite.getXmlSuite().getAllParameters();
        String iut = params.get("iut");
        if (null == iut) {
            // non-default parameter refers to test subject--use first URI value
            for (Map.Entry<String, String> param : params.entrySet()) {
                try {
                    URI uri = URI.create(param.getValue());
                    iut = uri.toString();
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }
        if (null == iut) {
            throw new NullPointerException(
                    "Unable to find URI reference for IUT in test run parameters.");
        }
        this.testSubject = model.createResource(iut, EARL.TestSubject);
        return model;
    }

    /**
     * Adds the list of conformance classes to the TestRun resource. A
     * conformance class corresponds to a {@literal <test>} tag in the TestNG
     * suite definition; it is represented as an earl:TestRequirement resource.
     * 
     * @param earl
     *            An RDF model containing EARL statements.
     * @param testList
     *            The list of test sets comprising the test suite.
     */
    void addTestRequirements(Model earl, final List<XmlTest> testList) {
        Seq reqs = earl.createSeq();
        String key = null;
        for (XmlTest xmlTest : testList) {
            String testName = xmlTest.getName();
            for (Entry<String, List<String>> ccEntry : config.getConformanceClassMap().entrySet()) {
        		if(ccEntry.getKey().contains(suiteName)){
        			key = ccEntry.getKey();	
        		}
        	}
            Resource testReq = earl.createResource(testName.replaceAll("\\s", "-"),
                    EARL.TestRequirement);
            testReq.addProperty(DCTerms.title, testName);
            String testOptionality = xmlTest.getParameter(CITE.CC_OPTIONALITY);
            if (null != testOptionality && !testOptionality.isEmpty()) {
                testReq.addProperty(CITE.optionality, testOptionality);
            }
            if(null != key){
                List<String> ConfClass = config.getConformanceClassMap().get(key);
                for (String cClass : ConfClass) {
                	if(cClass.equalsIgnoreCase(testName)){
                		testReq.addProperty(CITE.isBasic, "true");
                	}
                }
             }
            reqs.add(testReq);
        }
        this.testRun.addProperty(CITE.requirements, reqs);
    }

    /**
     * Adds the test inputs to the TestRun resource. Each input is an anonymous
     * member of an unordered collection (rdf:Bag). A {@value #TEST_RUN_ID}
     * parameter is treated in special manner: its value is set as the value of
     * the standard dct:identifier property.
     * 
     * @param earl
     *            An RDF model containing EARL statements.
     * @param params
     *            A collection of name-value pairs gleaned from the test suite
     *            parameters.
     */
    void addTestInputs(Model earl, final Map<String, String> params) {
        Bag inputs = earl.createBag();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (param.getKey().equals(TEST_RUN_ID)) {
                this.testRun.addProperty(DCTerms.identifier, param.getValue());
            } else {
                if (param.getValue().isEmpty())
                    continue;
                Resource testInput = earl.createResource();
                testInput.addProperty(DCTerms.title, param.getKey());
                testInput.addProperty(DCTerms.description, param.getValue());
                inputs.add(testInput);
            }
        }
        this.testRun.addProperty(CITE.inputs, inputs);
    }

    /**
     * Writes the model to a file (earl-results.rdf) in the specified directory
     * using the RDF/XML syntax.
     * 
     * @param model
     *            A representation of an RDF graph.
     * @param outputDirectory
     *            A File object denoting the directory in which the results file
     *            will be written.
     * @param abbreviated
     *            Indicates whether or not to serialize the model using the
     *            abbreviated syntax.
     * @throws IOException
     *             If an IO error occurred while trying to serialize the model
     *             to a (new) file in the output directory.
     */
    void writeModel(Model model, File outputDirectory, boolean abbreviated) throws IOException {
        if (!outputDirectory.isDirectory()) {
            throw new IllegalArgumentException(
                    "Directory does not exist at " + outputDirectory.getAbsolutePath());
        }
        File outputFile = new File(outputDirectory, "earl-results.rdf");
        if (!outputFile.createNewFile()) {
            outputFile.delete();
            outputFile.createNewFile();
        }
        LOGR.log(Level.CONFIG, "Writing EARL results to" + outputFile.getAbsolutePath());
        String syntax = (abbreviated) ? "RDF/XML-ABBREV" : "RDF/XML";
        String baseUri = new StringBuilder("http://example.org/earl/")
                .append(outputDirectory.getName()).append('/').toString();
        OutputStream outStream = new FileOutputStream(outputFile);
        try (Writer writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8)) {
            model.write(writer, syntax, baseUri);
        }
    }

    /**
     * Returns a description of an error or exception that occurred while
     * executing a test. The details are extracted from the associated
     * <code>Throwable</code> or its underlying cause.
     * 
     * @param result
     *            Information about a test result.
     * @return A String providing diagnostic information.
     */
    String getDetailMessage(ITestResult result) {
        if (null == result.getThrowable()) {
            return "No details available.";
        }
        String msg = result.getThrowable().getMessage();
        if (null == msg && null != result.getThrowable().getCause()) {
            msg = result.getThrowable().getCause().getMessage();
        } else {
            msg = result.getThrowable().toString();
        }
        return msg;
    }

    /**
     * Creates EARL statements from the given test results. A test result is
     * described by an Assertion resource. The TestResult and TestCase resources
     * are linked to the Assertion in accord with the EARL schema; the latter is
     * also linked to a TestRequirement.
     * 
     * @param earl
     *            An RDF Model containing EARL statements.
     * @param results
     *            The results of invoking a collection of test methods.
     */
    void processTestResults(Model earl, IResultMap results) {
        for (ITestResult tngResult : results.getAllResults()) {
            // create earl:Assertion
            long endTime = tngResult.getEndMillis();
            GregorianCalendar calTime = new GregorianCalendar(TimeZone.getDefault());
            calTime.setTimeInMillis(endTime);
            Resource assertion = earl.createResource("assert-" + ++this.resultCount,
                    EARL.Assertion);
            assertion.addProperty(EARL.mode, EARL.AutomaticMode);
            assertion.addProperty(EARL.assertedBy, this.assertor);
            assertion.addProperty(EARL.subject, this.testSubject);
            // link earl:TestResult to earl:Assertion
            Resource earlResult = earl.createResource("result-" + this.resultCount,
                    EARL.TestResult);
            earlResult.addProperty(DCTerms.date, earl.createTypedLiteral(calTime));
            switch (tngResult.getStatus()) {
            case ITestResult.FAILURE:
                earlResult.addProperty(DCTerms.description, getDetailMessage(tngResult));
                if (AssertionError.class.isInstance(tngResult.getThrowable())) {
                    earlResult.addProperty(EARL.outcome, EARL.Fail);
                } else { // an exception occurred
                    earlResult.addProperty(EARL.outcome, EARL.CannotTell);
                }
                processResultAttributes(earlResult, tngResult);
                break;
            case ITestResult.SKIP:
                earlResult.addProperty(DCTerms.description, getDetailMessage(tngResult));
                earlResult.addProperty(EARL.outcome, EARL.NotTested);
                break;
            default:
                earlResult.addProperty(EARL.outcome, EARL.Pass);
                break;
            }
            assertion.addProperty(EARL.result, earlResult);
            // link earl:TestCase to earl:Assertion and earl:TestRequirement
            String testMethodName = tngResult.getMethod().getMethodName();
            String testClassName = tngResult.getTestClass().getName().replaceAll("\\.", "/");
            StringBuilder testCaseId = new StringBuilder(testClassName);
            testCaseId.append('#').append(testMethodName);
            Resource testCase = earl.createResource(testCaseId.toString(), EARL.TestCase);
            testCase.addProperty(DCTerms.title, breakIntoWords(testMethodName));
            String testDescr = tngResult.getMethod().getDescription();
            if (null != testDescr && !testDescr.isEmpty()) {
                testCase.addProperty(DCTerms.description, testDescr);
            }
            assertion.addProperty(EARL.test, testCase);
            String testReqName = tngResult.getTestContext().getName().replaceAll("\\s", "-");
            earl.createResource(testReqName).addProperty(DCTerms.hasPart, testCase);
        }
    }

    /**
     * Processes any attributes that were attached to a test result. Attributes
     * should describe relevant test events in order to help identify the root
     * cause of a fail verdict. Specifically, the following statements are added
     * to the report:
     * <ul>
     * <li>{@value #REQ_ATTR} : Information about the request message
     * (earl:TestResult --cite:message-- http:Request)</li>
     * <li>{@value #RSP_ATTR} : Information about the response message
     * (http:Request --http:resp-- http:Response)</li>
     * </ul>
     * 
     * @param earlResult
     *            An earl:TestResult resource.
     * @param tngResult
     *            The TestNG test result.
     */
    void processResultAttributes(Resource earlResult, final ITestResult tngResult) {
        if (!tngResult.getAttributeNames().contains(REQ_ATTR))
            return;
        // keep it simple for now
        String reqVal = tngResult.getAttribute(REQ_ATTR).toString();
        String httpMethod = (reqVal.startsWith("<")) ? HttpMethod.POST : HttpMethod.GET;
        Resource httpReq = this.earlModel.createResource(HTTP.Request);
        httpReq.addProperty(HTTP.methodName, httpMethod);
        if (httpMethod.equals(HttpMethod.GET)) {
            httpReq.addProperty(HTTP.requestURI, reqVal);
        } else {
        	httpReq.addProperty(HTTP.requestURI, reqVal);
            Resource reqContent = this.earlModel.createResource(CONTENT.ContentAsXML);
            // XML content may be truncated and hence not well-formed
            reqContent.addProperty(CONTENT.rest, reqVal);
            httpReq.addProperty(HTTP.body, reqContent);
        }
        Object rsp = tngResult.getAttribute(RSP_ATTR);
        if (null != rsp) {
            Resource httpRsp = this.earlModel.createResource(HTTP.Response);
            // safe assumption, but need more response info to know for sure
            Resource rspContent = this.earlModel.createResource(CONTENT.ContentAsXML);
            rspContent.addProperty(CONTENT.rest, rsp.toString());
            httpRsp.addProperty(HTTP.body, rspContent);
            httpReq.addProperty(HTTP.resp, httpRsp);
        }
        earlResult.addProperty(CITE.message, httpReq);
    }
    
    /*
     * This method is used to correct the mangled name issue.
     * 	e.g. 'compileXMLSchema' ===> 'compile XML Schema'
     *  
     * @param testName
     *           Name of the test method. 
     */
    public String breakIntoWords(String testName) {
		String updateTestName = "";
		String[] id = { "_1_", "_2_", "_3_", "_4_", "_5_", "_6_", "_7_", "_8_",
				"_9_" };
		String[] name = { "XML", "XSD", "GML", "CRS", "(GET)", "(POST)",
				"BBOX", "URI", "WFS" };

		for (int i = 0; i < id.length; i++) {
			if (testName.indexOf(name[i]) > -1) {
				testName = testName.replace(name[i], " " + id[i]);
			}
		}
		String[] temp = testName.split("(?=\\p{Upper})");

		for (int index = 0; index < temp.length; index++) {
			updateTestName = updateTestName + temp[index] + " ";
		}

		for (int index = 0; index < id.length; index++) {
			if (updateTestName.indexOf(id[index]) > -1) {
				updateTestName = updateTestName.replace(id[index], name[index]);
			}
		}
		return updateTestName;
	}
}
