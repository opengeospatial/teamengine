/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.executors.testng;

/*-
 * #%L
 * TEAM Engine - Service Providers
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.XMLReporterConfig;
import org.testng.xml.XmlSuite;

/**
 * A basic XML reporter that suppresses stack traces and writes the test results to a
 * single file (testng-results.xml) in the specified output directory.
 *
 * @see <a href="http://testng.org/doc/documentation-main.html#logging-xml-reports">
 * TestNG documentation, 6.2.5</a>
 */
public final class BasicXMLReporter implements IReporter {

	private final XMLReporter reporter;

	public BasicXMLReporter() {
		this.reporter = createCustomXMLReporter();
	}

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		this.reporter.generateReport(xmlSuites, suites, outputDirectory);
	}

	/**
	 * Creates an XML reporter that suppresses stack traces and includes test result
	 * attributes (if set).
	 * @return A customized reporter that generates an XML representation of the test
	 * results. The document element is &lt;testng-results&gt;.
	 */
	XMLReporter createCustomXMLReporter() {
		XMLReporter customReporter = new XMLReporter();
		XMLReporterConfig config = customReporter.getConfig();
		config.setStackTraceOutput(XMLReporterConfig.StackTraceLevels.NONE);
		config.setGenerateTestResultAttributes(true);
		config.setGenerateGroupsAttribute(true);
		return customReporter;
	}

}
