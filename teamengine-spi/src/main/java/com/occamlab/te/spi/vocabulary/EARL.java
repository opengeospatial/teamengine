package com.occamlab.te.spi.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * Terms constituting the W3C EARL 1.0 vocabulary (W3C Working Draft, 10 May
 * 2011).
 *
 * @see <a href="https://www.w3.org/TR/EARL10-Schema/" target="_blank">
 *      Evaluation and Report Language (EARL) 1.0 Schema</a>
 */
public class EARL {

    /** Model that holds the EARL vocabulary terms. */
    private static final Model model = ModelFactory.createDefaultModel();
    /** EARL namespace name. */
    public static final String NS_URI = "http://www.w3.org/ns/earl#";
    /** Namespace of the EARL vocabulary. */
    public static final Resource NAMESPACE = model.createResource(NS_URI);
    /* EARL classes */
    public static final Resource Assertion = model.createResource(NS_URI + "Assertion");
    public static final Resource Assertor = model.createResource(NS_URI + "Assertor");
    public static final Resource TestSubject = model.createResource(NS_URI + "TestSubject");
    public static final Resource TestCriterion = model.createResource(NS_URI + "TestCriterion");
    public static final Resource TestRequirement = model.createResource(NS_URI + "TestRequirement");
    public static final Resource TestCase = model.createResource(NS_URI + "TestCase");
    public static final Resource TestResult = model.createResource(NS_URI + "TestResult");
    public static final Resource TestMode = model.createResource(NS_URI + "TestMode");
    public static final Resource OutcomeValue = model.createResource(NS_URI + "OutcomeValue");
}
