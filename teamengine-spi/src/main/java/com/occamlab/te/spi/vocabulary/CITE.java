package com.occamlab.te.spi.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Additional vocabulary terms that are used to describe a test run.
 */
public class CITE {

    /** Model that holds the CITE vocabulary terms. */
    private static final Model model = ModelFactory.createDefaultModel();
    public static final String NS_URI = "http://cite.opengeospatial.org/";
    public static final Resource NAMESPACE = model.createResource(NS_URI);
    public static final Resource TestRun = model.createResource(NS_URI + "TestRun");
    public static final Property testsFailed = model.createProperty(NS_URI + "testsFailed");
    public static final Property testsPassed = model.createProperty(NS_URI + "testsPassed");
    public static final Property testsSkipped = model.createProperty(NS_URI + "testsSkipped");
    /** [cite:TestRun] requirements [earl:TestRequirement] 1..* (rdf:Seq) */
    public static final Property requirements = model.createProperty(NS_URI + "requirements");
    /** [cite:TestRun] inputs [rdf:Description] 1..* (rdf:Bag) */
    public static final Property inputs = model.createProperty(NS_URI + "inputs");
}
