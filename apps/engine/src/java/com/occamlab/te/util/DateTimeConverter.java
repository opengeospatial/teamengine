package com.occamlab.te.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Functions to convert various dates and times to a standard DateTime string.
 * 
 * @author jparrpearson
 */
public class DateTimeConverter {


	public DateTimeConverter() {
	}

	/**
	 * Creats a DateTime object and adds any necessary default information to the input string
	 * 
	 * @param dateTimeStr
	 *            a string representing a date/time
	 */
	public String getDateTimeValue(String dateTimeStr) {
		
		// Ensure the string is in proper ISO form (make adjustments from XML-syntax)
		if (dateTimeStr.endsWith("Z")) {
			// No 'T' even though it ends with a 'Z', so add one
			if (dateTimeStr.indexOf("T") == -1) {
				StringBuffer buf = new StringBuffer(dateTimeStr);
				int lastZ = buf.lastIndexOf("Z");
				buf.insert(lastZ, "T");
				dateTimeStr = buf.toString();
			}
		}
		Pattern pattern = Pattern.compile(".*[+-]\\d{2}:\\d{2}");
		Matcher matcher = pattern.matcher(dateTimeStr);
		if (matcher.matches()) {
			// There is a timezone, but no 'T', so add one
			if (dateTimeStr.indexOf("T") == -1) {
				StringBuffer buf = new StringBuffer(dateTimeStr);
				int lastColon = buf.lastIndexOf(":");
				buf.insert(lastColon-3, "T");
				dateTimeStr = buf.toString();
			}
		}		
				
		// Create a formatter to parse the input string
		DateTimeFormatter formatter = ISODateTimeFormat.dateOptionalTimeParser();
		DateTime dateTime = null;
		try {
			// Format the input string as a ISO standard DateTime string
			dateTime = formatter.parseDateTime(dateTimeStr);
		} catch (Exception e) {
			System.out.println("DateTimeConverter ERROR: "+e.getMessage());
		}

		return dateTime.toString();
	}

	public static void main(String[] args) {
		DateTimeConverter dateTimeConverter = new DateTimeConverter();
		String result = dateTimeConverter.getDateTimeValue(args[0]);
		System.out.println(result);
	}
}