package com.occamlab.te.spi.executors.testng;

import java.util.Map;
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

    private Document testRunArgs;

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
    PrimarySuiteListener(Document testRunArgs) {
        this.testRunArgs = testRunArgs;
    }

    @Override
    public void onStart(ISuite suite) {
        if (null == this.testRunArgs) {
            return;
        }
        Map<String, String> params = suite.getXmlSuite().getParameters();
        NodeList entries = testRunArgs.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            params.put(entry.getAttribute("key"), entry.getTextContent());
        }
    }

    @Override
    public void onFinish(ISuite suite) {
    }
}
