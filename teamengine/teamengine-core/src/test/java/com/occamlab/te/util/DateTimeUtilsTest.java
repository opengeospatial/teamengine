package com.occamlab.te.util;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

public class DateTimeUtilsTest {

    @Test
    public void getBeginningInstantForDate() {
        String date = "2013-04-25Z";
        // result expressed in local time zone
        String beginVal = DateTimeUtils.getBeginningInstant(date);
        DateTime dateTime = DateTime.parse(beginVal);
        DateTime beginDay = DateTime.parse("2013-04-25T00:00.00Z");
        assertEquals("Expected time instants to be equal.", 0,
                dateTime.compareTo(beginDay));
    }

}
