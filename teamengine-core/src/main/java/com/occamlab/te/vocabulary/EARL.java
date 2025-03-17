/**************************************************************
 *
 * Contributor(s):
 *     C. Heazel (WiSC): Moved vocabulary package from spi to core
 *
 **************************************************************/
package com.occamlab.te.vocabulary;

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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

/**
 * Terms constituting the W3C EARL 1.0 vocabulary (W3C Working Draft, 10 May 2011).
 *
 * @see <a href="https://www.w3.org/TR/EARL10-Schema/" target="_blank"> Evaluation and
 * Report Language (EARL) 1.0 Schema</a>
 */
public class EARL {

	/** Model that holds the EARL vocabulary terms. */
	private static final Model model = ModelFactory.createDefaultModel();

	/** EARL namespace name. */
	public static final String NS_URI = "http://www.w3.org/ns/earl#";

	/** Namespace of the EARL vocabulary. */
	public static final Resource NAMESPACE = model.createResource(NS_URI);

	// EARL classes
	public static final Resource Assertion = model.createResource(NS_URI + "Assertion");

	public static final Resource Assertor = model.createResource(NS_URI + "Assertor");

	public static final Resource TestSubject = model.createResource(NS_URI + "TestSubject");

	public static final Resource TestCriterion = model.createResource(NS_URI + "TestCriterion");

	public static final Resource TestRequirement = model.createResource(NS_URI + "TestRequirement");

	public static final Resource TestCase = model.createResource(NS_URI + "TestCase");

	public static final Resource TestResult = model.createResource(NS_URI + "TestResult");

	public static final Resource TestMode = model.createResource(NS_URI + "TestMode");

	public static final Resource OutcomeValue = model.createResource(NS_URI + "OutcomeValue");

	// EARL properties
	public static final Property assertedBy = model.createProperty(NS_URI + "assertedBy");

	public static final Property subject = model.createProperty(NS_URI + "subject");

	public static final Property test = model.createProperty(NS_URI + "test");

	public static final Property result = model.createProperty(NS_URI + "result");

	public static final Property mode = model.createProperty(NS_URI + "mode");

	public static final Property mainAssertor = model.createProperty(NS_URI + "mainAssertor");

	public static final Property outcome = model.createProperty(NS_URI + "outcome");

	public static final Property pointer = model.createProperty(NS_URI + "pointer");

	public static final Property info = model.createProperty(NS_URI + "info");

	// EARL instances
	public static final Resource AutomaticMode = model.createResource(NS_URI + "automatic")
		.addProperty(RDF.type, TestMode)
		.addProperty(DCTerms.title, "Automatic");

	public static final Resource Pass = model.createResource(NS_URI + "passed")
		.addProperty(RDF.type, OutcomeValue)
		.addProperty(DCTerms.title, "Pass");

	public static final Resource Fail = model.createResource(NS_URI + "failed")
		.addProperty(RDF.type, OutcomeValue)
		.addProperty(DCTerms.title, "Fail");

	public static final Resource CannotTell = model.createResource(NS_URI + "cantTell")
		.addProperty(RDF.type, OutcomeValue)
		.addProperty(DCTerms.title, "Undetermined");

	public static final Resource NotTested = model.createResource(NS_URI + "untested")
		.addProperty(RDF.type, OutcomeValue)
		.addProperty(DCTerms.title, "Not tested");

	public static final Resource NotApplicable = model.createResource(NS_URI + "inapplicable")
		.addProperty(RDF.type, OutcomeValue)
		.addProperty(DCTerms.title, "Not applicable");

}
