package com.occamlab.te.spi.stats;

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

import java.util.List;

/**
 * This POJO class is used to hold the user details which is retrieved from the
 * session.xml file and used for further operations.
 *
 * @author Keshav
 *
 */
public class SessionDetails {

	String id;

	String etsName;

	String date;

	int status;

	List<String> failedTestList;

	public SessionDetails(String id, String etsName, String date, int status, List<String> failedTestList) {
		this.id = id;
		this.etsName = etsName;
		this.date = date;
		this.status = status;
		this.failedTestList = failedTestList;
	}

	public SessionDetails() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEtsName() {
		return etsName;
	}

	public void setEtsName(String etsName) {
		this.etsName = etsName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public List<String> getFailedTestList() {
		return failedTestList;
	}

	public void setFailedTestList(List<String> failedTestList) {
		this.failedTestList = failedTestList;
	}

}
