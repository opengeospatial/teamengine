package com.occamlab.te.spi.executors.testng;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
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
public class EarlTestListener extends TestListenerAdapter {

    private static final Logger LOGR = Logger.getLogger(EarlTestListener.class.getPackage().getName());
    private Model earlModel;
    private int resultCount = 0;
    private Resource assertor;
    private Resource testSubject;

    /**
     * Invoked when a test set (&lt;test&gt;) starts. This typically corresponds
     * to a conformance class.
     * 
     * @see org.testng.TestListenerAdapter#onStart(org.testng.ITestContext)
     */
    @Override
    public void onStart(ITestContext testRunContext) {
        super.onStart(testRunContext);
        Object obj = testRunContext.getSuite().getAttribute("earl");
        if (null == obj) {
            throw new NullPointerException("RDF model not obtained using suite attribute \"earl\"");
        }
        this.earlModel = Model.class.cast(obj);
        this.assertor = earlModel.listSubjectsWithProperty(RDF.type, EARL.Assertor).next();
        this.testSubject = earlModel.listSubjectsWithProperty(RDF.type, EARL.TestSubject).next();
    }

    /**
     * Invoked when a test set has finished. The model is augmented with some
     * summary information about the results for the set (conformance class)
     * just completed.
     * 
     * @see org.testng.TestListenerAdapter#onFinish(org.testng.ITestContext)
     */
    @Override
    public void onFinish(ITestContext testContext) {
        super.onFinish(testContext);
        // TODO: Write summary (success rate)
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
        LOGR.fine("Finished test method " + result.getMethod().getMethodName());
        this.resultCount += 1;
        // earl:Assertion
        long endTime = result.getEndMillis();
        GregorianCalendar calTime = new GregorianCalendar(TimeZone.getDefault());
        calTime.setTimeInMillis(endTime);
        Resource assertion = this.earlModel.createResource("assert-" + this.resultCount, EARL.Assertion);
        assertion.addProperty(EARL.mode, EARL.Automatic);
        assertion.addProperty(EARL.assertedBy, this.assertor);
        assertion.addProperty(EARL.subject, this.testSubject);
        // earl:TestResult
        Resource earlResult = this.earlModel.createResource("result-" + this.resultCount, EARL.TestResult);
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

}
