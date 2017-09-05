/**
 * **************************************************************************
 *
 * Contributor(s): 
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */
package com.occamlab.te.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Monitors which service capabilities are exercised by a client request. A
 * representation of a service implementation conformance statement (ICS) is
 * updated according to the content of a request message; that is, any supplied
 * parameter options are pruned from the initial tree.
 * <p>
 * When the test session is terminated the residual tree includes only those
 * request parameter values that did <strong>not</strong> appear in any request,
 * thus indicating which implemented options were not covered.
 * </p>
 * 
 * <p>
 * A sample representation of an ICS for the GetCapabilities request is shown in
 * the listing below.
 * </p>
 * 
 * <pre>
 * {@code
 * <request name="GetCapabilities">
 *   <param name="format">
 *     <value>text/xml</value>
 *   </param>
 *   <param name="updatesequence">
 *     <value>0</value>
 *   </param>
 * </request>
 * }
 * </pre>
 * 
 */
public class CoverageMonitor {

    private static final Logger LOGR = Logger.getLogger(CoverageMonitor.class
            .getPackage().getName());
    private static final Map<URI, String> ICS_MAP = createICSMap();

    private URI requestId;
    private Document coverageDoc;
    private File testSessionDir;

    /**
     * Creates a CoverageMonitor for a given service request.
     *
     * @param uri
     *            A URI value that identifies a service request message.
     */
    public CoverageMonitor(String uri) {
        this.requestId = URI.create(uri);
        String icsPath = "/coverage/" + ICS_MAP.get(this.requestId);
        try {
	           // Fortify Mod: Disable entity expansion to foil External Entity Injections
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            df.setNamespaceAware(true);
	      df.setExpandEntityReferences(false);
            DocumentBuilder docBuilder = df.newDocumentBuilder();
            // DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                // End Fortify Mods
            this.coverageDoc = docBuilder.parse(
                    getClass().getResourceAsStream(icsPath), null);
        } catch (Exception e) {
            LOGR.warning(e.getMessage());
        }
        LOGR.config("Created coverage monitor using ICS at " + icsPath);
    }

    private static Map<URI, String> createICSMap() {
        HashMap<URI, String> icsMap = new HashMap<URI, String>();
        icsMap.put(URI.create("urn:wms_client_test_suite/GetCapabilities"),
                "WMS-GetCapabilities.xml");
        icsMap.put(URI.create("urn:wms_client_test_suite/GetMap"),
                "WMS-GetMap.xml");
        icsMap.put(URI.create("urn:wms_client_test_suite/GetFeatureInfo"),
                "WMS-GetFeatureInfo.xml");
        return icsMap;
    }

    /**
     * Returns the location of the test session directory.
     * 
     * @return A File object denoting a directory in the local file system.
     */
    public File getTestSessionDir() {
        return testSessionDir;
    }

    /**
     * Sets the location of the test session directory where the coverage
     * results will be written to.
     * 
     * @param sessionDir
     *            A File object (it should correspond to a directory).
     */
    public void setTestSessionDir(File sessionDir) {
        if (!sessionDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: "
                    + testSessionDir);
        }
        this.testSessionDir = sessionDir;
    }

    /**
     * Inspects the query part of a GET request URI and updates the ICS document
     * by removing parameter values that occur in the request. The initial
     * coverage report is modified over the course of the test run as requests
     * are received; the residual document includes only those parameter values
     * that were <em>not requested</em>.
     * 
     * @param query
     *            The (decoded) query component of a GET request.
     */
    void inspectQuery(String query) {
        Map<String, String> qryParams = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String[] nvp = param.split("=");
            if(nvp.length > 1){
              qryParams.put(nvp[0].toLowerCase(), nvp[1]);
            }else{
              qryParams.put(nvp[0].toLowerCase(), "");
            }    
        }
        String reqType = qryParams.get("request");
        XPath xpath = XPathFactory.newInstance().newXPath();
        for (Map.Entry<String, String> paramEntry : qryParams.entrySet()) {
            String[] paramValues = paramEntry.getValue().split(",");
            for (int i = 0; i < paramValues.length; i++) {
                String expr = String
                        .format("//request[@name='%s']/param[@name='%s']/value[text() = '%s']",
                                reqType, paramEntry.getKey(), paramValues[i]);
                NodeList result = null;
                try {
                    result = (NodeList) xpath.evaluate(expr, this.coverageDoc,
                            XPathConstants.NODESET);
                } catch (XPathExpressionException xpe) {
                    LOGR.log(Level.WARNING, "Failed to evaluate expression "
                            + expr, xpe);
                }
                if ((null != result) && (result.getLength() > 0)) {
                    for (int j = 0; j < result.getLength(); j++) {
                        Node value = result.item(j);
                        Element param = (Element) value.getParentNode();
                        param.removeChild(value);
                        // remove empty param element
                        if (param.getElementsByTagName("value").getLength() == 0) {
                            Node request = param.getParentNode();
                            request.removeChild(param);
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes the residual ICS document to a file in the test session directory.
     */
    public void writeCoverageResults() {
        File coverageFile = new File(this.testSessionDir,
                ICS_MAP.get(this.requestId));
        if (coverageFile.exists()) {
            return;
        }
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(coverageFile, false);
            writeDocument(fos, this.coverageDoc);
        } catch (FileNotFoundException e) {
        } finally {
            try {
                fos.close();
                LOGR.config("Wrote coverage results to "
                        + coverageFile.getCanonicalPath());
            } catch (IOException ioe) {
                LOGR.warning(ioe.getMessage());
            }
        }

    }

    /**
     * Writes a DOM Document to the given OutputStream using the "UTF-8"
     * encoding. The XML declaration is omitted.
     * 
     * @param outStream
     *            The destination OutputStream object.
     * @param doc
     *            A Document node.
     */
    void writeDocument(OutputStream outStream, Document doc) {
        DOMImplementationRegistry domRegistry = null;
        try {
            domRegistry = DOMImplementationRegistry.newInstance();
        } catch (Exception e) {
            LOGR.warning(e.getMessage());
        }
        DOMImplementationLS impl = (DOMImplementationLS) domRegistry
                .getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();
        writer.getDomConfig().setParameter("xml-declaration", false);
        writer.getDomConfig().setParameter("format-pretty-print", true);
        LSOutput output = impl.createLSOutput();
        output.setEncoding("UTF-8");
        output.setByteStream(outStream);
        writer.write(doc, output);
    }
}
