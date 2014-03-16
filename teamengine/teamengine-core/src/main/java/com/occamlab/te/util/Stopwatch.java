package com.occamlab.te.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * Support ctl:startStopwatch and ctl:elapsedTime functions so test script
 * authors can time execution of test activities.
 * 
 * @author Paul Daisey (Image Matters LLC)
 * 
 */
public class Stopwatch {
    static Map<String, Date> watchesMap = new HashMap<String, Date>();

    /**
     * Start a named Stopwatch
     * 
     * @param watchName
     */
    public static void start(String watchName) {
        watchesMap.put(watchName, new Date());
    }

    /**
     * @param watchName
     * @return elapsed time in milliseconds for a named Watch, or 0 if watchName
     *         not started
     */
    public static String elapsedTime(String watchName) {
        long elapsed = 0;
        Date start = watchesMap.get(watchName);
        if (start != null) {
            Date end = new Date();
            elapsed = end.getTime() - start.getTime();
        }
        return Long.toString(elapsed);
    }
}
