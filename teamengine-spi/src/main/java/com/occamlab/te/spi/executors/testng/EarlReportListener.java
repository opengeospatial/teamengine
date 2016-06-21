package com.occamlab.te.spi.executors.testng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.occamlab.te.spi.vocabulary.EARL;

/**
 * A TestListener that keeps track of tests as they are run and constructs an
 * EARL RDF model representing the results.
 *
 * @see <a href="https://www.w3.org/TR/EARL10-Schema/" target="_blank">
 *      Evaluation and Report Language (EARL) 1.0 Schema</a>
 */
public class EarlReportListener extends TestListenerAdapter {

    private static final Logger LOGR = Logger.getLogger(EarlReportListener.class.getPackage().getName());
    private Model earlModel;
    /** ISO 639 language code (2-3 letter, possibly with region subtag). */
    private String langCode = "en";
    private Resource assertor;
    private Resource testSubject;

    /**
     * Invoked when the test run starts. The RDF model is initialized.
     * 
     * @see org.testng.TestListenerAdapter#onStart(org.testng.ITestContext)
     */
    @Override
    public void onStart(ITestContext testRunContext) {
        super.onStart(testRunContext);
        this.earlModel = initModel(testRunContext);
    }

    /**
     * Invoked when the test run has finished. The resulting RDF model is
     * written to the configured output directory; if this directory does not
     * exist, the model is written to the directory specified by the system
     * property <code>java.io.tmpdir</code>.
     * 
     * @see org.testng.TestListenerAdapter#onFinish(org.testng.ITestContext)
     */
    @Override
    public void onFinish(ITestContext testContext) {
        super.onFinish(testContext);
        // SuiteRunner appends suite name to path on read
        File outputDir = new File(testContext.getOutputDirectory()).getParentFile();
        if (!outputDir.isDirectory()) {
            outputDir = new File(System.getProperty("java.io.tmpdir"));
        }
        try {
            writeModel(this.earlModel, outputDir, true);
        } catch (IOException iox) {
            throw new RuntimeException("Failed to serialize model to " + outputDir.getAbsolutePath(), iox);
        }
    }

    /**
     * Invoked when a test method passed.
     * 
     * @see org.testng.TestListenerAdapter#onTestSuccess(org.testng.ITestResult)
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        super.onTestSuccess(result);
        onTestFinish(result);
    }

    /**
     * Invoked when a test method failed.
     * 
     * @see org.testng.TestListenerAdapter#onTestFailure(org.testng.ITestResult)
     */
    @Override
    public void onTestFailure(ITestResult result) {
        super.onTestFailure(result);
        onTestFinish(result);
    }

    /**
     * Invoked when a test method was skipped.
     * 
     * @see org.testng.TestListenerAdapter#onTestSkipped(org.testng.ITestResult)
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        super.onTestSkipped(result);
        onTestFinish(result);
    }

    /**
     * Invoked when a test method has finished. The actual verdict is one of:
     * pass, fail, or skip. An earl:Assertion statement that describes the test
     * result is added to the model.
     * 
     * @param result
     *            Information about the test result.
     */
    void onTestFinish(ITestResult result) {
        // earl:Assertion
        long endTime = result.getEndMillis();
        GregorianCalendar calTime = new GregorianCalendar(TimeZone.getDefault());
        calTime.setTimeInMillis(endTime);
        Resource assertion = this.earlModel.createResource("assert-" + endTime, EARL.Assertion);
        assertion.addProperty(EARL.mode, EARL.Automatic);
        assertion.addProperty(EARL.assertedBy, this.assertor);
        assertion.addProperty(EARL.subject, this.testSubject);
        // earl:TestResult
        Resource earlResult = this.earlModel.createResource("result-" + endTime, EARL.TestResult);
        earlResult.addProperty(DCTerms.date, this.earlModel.createTypedLiteral(calTime));
        switch (result.getStatus()) {
        case ITestResult.FAILURE:
            earlResult.addProperty(DCTerms.description, result.getThrowable().getMessage());
            if (AssertionError.class.isInstance(result.getThrowable())) {
                earlResult.addProperty(EARL.outcome, EARL.Failed);
            } else { // an exception occurred
                earlResult.addProperty(EARL.outcome, EARL.Inconclusive);
            }
            break;
        case ITestResult.SKIP:
            earlResult.addProperty(DCTerms.description, result.getThrowable().getMessage());
            earlResult.addProperty(EARL.outcome, EARL.Untested);
            break;
        default:
            earlResult.addProperty(EARL.outcome, EARL.Passed);
            break;
        }
        assertion.addProperty(EARL.result, earlResult);
        // earl:TestCase
        String testMethodName = result.getMethod().getMethodName();
        String testClassName = result.getTestClass().getName().replaceAll("\\.", "/");
        StringBuilder testCaseId = new StringBuilder(testClassName);
        testCaseId.append('#').append(testMethodName);
        Resource testCase = this.earlModel.createResource(testCaseId.toString(), EARL.TestCase);
        testCase.addProperty(DCTerms.title, testMethodName);
        String testDescr = result.getMethod().getDescription();
        if (null != testDescr && !testDescr.isEmpty()) {
            testCase.addProperty(DCTerms.description, testDescr);
        }
        // earl:TestRequirement (conformance class/level)
        String xmlTestName = result.getTestClass().getXmlTest().getName();
        Resource testReq = this.earlModel.createResource(xmlTestName.replaceAll("\\s", "-"), EARL.TestRequirement);
        testReq.addProperty(DCTerms.title, xmlTestName);
        testCase.addProperty(DCTerms.isPartOf, testReq);
        assertion.addProperty(EARL.test, testCase);
    }

    /**
     * Initializes the test results with basic information about the assertor
     * (earl:Assertor) and test subject (earl:TestSubject).
     * 
     * @param testRunContext
     *            Information about the test run.
     * @return An RDF Model containing EARL statements.
     */
    Model initModel(ITestContext testRunContext) {
        Map<String, String> params = testRunContext.getSuite().getXmlSuite().getAllParameters();
        LOGR.log(Level.FINE, "Test run parameters\n:" + params);
        Model model = ModelFactory.createDefaultModel();
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put("earl", EARL.NS_URI);
        nsBindings.put("dct", DCTerms.NS);
        model.setNsPrefixes(nsBindings);
        this.assertor = model.createResource("https://github.com/opengeospatial/teamengine", EARL.Assertor);
        this.assertor.addProperty(DCTerms.title, "OGC TEAM Engine", this.langCode);
        this.assertor.addProperty(DCTerms.description,
                "Official test harness of the OGC conformance testing program (CITE).", this.langCode);
        // WARNING: may differ from actual parameter that refers to test subject
        this.testSubject = model.createResource(params.get("iut"), EARL.TestSubject);
        return model;
    }

    /**
     * Writes the model to a file (earl.rdf) in the specified directory using
     * the RDF/XML syntax.
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
            throw new IllegalArgumentException("Directory does not exist at " + outputDirectory.getAbsolutePath());
        }
        File outputFile = new File(outputDirectory, "earl.rdf");
        if (!outputFile.createNewFile()) {
            outputFile.delete();
            outputFile.createNewFile();
        }
        String syntax = (abbreviated) ? "RDF/XML-ABBREV" : "RDF/XML";
        String baseUri = new StringBuilder("http://example.org/earl/").append(outputDirectory.getName()).append('/')
                .toString();
        OutputStream outStream = new FileOutputStream(outputFile);
        try (Writer writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8)) {
            model.write(writer, syntax, baseUri);
        }
    }

}
