/**
 *
 *  Contributor(s):
 *     Charles Heazel (WiSC): Modified to address Fortify issues
 *     Mods deferred until dependency on core is resolved.
 *         February 26, 2018
 */
package com.occamlab.te.spi.executors.testng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.testng.TestNG;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.occamlab.te.spi.executors.TestRunExecutor;
import com.occamlab.te.spi.util.HtmlReport;

/**
 * 
 * Executes a TestNG test suite using the given test run arguments.
 */
public class TestNGExecutor implements TestRunExecutor {

    private static final Logger LOGR = Logger.getLogger(TestNGExecutor.class.getPackage().getName());

    private static final List<String> SUPPORTED_MEDIA_TYPES = Arrays.asList( "application/xml", "application/zip", "application/rdf+xml" );
    
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
    public TestNGExecutor(String testngSuite, String outputDirPath, boolean useDefaultListeners) {
        this.useDefaultListeners = useDefaultListeners;
        this.outputDir = new File(outputDirPath, "testng");
        if (!this.outputDir.exists() && !this.outputDir.mkdirs()) {
            LOGR.config("Failed to create output directory at " + this.outputDir);
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
     * {@code
     * <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
     * <properties version="1.0">
     *   <comment>Test run arguments</comment>
     *   <entry key="arg1">atom-feed.xml</entry>
     *   <entry key="arg2">L2</entry>
     * </properties>
     * }
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
            throw new IllegalArgumentException("No test run arguments were supplied.");
        }
        TestNG driver = new TestNG();
        setTestSuites(driver, this.testngConfig);
        driver.setVerbose(0);
        driver.setUseDefaultListeners(this.useDefaultListeners);
        UUID runId = UUID.randomUUID();
        File runDir = new File(this.outputDir, runId.toString());
        if (!runDir.mkdir()) {
            runDir = this.outputDir;
            LOGR.config("Created test run directory at " + runDir.getAbsolutePath());
        }
        driver.setOutputDirectory(runDir.getAbsolutePath());
        AlterSuiteParametersListener listener = new AlterSuiteParametersListener();
        listener.setTestRunArgs(testRunArgs);
        listener.setTestRunId(runId);
        driver.addAlterSuiteListener(listener);
        driver.run();
        Source source = null;
        try {
            File resultsFile = getResultsFile(getPreferredMediaType(testRunArgs), driver.getOutputDirectory());
            InputStream inStream = new FileInputStream(resultsFile);
            InputSource inSource = new InputSource(new InputStreamReader(inStream, StandardCharsets.UTF_8));
            source = new SAXSource(inSource);
            source.setSystemId(resultsFile.toURI().toString());
        } catch (IOException e) {
            LOGR.log(Level.SEVERE, "Error reading test results: " + e.getMessage());
        }
        return source;
    }

    /**
     * Returns the test results in the specified format. The default media type
     * is "application/xml", but "application/rdf+xml" (RDF/XML) is also
     * supported.
     * 
     * @param mediaType
     *            The media type of the test results (XML or RDF/XML).
     * @param outputDirectory
     *            The directory containing the test run output.
     * @return A File containing the test results.
     * @throws FileNotFoundException
     *             If no test results are found.
     */
	File getResultsFile(String mediaType, String outputDirectory)
			throws FileNotFoundException {
		// split out any media type parameters
		String contentType = mediaType.split(";")[0];
		String fileName = null;
		if (contentType.endsWith("rdf+xml") || contentType.endsWith("rdf+earl")) {
			fileName = "earl-results.rdf";
		} else if (contentType.endsWith("zip")) {
			File htmlResult = HtmlReport.getHtmlResultZip(outputDirectory);
			fileName = "result.zip";
		} else {
			fileName = "testng-results.xml";
		}
		File resultsFile = new File(outputDirectory, fileName);
		if (!resultsFile.exists()) {
			throw new FileNotFoundException("Test run results not found at "
					+ resultsFile.getAbsolutePath());
		}
		return resultsFile;
	}

    /**
     * Gets the preferred media type for the test results as indicated by the value of the "acceptMediaType" key in the
     * given properties file. The default value is "application/xml".
     * 
     * @param testRunArgs
     *            An XML properties file containing test run arguments.
     * @return The preferred media type.
     */
    String getPreferredMediaType( Document testRunArgs ) {
        String mediaTypeFromTestRunArg = parseMediaTypeFromTestRunArgs( testRunArgs );
        if ( mediaTypeFromTestRunArg != null && SUPPORTED_MEDIA_TYPES.contains( mediaTypeFromTestRunArg ) )
            return mediaTypeFromTestRunArg;
        return "application/xml";
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
                LOGR.log(Level.CONFIG, "Using TestNG config file {0}", tngFile.getAbsolutePath());
                testSuites.add(tngFile.getAbsolutePath());
            } else {
                throw new IllegalArgumentException("A valid TestNG config file reference is required.");
            }
            driver.setTestSuites(testSuites);
        }
    }

    private String parseMediaTypeFromTestRunArgs( Document testRunArgs ) {
        NodeList entries = testRunArgs.getElementsByTagName( "entry" );
        for ( int i = 0; i < entries.getLength(); i++ ) {
            Element entry = (Element) entries.item( i );
            if ( entry.getAttribute( "key" ).equals( "acceptMediaType" ) ) {
                return entry.getTextContent().trim();
            }
        }
        return null;
    }

}
