/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.executors.testng;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A listener that sets or updates the suite-level parameters in the test suites using the
 * test run arguments presented in a properties document. The value of the key attribute
 * is set as the parameter name. The extra "uuid" parameter contains the test run
 * identifier.
 */
public class AlterSuiteParametersListener implements IAlterSuiteListener {

	private final static Logger LOGR = Logger.getLogger(AlterSuiteParametersListener.class.getName());

	private Document testRunArgs;

	private String testRunId = UUID.randomUUID().toString();

	/**
	 * Sets the test run arguments from entries in a properties document.
	 * @param testRunArgs A Document that contains a set of XML properties.
	 */
	public void setTestRunArgs(Document testRunArgs) {
		if (null == testRunArgs || testRunArgs.getElementsByTagName("entry").getLength() == 0) {
			throw new IllegalArgumentException(String.format("No test run arguments found."));
		}
		this.testRunArgs = testRunArgs;
	}

	/**
	 * Sets the test run identifier.
	 * @param testRunId A universally unique identifier (128-bit value).
	 */
	public void setTestRunId(String testRunId) {
		this.testRunId = testRunId;
	}

	/**
	 * Adds the entries from the properties document to the set of test suite parameters.
	 * An entry is skipped if its value is an empty string.
	 */
	@Override
	public void alter(List<XmlSuite> xmlSuites) {
		if (null == this.testRunArgs || this.testRunArgs.getElementsByTagName("entry").getLength() == 0) {
			return;
		}
		for (XmlSuite xmlSuite : xmlSuites) {
			Map<String, String> params = xmlSuite.getParameters();
			NodeList entries = this.testRunArgs.getElementsByTagName("entry");
			for (int i = 0; i < entries.getLength(); i++) {
				Element entry = (Element) entries.item(i);
				String value = entry.getTextContent().trim();
				if (value.isEmpty()) {
					continue;
				}
				params.put(entry.getAttribute("key"), value);
				LOGR.log(Level.FINE, "Added parameter: {0}={1}", new Object[] { entry.getAttribute("key"), value });
			}
			params.put("uuid", this.testRunId.toString());
		}
	}

}
