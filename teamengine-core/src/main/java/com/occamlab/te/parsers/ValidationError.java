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

/**
 * Encapsulates information pertaining to a validation error. Instances of this class are
 * immutable.
 *
 * @author rmartell
 * @version $Rev$
 */
public class ValidationError {

	/**
	 * A warning (e.g., a condition that does not cause the instance to be non-conforming.
	 */
	public static final short WARNING = 1;

	/** An error (e.g., the instance is invalid). */
	public static final short ERROR = 2;

	/** A fatal error (e.g., the instance is not well-formed). */
	public static final short FATAL_ERROR = 3;

	/** The error message. */
	private String message;

	/** The severity level. */
	private short severity;

	/**
	 * Constructs an immutable error object.
	 * @param severity the severity level (Warning, Error, Fatal)
	 * @param message a descriptive message
	 */
	public ValidationError(short severity, String message) {
		if (null == message) {
			message = "No details available";
		}
		this.message = message;
		this.severity = severity;
	}

	/**
	 * Returns the message describing this error.
	 * @return the details about this error
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the severity code (a <code>short</code> value) for this error.
	 * @return the severity code for this error
	 */
	public short getSeverity() {
		return severity;
	}

}
