/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.jaxrs;

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

import javax.xml.transform.Source;
import org.w3c.dom.Document;

/**
 * Controls execution of a test suite. An executable test suite (ETS) is distinguished by
 * an alphanumeric code and a version.
 */
public interface TestSuiteController {

	/**
	 * Returns the ETS code.
	 * @return A String containing an alphanumeric code value. It cannot start with a
	 * digit.
	 */
	String getCode();

	/**
	 * Returns the version of this ETS.
	 * @return A String indicating the version; it complies with the Maven versioning
	 * scheme.
	 */
	String getVersion();

	/**
	 * Returns the title of this ETS.
	 * @return A String denoting the title.
	 */
	String getTitle();

	/**
	 * Executes a test run and returns the results.
	 * @param testRunArgs A DOM Document conveying the test run arguments. The content is
	 * implementation-specific.
	 * @return A Source object that supplies an XML representation of the test results.
	 * @throws Exception If the supplied test run arguments are invalid for any reason.
	 */
	Source doTestRun(Document testRunArgs) throws Exception;

}
