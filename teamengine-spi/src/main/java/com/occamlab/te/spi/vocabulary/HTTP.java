package com.occamlab.te.spi.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Provides an RDF vocabulary to describe HTTP request and response messages.
 * Such information can help to diagnose the cause of test failures.
 *
 * @see <a href="https://www.w3.org/TR/HTTP-in-RDF10/" target="_blank">HTTP
 *      Vocabulary in RDF 1.0</a>
 */
public class HTTP {

    /** Model that holds the HTTP vocabulary terms. */
    private static final Model model = ModelFactory.createDefaultModel();
    public static final String NS_URI = "http://www.w3.org/2011/http#";
    // HTTP classes
    public static final Resource Request = model.createResource(NS_URI + "Request");
    public static final Resource Response = model.createResource(NS_URI + "Response");
    // General message properties
    public static final Property httpVersion = model.createProperty(NS_URI + "httpVersion");
    public static final Property headers = model.createProperty(NS_URI + "headers");
    public static final Property body = model.createProperty(NS_URI + "body");
    // Request message properties
    public static final Property methodName = model.createProperty(NS_URI + "methodName");
    public static final Property requestURI = model.createProperty(NS_URI + "requestURI");
    public static final Property resp = model.createProperty(NS_URI + "resp");
    // Response message properties
    public static final Property statusCodeValue = model.createProperty(NS_URI + "statusCodeValue");
    public static final Property reasonPhrase = model.createProperty(NS_URI + "reasonPhrase");
}
