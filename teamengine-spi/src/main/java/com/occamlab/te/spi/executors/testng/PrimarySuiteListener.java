package com.occamlab.te.spi.executors.testng;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A suite listener that adds the supplied test run arguments to the collection
 * of suite-level parameters. Since this listener is the first one to be
 * registered it will be called first and the parameters it sets should be
 * available to all subsequent listeners.
 */
class PrimarySuiteListener implements ISuiteListener {

    private final static Logger LOGR = Logger.getLogger(PrimarySuiteListener.class.getName());
    private Document testRunArgs;
    private UUID testRunId;

    /**
     * Constructs the listener. The test run arguments are expected to be
     * contained in an XML properties document. If a property has multiple
     * values they must be separated with a comma.
     * 
     * @param testRunArgs
     *            A DOM Document node that contains a set of XML properties.
     * 
     * @see java.util.Properties#loadFromXML(java.io.InputStream) loadFromXML
     */
    public PrimarySuiteListener(Document testRunArgs) {
        LOGR.log(Level.FINE, "Initializing PrimarySuiteListener...");
        if (null == testRunArgs) {
            LOGR.log(Level.WARNING, "Test run input document is null");
            throw new NullPointerException("Test run input document is null");
        }
        NodeList entries = testRunArgs.getElementsByTagName("entry");
        if (entries.getLength() == 0) {
            throw new IllegalArgumentException(
                    String.format("No test run arguments found in %s", testRunArgs.getDocumentElement().getNodeName()));
        }
        this.testRunArgs = testRunArgs;
        this.testRunId = UUID.randomUUID();
    }

    /**
     * Sets the test run identifier.
     * 
     * @param testRunId
     *            An immutable universally unique identifier (a 128-bit value).
     */
    public void setTestRunId(UUID testRunId) {
        this.testRunId = testRunId;
    }

    @Override
    public void onStart(ISuite suite) {
        LOGR.log(Level.FINE, "Entering PrimarySuiteListener::onStart...");
        Map<String, String> params = suite.getXmlSuite().getParameters();
        NodeList entries = testRunArgs.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            String value = entry.getTextContent().trim();
            if (value.isEmpty()) {
                continue;
            }
            params.put(entry.getAttribute("key"), value);
            LOGR.log(Level.FINE, "Added parameter: {0}={1}", new Object[] { entry.getAttribute("key"), value });
        }
        params.put("uuid", this.testRunId.toString());
    }

    @Override
    public void onFinish(ISuite suite) {
    }
}
