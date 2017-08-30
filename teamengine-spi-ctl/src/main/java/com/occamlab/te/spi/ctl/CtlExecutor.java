package com.occamlab.te.spi.ctl;

import com.occamlab.te.CtlEarlReporter;
import com.occamlab.te.Engine;
import com.occamlab.te.Generator;
import com.occamlab.te.RuntimeOptions;
import com.occamlab.te.SetupOptions;
import com.occamlab.te.TEClassLoader;
import com.occamlab.te.TECore;
import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.spi.executors.TestRunExecutor;
import com.occamlab.te.spi.util.HtmlReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Executes a CTL test suite using the given test run arguments.
 */
public class CtlExecutor implements TestRunExecutor {

    private final SetupOptions setupOpts;
    private Map<String, String> testInputs = new HashMap<String, String>();

    /**
     * Constructs a new CtlExecutor using the given set of configuration
     * settings.
     *
     * @param config The configuration settings for the test run.
     */
    public CtlExecutor(SetupOptions config) {
        this.setupOpts = config;
    }

    /**
     * Executes a test suite.
     *
     * @param testRunArgs An XML properties file that supplies the test run
     * arguments.
     * @return A Source representing the test results.
     */
    @Override
    public Source execute(Document testRunArgs) {
        RuntimeOptions runOpts = extractTestRunArguments(testRunArgs);
        runOpts.setLogDir(new File(System.getProperty("java.io.tmpdir")));
        if (null == runOpts.getSessionId()) {
            runOpts.setSessionId(UUID.randomUUID().toString());
        }
        String suiteName = null;
        try {
            Index masterIndex = Generator.generateXsl(this.setupOpts);
            SuiteEntry se = null;
            if (suiteName == null) {
              Iterator<String> it = masterIndex.getSuiteKeys().iterator();
              if (!it.hasNext()) {
                throw new Exception("Error: No suites in sources.");
              }
              se = masterIndex.getSuite(it.next());
            }
            suiteName = se.getTitle();
            TEClassLoader defaultLoader = new TEClassLoader(null);
            Engine engine = new Engine(masterIndex,
                    setupOpts.getSourcesName(), defaultLoader);
            TECore ctlRunner = new TECore(engine, masterIndex, runOpts);

            ctlRunner.execute();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Failed to execute test suite.", ex);
            throw new RuntimeException(ex);
        }
        File resultsDir = new File(runOpts.getLogDir(), runOpts.getSessionId());
        File testLog = new File(resultsDir, "report_logs.xml");
        
        CtlEarlReporter report = new CtlEarlReporter();
        try{
        	report.generateEarlReport (resultsDir, testLog, suiteName, this.testInputs);
        }  catch (IOException iox) {
            throw new RuntimeException("Failed to serialize EARL results to " + resultsDir.getAbsolutePath(), iox);
        }
        // NOTE: Final result should be transformed to EARL report (RDF/XML)
        // resolve xinclude directives in CTL results
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        docFactory.setXIncludeAware(true);
        Source results = null;
        try {
            File resultsFile = getResultsFile(getPreferredMediaType(testRunArgs), resultsDir.toString());
            if(getPreferredMediaType(testRunArgs).endsWith("zip")){
              InputStream inStream = new FileInputStream(resultsFile);
              InputSource inSource = new InputSource(new InputStreamReader(inStream, StandardCharsets.UTF_8));
              results = new SAXSource(inSource);
              results.setSystemId(resultsFile.toURI().toString());
              } else {
              	Document resultsDoc = docFactory.newDocumentBuilder().parse(resultsFile);
              	results = new DOMSource(resultsDoc, resultsFile.toURI().toString());
              }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO,
                "Test results: {0}", results.getSystemId());
        return results;
    }

    /**
     * Extracts test run arguments from an XML properties file. The arguments
     * are added to the resulting <code>RuntimeOptions</code> object as
     * key-value pairs represented as a string: "{key}={value}".
     *
     * @param testRunArgs An XML representation of a properties file containing
     * an {@literal <entry>} element for each supplied argument.
     * @return The configuration settings for a test run, including a list of
     * parameters (which may be empty).
     *
     * @see
     * <a target="_blank" href="https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html">Properties</a>
     */
    RuntimeOptions extractTestRunArguments(Document testRunArgs) {
        RuntimeOptions runOpts = new RuntimeOptions();
        if (null != testRunArgs) {
            NodeList entries = testRunArgs.getElementsByTagName("entry");
            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                String kvp = String.format("%s=%s", entry.getAttribute("key"), entry.getTextContent().trim());
                runOpts.addParam(kvp);
                this.testInputs.put(entry.getAttribute("key"), entry.getTextContent().trim());
            }
        }
        return runOpts;
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
    File getResultsFile(String mediaType, String outputDirectory) throws FileNotFoundException {
    	 // split out any media type parameters
        String contentType = mediaType.split(";")[0];
        String fileName = null;
        if(contentType.endsWith("rdf+xml")){
        	fileName = "earl-results.rdf";
        } else if(contentType.endsWith("zip")){
        	
        File htmlResult = HtmlReport.getHtmlResultZip(outputDirectory);
        fileName = "result.zip";
        } else{
        	fileName = "report_logs.xml";
        }
        File resultsFile = new File(outputDirectory, fileName);
        if (!resultsFile.exists()) {
            throw new FileNotFoundException("Test run results not found at " + resultsFile.getAbsolutePath());
        }
        return resultsFile;
    }
    
    /**
     * Gets the preferred media type for the test results as indicated by the
     * value of the "acceptMediaType" key in the given properties file. The
     * default value is "application/xml".
     * 
     * @param testRunArgs
     *            An XML properties file containing test run arguments.
     * @return The preferred media type.
     */
    String getPreferredMediaType(Document testRunArgs) {
    	String mediaType = "application/rdf+xml";
        NodeList entries = testRunArgs.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            if (entry.getAttribute("key").equals("format")) {
            	if(entry.getTextContent().trim().equalsIgnoreCase("xml")){
                mediaType = "application/xml";
            	} else if(entry.getTextContent().trim().equalsIgnoreCase("html")){
                    mediaType = "application/zip";
                	}
            }
        }
        return mediaType;
    }
    
}
