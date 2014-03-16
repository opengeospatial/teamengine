package com.occamlab.te.spi.executors.testng;

import com.occamlab.te.spi.executors.TestRunExecutor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import org.testng.TestNG;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * Executes a TestNG test suite using the given test run arguments.
 */
public class TestNGExecutor implements TestRunExecutor {

    private static final Logger LOGR = Logger.getLogger(TestNGExecutor.class
            .getPackage().getName());
    private boolean useDefaultListeners;
    private File outputDir;
    private URI testngConfig;

    /**
     * Constructs a TestNG executor with the given test suite definition. The
     * default listeners are <strong>not</strong> used.
     * 
     * @param testngSuite
     *            A reference to a file containing a TestNG suite definition
     *            (with &lt;suite&gt; as the document element).
     */
    public TestNGExecutor(String testngSuite) {
        this(testngSuite, System.getProperty("java.io.tmpdir"), false);
    }

    /**
     * Constructs a TestNG executor configured as indicated.
     * 
     * @param testngSuite
     *            A reference to a file containing a TestNG suite definition.
     * @param outputDirPath
     *            The location of the root directory for writing test results.
     *            If the directory does not exist and cannot be created, the
     *            location given by the "java.io.tmpdir" system property is used
     *            instead.
     * @param useDefaultListeners
     *            A boolean value indicating whether or not to use the default
     *            set of listeners.
     */
    public TestNGExecutor(String testngSuite, String outputDirPath,
            boolean useDefaultListeners) {
        this.useDefaultListeners = useDefaultListeners;
        this.outputDir = new File(outputDirPath, "testng");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            this.outputDir = new File(System.getProperty("java.io.tmpdir"));
        }
        if (null != testngSuite && !testngSuite.isEmpty()) {
            this.testngConfig = URI.create(testngSuite);
        }
    }

    /**
     * Executes a test suite using the supplied test run arguments. The test run
     * arguments are expected to be contained in an XML properties document
     * structured as shown in the following example.
     * 
     * <pre>
     * <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
     * <properties version="1.0">
     *   <comment>Test run arguments</comment>
     *   <entry key="uri">atom-feed.xml</entry>
     *   <entry key="classes">L2</entry>
     * </properties>
     * </pre>
     * 
     * <p>
     * <strong>Note:</strong>The actual arguments (key-value pairs) are
     * suite-specific.
     * </p>
     * 
     * @param testRunArgs
     *            A DOM Document node that contains a set of XML properties.
     * @return A Source object that provides an XML representation of the test
     *         results.
     */
    @Override
    public Source execute(Document testRunArgs) {
        if (null == testRunArgs) {
            throw new IllegalArgumentException(
                    "No test run arguments were supplied.");
        }
        TestNG driver = new TestNG();
        setTestSuites(driver, testngConfig);
        driver.setVerbose(0);
        driver.setUseDefaultListeners(this.useDefaultListeners);
        UUID runId = UUID.randomUUID();
        File runDir = new File(this.outputDir, runId.toString());
        if (!runDir.mkdir()) {
            runDir = this.outputDir;
        }
        driver.setOutputDirectory(runDir.getAbsolutePath());
        driver.addListener(new PrimarySuiteListener(testRunArgs));
        driver.run();
        Document resultsDoc = null;
        try {
            resultsDoc = parseResultsDoc(driver.getOutputDirectory());
        } catch (Exception ex) {
            LOGR.log(Level.SEVERE, "Failed to parse test results.", ex);
        }
        return new DOMSource(resultsDoc, resultsDoc.getDocumentURI());
    }

    Document parseResultsDoc(String outputDirectory)
            throws ParserConfigurationException, SAXException, IOException {
        File results = new File(outputDirectory, "testng-results.xml");
        Document resultsDoc;
        if (results.isFile()) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            resultsDoc = db.parse(results);
            resultsDoc.setDocumentURI(results.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Test results not found");
        }
        return resultsDoc;
    }

    /**
     * Sets the test suite to run using the given URI reference. Three types of
     * references are supported:
     * <ul>
     * <li>A file system reference</li>
     * <li>A file: URI</li>
     * <li>A jar: URI</li>
     * </ul>
     * 
     * @param driver
     *            The main TestNG driver.
     * @param ets
     *            A URI referring to a suite definition.
     */
    private void setTestSuites(TestNG driver, URI ets) {
        if (ets.getScheme().equalsIgnoreCase("jar")) {
            // jar:{url}!/{entry}
            String[] jarPath = ets.getSchemeSpecificPart().split("!");
            File jarFile = new File(URI.create(jarPath[0]));
            driver.setTestJar(jarFile.getAbsolutePath());
            driver.setXmlPathInJar(jarPath[1].substring(1));
        } else {
            List<String> testSuites = new ArrayList<String>();
            File tngFile = new File(ets);
            if (tngFile.exists()) {
                LOGR.log(Level.CONFIG, "Using TestNG config file {0}",
                        tngFile.getAbsolutePath());
                testSuites.add(tngFile.getAbsolutePath());
            } else {
                throw new IllegalArgumentException(
                        "A valid TestNG config file reference is required.");
            }
            driver.setTestSuites(testSuites);
        }
    }
}
