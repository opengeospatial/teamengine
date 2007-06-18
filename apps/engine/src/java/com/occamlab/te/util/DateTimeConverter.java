package com.occamlab.te.util;

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
		
		DateTimeFormatter formatter = ISODateTimeFormat.dateOptionalTimeParser();
		DateTime dateTime = null;
		try {
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