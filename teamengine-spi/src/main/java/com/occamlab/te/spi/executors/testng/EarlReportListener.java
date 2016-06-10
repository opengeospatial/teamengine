package com.occamlab.te.spi.executors.testng;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;

import com.occamlab.te.spi.vocabulary.EARL;

/**
 * A TestListener that keeps track of tests as they are run and constructs the
 * EARL RDF model expressing the results.
 *
 * @see <a href="https://www.w3.org/TR/EARL10-Schema/" target="_blank">
 *      Evaluation and Report Language (EARL) 1.0 Schema</a>
 */
public class EarlReportListener extends TestListenerAdapter {

    private static final Logger LOGR = Logger.getLogger(EarlReportListener.class.getPackage().getName());
    private Model testResults;
    /** ISO 639 language code (2-3 letter, possibly with region subtag). */
    private String langCode = "en";

    /**
     * 
     * @see org.testng.TestListenerAdapter#onStart(org.testng.ITestContext)
     */
    @Override
    public void onStart(ITestContext testRunContext) {
        super.onStart(testRunContext);
        this.testResults = initModel(testRunContext);
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
        Resource assertor = model.createResource("https://github.com/opengeospatial/teamengine", EARL.Assertor);
        assertor.addProperty(DCTerms.title, "OGC TEAM Engine", this.langCode);
        assertor.addProperty(DCTerms.description,
                "Official test harness of the OGC conformance testing program (CITE).", this.langCode);
        // may differ from default parameter that refers to test subject
        model.createResource(params.get("iut"), EARL.TestSubject);
        return model;
    }

}
