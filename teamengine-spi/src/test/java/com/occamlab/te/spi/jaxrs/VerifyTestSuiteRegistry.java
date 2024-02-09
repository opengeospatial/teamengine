/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
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
		Assert.assertNull(registry.getController("foo"));
	}

}
