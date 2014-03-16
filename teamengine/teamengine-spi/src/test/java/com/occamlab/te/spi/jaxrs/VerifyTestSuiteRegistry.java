package com.occamlab.te.spi.jaxrs;

import com.occamlab.te.spi.jaxrs.TestSuiteRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Verifies behavior of TestSuiteRegistry.
 */
public class VerifyTestSuiteRegistry {

    public VerifyTestSuiteRegistry() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void getInstance() {
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        Assert.assertNotNull(registry);
    }

    @Test
    public void getControllersAsEmptyList() {
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        Assert.assertTrue(registry.getControllers().isEmpty());
    }

    @Test
    public void getUnregisteredController() {
        TestSuiteRegistry registry = TestSuiteRegistry.getInstance();
        Assert.assertNull(registry.getController("foo", "1.0"));
    }
}
