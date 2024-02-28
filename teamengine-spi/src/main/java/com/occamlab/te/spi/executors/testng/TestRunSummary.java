/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.executors.testng;

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

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;

/**
 * Provides summary information about the results of a test run.
 */
public class TestRunSummary {

	private int totalPassed;

	private int totalFailed;

	private int totalSkipped;

	private Duration totalDuration;

	public TestRunSummary(final ISuite suite) {
		summarizeResults(suite.getResults());
	}

	/**
	 * The total number of tests that passed.
	 * @return the totalPassed
	 */
	public int getTotalPassed() {
		return totalPassed;
	}

	/**
	 * The total number of tests that failed.
	 * @return the totalFailed
	 */
	public int getTotalFailed() {
		return totalFailed;
	}

	/**
	 * The total number of tests that were skipped.
	 * @return the totalSkipped
	 */
	public int getTotalSkipped() {
		return totalSkipped;
	}

	/**
	 * The total duration of the test run in ISO 8601 format (PT[n]H[n]M[n]S).
	 * @return the duration of the test run.
	 */
	public String getTotalDuration() {
		return totalDuration.toString();
	}

	/**
	 * Reads the test results and extracts summary information. Each entry in the results
	 * corresponds to a test set (conformance class).
	 * @param results The results for all test sets that were executed.
	 */
	void summarizeResults(Map<String, ISuiteResult> results) {
		Date earliestStartDate = new Date();
		Date latestEndDate = new Date();
		for (Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
			ITestContext testContext = entry.getValue().getTestContext();
			this.totalPassed += testContext.getPassedTests().size();
			this.totalFailed += testContext.getFailedTests().size();
			this.totalSkipped += testContext.getSkippedTests().size();
			Date startDate = testContext.getStartDate();
			if (earliestStartDate.after(startDate)) {
				earliestStartDate = startDate;
			}
			Date endDate = testContext.getEndDate();
			if (latestEndDate.before(endDate)) {
				latestEndDate = (endDate != null) ? endDate : startDate;
			}
		}
		this.totalDuration = Duration.between(earliestStartDate.toInstant(), latestEndDate.toInstant());
	}

}
