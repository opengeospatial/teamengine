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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Seq;
import org.apache.jena.vocabulary.DCTerms;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlTest;

import com.occamlab.te.spi.vocabulary.CITE;
import com.occamlab.te.spi.vocabulary.EARL;

/**
 * A listener that creates and serializes an RDF graph containing the test
 * results expressed using the W3C Evaluation and Report Language (EARL)
 * vocabulary. The initial graph is set as the value of the suite attribute
 * "earl"; it is subsequently augmented by an {@link EarlTestListener} during
 * the test run.
 * 
 * When the test run has finished, the graph is serialized as RDF/XML to a file
 * (earl.rdf) in the output directory.
 */
public class EarlSuiteListener implements ISuiteListener {

    private static final Logger LOGR = Logger.getLogger(EarlSuiteListener.class.getPackage().getName());
    /** ISO 639 language code (2-3 letter, possibly with region subtag). */
    private String langCode = "en";
    private static final String TEST_RUN_ID = "uuid";
    private Resource testRun;

    @Override
    public void onStart(ISuite suite) {
        Model model = initModel(suite);
        addTestRequirements(model, suite.getXmlSuite().getTests());
        suite.setAttribute("earl", model);
    }

    @Override
    public void onFinish(ISuite suite) {
        Object obj = suite.getAttribute("earl");
        if (null == obj) {
            LOGR.warning("RDF model not obtained using suite attribute \"earl\"");
            return;
        }
        Model model = Model.class.cast(obj);
        addTestInputs(model, suite.getXmlSuite().getAllParameters());
        TestRunSummary summary = new TestRunSummary(suite);
        this.testRun.addLiteral(CITE.testsPassed, new Integer(summary.getTotalPassed()));
        this.testRun.addLiteral(CITE.testsFailed, new Integer(summary.getTotalFailed()));
        this.testRun.addLiteral(CITE.testsSkipped, new Integer(summary.getTotalSkipped()));
        Literal duration = model.createTypedLiteral(summary.getTotalDuration(), XSDDatatype.XSDduration);
        this.testRun.addLiteral(DCTerms.extent, duration);
        // SuiteRunner appends suite name to path on read
        File outputDir = new File(suite.getOutputDirectory()).getParentFile();
        if (!outputDir.isDirectory()) {
            outputDir = new File(System.getProperty("java.io.tmpdir"));
        }
        try {
            writeModel(model, outputDir, true);
        } catch (IOException iox) {
            throw new RuntimeException("Failed to serialize EARL results to " + outputDir.getAbsolutePath(), iox);
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
    Model initModel(ISuite suite) {
        Map<String, String> params = suite.getXmlSuite().getAllParameters();
        LOGR.log(Level.FINE, "Test run parameters\n:" + params);
        Model model = ModelFactory.createDefaultModel();
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put("earl", EARL.NS_URI);
        nsBindings.put("dct", DCTerms.NS);
        nsBindings.put("cite", CITE.NS_URI);
        model.setNsPrefixes(nsBindings);
        this.testRun = model.createResource(CITE.TestRun);
        this.testRun.addProperty(DCTerms.title, suite.getName());
        String nowUTC = ZonedDateTime.now(ZoneId.of("Z")).format(DateTimeFormatter.ISO_INSTANT);
        this.testRun.addProperty(DCTerms.created, nowUTC);
        Resource assertor = model.createResource("https://github.com/opengeospatial/teamengine", EARL.Assertor);
        assertor.addProperty(DCTerms.title, "OGC TEAM Engine", this.langCode);
        assertor.addProperty(DCTerms.description,
                "Official test harness of the OGC conformance testing program (CITE).", this.langCode);
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
            throw new NullPointerException("Unable to find URI reference for IUT in test run parameters.");
        }
        model.createResource(iut, EARL.TestSubject);
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
        LOGR.log(Level.CONFIG, "Writing EARL results to" + outputFile.getAbsolutePath());
        String syntax = (abbreviated) ? "RDF/XML-ABBREV" : "RDF/XML";
        String baseUri = new StringBuilder("http://example.org/earl/").append(outputDirectory.getName()).append('/')
                .toString();
        OutputStream outStream = new FileOutputStream(outputFile);
        try (Writer writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8)) {
            model.write(writer, syntax, baseUri);
        }
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
        for (XmlTest xmlTest : testList) {
            String testName = xmlTest.getName();
            Resource testRequirement = earl.createResource(testName.replaceAll("\\s", "-"), EARL.TestRequirement);
            testRequirement.addProperty(DCTerms.title, testName);
            reqs.add(testRequirement);
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
}
