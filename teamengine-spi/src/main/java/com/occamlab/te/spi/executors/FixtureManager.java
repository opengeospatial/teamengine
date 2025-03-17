/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.executors;

/*-
 * #%L
 * TEAM Engine - Service Providers
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages test fixtures that provide data to support the execution of a test run. Such
 * data often describe the test subject or its environment.
 */
public class FixtureManager {

	/**
	 * A singleton instance of the manager.
	 */
	private static volatile FixtureManager manager;

	/**
	 * A collection of TestRunFixture objects, keyed by identifier (e.g. a UUID value).
	 */
	private Map<String, TestRunFixture> fixtures;

	/**
	 * Returns a singleton manager in a lazy (but thread-safe) manner.
	 * @return the <code>FixtureManager</code> instance.
	 */
	public static FixtureManager getInstance() {

		// Employ a "double-checked locking" strategy because a lock is only
		// needed upon initialization; synchronize on the monitor belonging to
		// the class itself.
		if (null == manager) {
			synchronized (FixtureManager.class) {
				// check again, because the thread might have been preempted
				// just after the outer if was processed but before the
				// synchronized statement was executed
				if (manager == null) {
					manager = new FixtureManager();
				}
			}
		}
		return manager;
	}

	/**
	 * Gets the fixture for the specified test run. If runId is an empty String and only
	 * one fixture exists this is returned.
	 * @param runId The test run identifier (may be an empty String).
	 * @return A TestRunFixture, or {@code null } if a matching one cannot be found.
	 */
	public TestRunFixture getFixture(String runId) {
		if (runId.isEmpty() && this.fixtures.size() == 1) {
			runId = this.fixtures.keySet().iterator().next();
		}
		return fixtures.get(runId);
	}

	/**
	 * Adds a fixture.
	 * @param runId The test run identifier.
	 * @param fixture The TestRunFixture to be added (or replaced).
	 */
	public void addFixture(String runId, TestRunFixture fixture) {
		this.fixtures.put(runId, fixture);
	}

	/**
	 * Removes a fixture.
	 * @param runId The test run identifier.
	 */
	public void removeFixture(String runId) {
		this.fixtures.remove(runId);
	}

	/**
	 * Lists the identifiers of registered test run fixtures.
	 * @return A Set containing fixture identifiers.
	 */
	public Set<String> listFixtureIdentifiers() {
		return fixtures.keySet();
	}

	private FixtureManager() {
		this.fixtures = new HashMap<>();
	}

}
