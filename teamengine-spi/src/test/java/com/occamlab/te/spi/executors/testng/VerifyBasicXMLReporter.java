package com.occamlab.te.spi.executors.testng;

import com.occamlab.te.spi.executors.testng.BasicXMLReporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.XMLReporterConfig;

public class VerifyBasicXMLReporter {

    public VerifyBasicXMLReporter() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void createCustomXMLReporter() {
        BasicXMLReporter iut = new BasicXMLReporter();
        XMLReporter result = iut.createCustomXMLReporter();
        Assert.assertEquals("Unexpected stackTraceOutputMethod", XMLReporterConfig.StackTraceLevels.NONE,
                result.getConfig().getStackTraceOutput());
        Assert.assertNull("Expected null outputDirectory", result.getConfig().getOutputDirectory());
    }
}
