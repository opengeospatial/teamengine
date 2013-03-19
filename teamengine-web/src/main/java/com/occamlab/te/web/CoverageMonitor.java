package com.occamlab.te.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
 * updated according to the content of a request message. The residual document
 * includes only those request parameter values that were not requested, thus
 * indicating which implemented options were not covered.
 * 
 * <p>
 * A sample representation of a server ICS is shown in the listing below.
 * </p>
 * 
 * <pre>
 * {@code
 * <service uri="http://ri.opengeospatial.org:8680/degree-wms-130/services">
 *   <request name="GetCapabilities">
 *     <param name="format">
 *       <value>text/xml</value>
 *     </param>
 *     <param name="updatesequence">
 *       <value>0</value>
 *     </param>
 *   </request>
 * </service>
 * }
 * </pre>
 * 
 */
public class CoverageMonitor {

    private static final Logger LOGR = Logger.getLogger(CoverageMonitor.class
            .getPackage().getName());
    private static final String COVERAGE_FILE = "coverage.xml";
    DocumentBuilder docBuilder;
    private File testSessionDir;

    /**
     * Creates a CoverageMonitor for a given service implementation. A fresh
     * copy of a service implementation conformance statement (ICS) is written
     * to the specified test session directory in the file "coverage.xml".
     * 
     * @param url
     *            A URL value that identifies a service endpoint.
     * @param sessionDir
     *            A test session directory.
     */
    public CoverageMonitor(String url, File sessionDir) {
        if (!sessionDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: "
                    + testSessionDir);
        }
        this.testSessionDir = sessionDir;
        File file = new File(testSessionDir, COVERAGE_FILE);
        if (!file.exists()) {
            // Only one known client test suite (WMS); use url in future
            String icsRef = "/ics/service-wms.xml";
            OutputStream fos = null;
            try {
                this.docBuilder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document coverage = this.docBuilder.parse(getClass()
                        .getResourceAsStream(icsRef), null);
                fos = new FileOutputStream(file);
                writeDocument(fos, coverage);
            } catch (Exception e) {
                LOGR.warning(e.getMessage());
            } finally {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    LOGR.warning(ioe.getMessage());
                }
            }
        }
    }

    /**
     * Inspects the query part of a GET request URI and updates the coverage
     * file by removing parameter values that occur in the request. The initial
     * coverage report is modified over the course of the test run as requests
     * are received; the residual document includes only unrequested parameter
     * values.
     * 
     * @param query
     *            The (decoded) query component of a GET request.
     */
    void inspectQuery(String query) {
        File coverageFile = new File(testSessionDir, COVERAGE_FILE);
        Document coverage = null;
        try {
            coverage = this.docBuilder.parse(coverageFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse coverage file at "
                    + coverageFile, e);
        }
        Map<String, String> qryParams = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String[] nvp = param.split("=");
            qryParams.put(nvp[0].toLowerCase(), nvp[1]);
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
                    result = (NodeList) xpath.evaluate(expr, coverage,
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
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(coverageFile, false);
            writeDocument(fos, coverage);
        } catch (FileNotFoundException e) {
        } finally {
            try {
                fos.close();
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
