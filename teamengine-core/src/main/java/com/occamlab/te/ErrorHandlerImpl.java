package com.occamlab.te;

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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.io.PrintWriter;

/**
 * Handles errors arising while processing XML resources and records the numbers of error
 * and warning notifications received.
 *
 */
public class ErrorHandlerImpl implements ErrorHandler {

	private int ErrorCount = 0;

	private int WarningCount = 0;

	private PrintWriter Logger;

	private String Prefix = "";

	public ErrorHandlerImpl() {
		this(null, new PrintWriter(System.out));
	}

	public ErrorHandlerImpl(String role, PrintWriter logger) {
		setRole(role);
		setLogger(logger);
	}

	public void setRole(String role) {
		if (role != null) {
			Prefix = role + " ";
		}
	}

	public void setLogger(PrintWriter logger) {
		Logger = logger;
	}

	public String getErrorCounts() {
		String msg = "";
		if (ErrorCount > 0 || WarningCount > 0) {
			if (ErrorCount > 0) {
				msg += ErrorCount + " error" + (ErrorCount == 1 ? "" : "s");
				if (WarningCount > 0)
					msg += " and ";
			}
			if (WarningCount > 0) {
				msg += WarningCount + " warning" + (WarningCount == 1 ? "" : "s");
			}
		}
		else {
			msg = "No errors or warnings";
		}
		msg += " detected.";
		return msg;
	}

	private void error(String type, SAXParseException e) {
		Logger.print(type);
		if (e.getLineNumber() >= 0) {
			Logger.print(" at line " + e.getLineNumber());
			if (e.getColumnNumber() >= 0) {
				Logger.print(", column " + e.getColumnNumber());
			}
			if (e.getSystemId() != null) {
				Logger.print(" of " + e.getSystemId());
			}
		}
		else {
			if (e.getSystemId() != null) {
				Logger.print(" in " + e.getSystemId());
			}
		}
		Logger.println(":");
		Logger.println("  " + e.getMessage());
		Logger.flush();
		// System.err.println(type + " at line " + + e.getLineNumber() + ",
		// column " + e.getColumnNumber() + " of " + e.getSystemId() + ":");
		// System.err.println(" " + e.getMessage());
	}

	public int getErrorCount() {
		return ErrorCount;
	}

	public int getWarningCount() {
		return WarningCount;
	}

	public void error(SAXParseException exception) {
		error(Prefix + "error", exception);
		ErrorCount++;
	}

	public void fatalError(SAXParseException exception) {
		error("Fatal " + Prefix + "error", exception);
		ErrorCount++;
	}

	public void warning(SAXParseException exception) {
		error(Prefix + "warning", exception);
		WarningCount++;
	}

}
