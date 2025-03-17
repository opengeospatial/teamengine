package com.occamlab.te.util;

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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Provides various utility methods to manipulate temporal values.
 *
 */
public class DateTimeUtils {

	/**
	 * Parses a temporal value and returns the beginning instant. If the provided value is
	 * an instant, it is returned in the local (server) time zone.
	 * @param timeStamp a string representation of a temporal value (year, month, date, or
	 * dateTime values conforming to XML Schema datatypes).
	 * @return a String (ISO-8601 syntax) representing the beginning moment of the given
	 * temporal value.
	 */
	public static String getBeginningInstant(String timeStamp) {

		// Ensure the string is in proper ISO form (make adjustments from
		// XML-syntax)
		if (timeStamp.endsWith("Z")) {
			// No 'T' even though it ends with a 'Z', so add one
			if (timeStamp.indexOf("T") == -1) {
				StringBuffer buf = new StringBuffer(timeStamp);
				int lastZ = buf.lastIndexOf("Z");
				buf.insert(lastZ, "T");
				timeStamp = buf.toString();
			}
		}
		Pattern pattern = Pattern.compile(".*[+-]\\d{2}:\\d{2}");
		Matcher matcher = pattern.matcher(timeStamp);
		if (matcher.matches()) {
			// There is a timezone, but no 'T', so add one
			if (timeStamp.indexOf("T") == -1) {
				StringBuffer buf = new StringBuffer(timeStamp);
				int lastColon = buf.lastIndexOf(":");
				buf.insert(lastColon - 3, "T");
				timeStamp = buf.toString();
			}
		}
		DateTimeFormatter formatter = ISODateTimeFormat.dateOptionalTimeParser();
		DateTime dateTime = null;
		try {
			dateTime = formatter.parseDateTime(timeStamp);
		}
		catch (Exception e) {
			System.out.println("DateTimeUtils ERROR: " + e.getMessage());
		}

		return dateTime.toString();
	}

}
