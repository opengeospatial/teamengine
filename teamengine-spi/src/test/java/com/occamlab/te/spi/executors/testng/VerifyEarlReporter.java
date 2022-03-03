/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 ****************************************************************
 *
 * Contributor(s):
 *    C. Heazel (WiSC): Moved vocabulary package from spi to core
 *
 ***************************************************************/
package com.occamlab.te.spi.executors.testng;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

import com.occamlab.te.vocabulary.EARL;

public class VerifyEarlReporter {

    private static ITestContext testContext;
    private static ISuite suite;
    private static XmlSuite xmlSuite;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() throws Exception {
        testContext = mock(ITestContext.class);
        suite = mock(ISuite.class);
        when(suite.getName()).thenReturn("abc20-1.0");
        when(testContext.getSuite()).thenReturn(suite);
        xmlSuite = mock(XmlSuite.class);
        when(suite.getXmlSuite()).thenReturn(xmlSuite);
    }

    @Test
    public void initializeModel() {
        String testSubject = "http://www.example.org/test/subject";
        Map<String, String> params = new HashMap<String, String>();
        params.put("iut", testSubject);
        params.put("uuid", UUID.randomUUID().toString());
        when(xmlSuite.getAllParameters()).thenReturn(params);
        EarlReporter iut = new EarlReporter();
        Model model = iut.initializeModel(suite);
        assertNotNull(model);
        ResIterator itr = model.listSubjectsWithProperty(RDF.type, EARL.TestSubject);
        assertEquals("Unexpected URI for earl:TestSubject", testSubject, itr.next().getURI());
    }

    @Test
    public void writeModel() throws IOException {
        String testSubject = "http://www.example.org/test/subject-2";
        Map<String, String> params = new HashMap<String, String>();
        params.put("iut", testSubject);
        params.put("uuid", UUID.randomUUID().toString());
        when(xmlSuite.getAllParameters()).thenReturn(params);
        EarlReporter iut = new EarlReporter();
        Model model = iut.initializeModel(suite);
        File outputDir = new File(System.getProperty("user.home"));
        iut.writeModel(model, outputDir, true);
        File earlFile = new File(outputDir, "earl-results.rdf");
        assertTrue("EARL results file does not exist at " + earlFile.getAbsolutePath(), earlFile.exists());
        assertTrue("EARL results file is emtpy.", earlFile.length() > 0);
        earlFile.delete();
    }
}
