package com.occamlab.te.spi.executors.testng;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.vocabulary.RDF;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;

import com.occamlab.te.spi.vocabulary.EARL;

public class VerifyEarlReportListener {

    private static ITestContext testContext;
    private static ISuite suite;
    private static XmlSuite xmlSuite;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(testContext.getSuite()).thenReturn(suite);
        xmlSuite = mock(XmlSuite.class);
        when(suite.getXmlSuite()).thenReturn(xmlSuite);
    }

    @Test
    public void initializeModel() {
        String testSubject = "http://www.example.org/test/subject";
        Map<String, String> params = new HashMap<String, String>();
        params.put("iut", testSubject);
        when(xmlSuite.getAllParameters()).thenReturn(params);
        EarlReportListener iut = new EarlReportListener();
        Model model = iut.initModel(testContext);
        assertNotNull(model);
        ResIterator itr = model.listSubjectsWithProperty(RDF.type, EARL.TestSubject);
        assertEquals("Unexpected URI for earl:TestSubject", testSubject, itr.next().getURI());
    }

    @Test
    public void writeModel() throws IOException {
        String testSubject = "http://www.example.org/test/subject-2";
        Map<String, String> params = new HashMap<String, String>();
        params.put("iut", testSubject);
        when(xmlSuite.getAllParameters()).thenReturn(params);
        EarlReportListener iut = new EarlReportListener();
        Model model = iut.initModel(testContext);
        File outputDir = new File(System.getProperty("user.home"));
        iut.writeModel(model, outputDir, true);
        File earlFile = new File(outputDir, "earl.rdf");
        assertTrue("EARL results file does not exist at " + earlFile.getAbsolutePath(), earlFile.exists());
        assertTrue("EARL results file is emtpy.", earlFile.length() > 0);
        earlFile.delete();
    }
}
