package com.occamlab.te.spi.executors.testng;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;

import com.occamlab.te.spi.vocabulary.EARL;

/**
 * A TestListener that keeps track of tests as they are run and constructs the
 * EARL RDF model expressing the results as the test suite is executed.
 *
 * @see <a href="https://www.w3.org/TR/EARL10-Schema/" target="_blank">
 *      Evaluation and Report Language (EARL) 1.0 Schema</a>
 */
public class EarlReportListener extends TestListenerAdapter {

    private static final Logger LOGR = Logger.getLogger(EarlReportListener.class.getPackage().getName());
    private Model testResults;

    /**
     * 
     * @see org.testng.TestListenerAdapter#onStart(org.testng.ITestContext)
     */
    @Override
    public void onStart(ITestContext testContext) {
        super.onStart(testContext);
        this.testResults = initModel(testContext);
    }

    Model initModel(ITestContext testContext) {
        Model model = ModelFactory.createDefaultModel();
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put("earl", EARL.NS_URI);
        model.setNsPrefixes(nsBindings);
        // TODO
        return model;
    }

}
