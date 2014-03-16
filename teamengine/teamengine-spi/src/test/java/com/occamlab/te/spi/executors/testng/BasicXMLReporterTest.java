package com.occamlab.te.spi.executors.testng;

import com.occamlab.te.spi.executors.testng.BasicXMLReporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.XMLReporterConfig;

public class BasicXMLReporterTest {

    public BasicXMLReporterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testCreateCustomXMLReporter() {
        BasicXMLReporter reporter = new BasicXMLReporter();
        XMLReporter result = reporter.createCustomXMLReporter();
        Assert.assertEquals("Unexpected stackTraceOutputMethod",
                XMLReporterConfig.STACKTRACE_NONE,
                result.getStackTraceOutputMethod());
        Assert.assertNull("Expected null outputDirectory",
                result.getOutputDirectory());
    }
}
