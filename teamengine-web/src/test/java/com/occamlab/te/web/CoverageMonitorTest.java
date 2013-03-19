package com.occamlab.te.web;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class CoverageMonitorTest {

    private static DocumentBuilder docBuilder;
    private static File sessionDir;
    private static final String COVERAGE_FILE = "coverage.xml";

    @BeforeClass
    public static void initFixture() throws ParserConfigurationException {
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        sessionDir = new File(System.getProperty("java.io.tmpdir"));
    }

    @After
    public void deleteCoverageFile() {
        File file = new File(sessionDir, COVERAGE_FILE);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Failed to delete file at " + file);
        }
    }

    @Test
    public void createCoverageMonitor() {
        File sessionDir = new File(System.getProperty("java.io.tmpdir"));
        CoverageMonitor monitor = new CoverageMonitor("", sessionDir);
        assertNotNull(monitor);
        File coverageFile = new File(sessionDir, "coverage.xml");
        assertTrue("Expected to find coverage.xml file in session dir.",
                coverageFile.exists());
        assertTrue("File coverage.xml is empty.", coverageFile.length() > 0);
    }

    @Test
    public void testInspectQuery() throws SAXException, IOException,
            XPathExpressionException {
        CoverageMonitor monitor = new CoverageMonitor("", sessionDir);
        String query = "REQUEST=GetMap&LAYERS=cite:BasicPolygons,cite:Terrain";
        monitor.inspectQuery(query);
        File coverageFile = new File(sessionDir, "coverage.xml");
        Document coverage = docBuilder.parse(coverageFile);
        String expr = String
                .format("not(//request[@name='GetMap']/param[@name='%s']/value[text() = '%s'])",
                        "layers", "cite:Terrain");
        assertXPath(expr, coverage);
        expr = String
                .format("//request[@name='GetMap']/param[@name='%s']/value[text() = '%s']",
                        "layers", "cite:Lakes");
        assertXPath(expr, coverage);
    }

    void assertXPath(String expr, Document coverage)
            throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        Boolean result = (Boolean) xpath.evaluate(expr, coverage,
                XPathConstants.BOOLEAN);
        assertTrue("XPath expression evaluated as false: " + expr, result);
    }

}
