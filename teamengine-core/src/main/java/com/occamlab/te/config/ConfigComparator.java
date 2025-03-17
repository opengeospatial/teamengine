package com.occamlab.te.config;

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

import java.util.Comparator;

/**
 * Compares two ConfigEntry objects for order.
 *
 */
public class ConfigComparator implements Comparator<ConfigEntry> {

	private String denull(String s) {
		return s == null ? "" : s;
	}

	public int compare(ConfigEntry o1, ConfigEntry o2) {
		int i = o1.organization.compareTo(o2.organization);
		if (i != 0)
			return i;
		i = o1.standard.compareTo(o2.standard);
		if (i != 0)
			return i;
		i = o1.version.compareTo(o2.version);
		if (i != 0)
			return i;
		if (o1.suite != null && o2.suite != null) {
			return denull(o1.revision).compareTo(denull(o2.revision));
		}
		if (o1.suite == null && o2.suite != null) {
			return -1;
		}
		if (o1.suite != null && o2.suite == null) {
			return 1;
		}
		return 0;
	}

}
