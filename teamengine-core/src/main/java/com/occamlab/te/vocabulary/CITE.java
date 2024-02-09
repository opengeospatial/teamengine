/**********************************************************
 *
 *  Contributor(s):
 *      C. Heazel (WiSC): Moved vocabulary package from spi to core
 *
 */
package com.occamlab.te.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

/**
 * Additional vocabulary terms that are used to describe a test run.
 */
public class CITE {

	/**
	 * A TestNG test parameter (test/parameter) that indicates the optionality of a
	 * conformance class in a conformity assessment. At least one is usually deemed to be
	 * mandatory.
	 */
	public static final String CC_OPTIONALITY = "optionality";

	/** Model that holds the CITE vocabulary terms. */
	private static final Model model = ModelFactory.createDefaultModel();

	public static final String NS_URI = "http://cite.opengeospatial.org/";

	public static final Resource NAMESPACE = model.createResource(NS_URI);

	public static final Resource TestRun = model.createResource(NS_URI + "TestRun");

	public static final Property testsFailed = model.createProperty(NS_URI + "testsFailed");

	public static final Property testsPassed = model.createProperty(NS_URI + "testsPassed");

	public static final Property testsSkipped = model.createProperty(NS_URI + "testsSkipped");

	public static final Property testsContinue = model.createProperty(NS_URI + "testsContinue");

	public static final Property testsBestPractice = model.createProperty(NS_URI + "testsBestPractice");

	public static final Property testsNotTested = model.createProperty(NS_URI + "testsNotTested");

	public static final Property testsWarning = model.createProperty(NS_URI + "testsWarning");

	public static final Property testsInheritedFailure = model.createProperty(NS_URI + "testsInheritedFailure");

	public static final Property isBasic = model.createProperty(NS_URI + "isBasic");

	/** [cite:TestRun] requirements [earl:TestRequirement] 1..* (rdf:Seq) */
	public static final Property requirements = model.createProperty(NS_URI + "requirements");

	/** [cite:TestRun] inputs [rdf:Description] 1..* (rdf:Bag) */
	public static final Property inputs = model.createProperty(NS_URI + "inputs");

	/** [earl:TestResult] message [http:Request] */
	public static final Property message = model.createProperty(NS_URI + "message");

	/** [earl:Assertion] arguments [rdf:Description] 1..* (rdf:Seq) */
	public static final Property arguments = model.createProperty(NS_URI + "arguments");

	/**
	 * Property of a conformance class (earl:TestRequirement). The following values
	 * indicate its significance with respect to a conformity assessment:
	 * <ul>
	 * <li>mandatory</li>
	 * <li>recommended</li>
	 * <li>optional (default)</li>
	 * </ul>
	 */
	public static final Property optionality = model.createProperty(NS_URI + CC_OPTIONALITY);

	// CTL test verdicts
	public static final Resource Best_Practice = model.createResource(NS_URI + "earl#bestPractice")
		.addProperty(RDF.type, EARL.OutcomeValue);

	public static final Resource Warning = model.createResource(NS_URI + "earl#warning")
		.addProperty(RDF.type, EARL.OutcomeValue);

	public static final Resource Continue = model.createResource(NS_URI + "earl#continue")
		.addProperty(RDF.type, EARL.OutcomeValue);

	public static final Resource Not_Tested = model.createResource(NS_URI + "earl#notTested")
		.addProperty(RDF.type, EARL.OutcomeValue);

	public static final Resource Inherited_Failure = model.createResource(NS_URI + "earl#inheritedFailure")
		.addProperty(RDF.type, EARL.OutcomeValue);

	// Indicate the test suite type i.e. CTL or TestNG based.
	public static final Property testSuiteType = model.createProperty(NS_URI + "testSuiteType");

	// Indicate the implementations passing these core conformance classes can be
	// certified or not.
	public static final Property areCoreConformanceClassesPassed = model
		.createProperty(NS_URI + "areCoreConformanceClassesPassed");

	/** webDirPath */
	public static final Property webDirPath = model.createProperty(NS_URI + "webDirPath");

}
