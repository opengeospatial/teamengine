package com.occamlab.te.parsers;

/*-
 * #%L
 * TEAM Engine - Core Module
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

import java.io.PrintWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class XSLTransformationErrorHandler implements ErrorListener {

	PrintWriter logger;

	boolean ignoreErrors;

	boolean ignoreWarnings;

	int errorCount;

	int warningCount;

	public XSLTransformationErrorHandler(PrintWriter logger, boolean ignoreErrors, boolean ignoreWarnings) {
		this.logger = logger;
		this.ignoreErrors = ignoreErrors;
		this.ignoreWarnings = ignoreWarnings;
	}

	@Override
	public void error(TransformerException e) throws TransformerException {
		if (!ignoreErrors) {
			logger.println("Error: " + e.getMessageAndLocation());
		}
		errorCount++;
	}

	@Override
	public void fatalError(TransformerException e) throws TransformerException {
		logger.println("Fatal Error: " + e.getMessageAndLocation());
	}

	@Override
	public void warning(TransformerException e) throws TransformerException {
		if (!ignoreWarnings) {
			logger.println("Warning: " + e.getMessageAndLocation());
		}
		warningCount++;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public int getWarningCount() {
		return warningCount;
	}

}
