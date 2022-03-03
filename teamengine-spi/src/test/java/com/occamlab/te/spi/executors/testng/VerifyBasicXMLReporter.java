/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
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
        Assert.assertEquals("Unexpected stackTraceOutputMethod", XMLReporterConfig.STACKTRACE_NONE,
                result.getStackTraceOutputMethod());
        Assert.assertNull("Expected null outputDirectory", result.getOutputDirectory());
    }
}
