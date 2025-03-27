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

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * Support ctl:startStopwatch and ctl:elapsedTime functions so test script authors can
 * time execution of test activities.
 *
 * @author Paul Daisey (Image Matters LLC)
 *
 */
public class Stopwatch {

	static Map<String, Date> watchesMap = new HashMap<>();

	/**
	 * Start a named Stopwatch
	 * @param watchName
	 */
	public static void start(String watchName) {
		watchesMap.put(watchName, new Date());
	}

	/**
	 * @param watchName
	 * @return elapsed time in milliseconds for a named Watch, or 0 if watchName not
	 * started
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
