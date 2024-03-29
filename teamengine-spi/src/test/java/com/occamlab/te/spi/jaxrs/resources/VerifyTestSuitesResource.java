/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.jaxrs.resources;

import com.occamlab.te.spi.jaxrs.resources.TestSuiteSetResource;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class VerifyTestSuitesResource {

	private static final String HTML_NS = "http://www.w3.org/1999/xhtml";

	@Test
	public void testReadTemplate() {
		TestSuiteSetResource iut = new TestSuiteSetResource();
		Document result = iut.readTemplate();
		Node ul = result.getElementsByTagNameNS(HTML_NS, "ul").item(0);
		Assert.assertNotNull(ul);
		Assert.assertEquals("Expected empty ul element.", 0, ul.getChildNodes().getLength());
	}

}
