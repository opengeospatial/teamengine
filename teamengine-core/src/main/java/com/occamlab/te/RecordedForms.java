package com.occamlab.te;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordedForms {

	List<File> files = new ArrayList();

	int current = 0;

	public RecordedForms(List<File> recordedForms) {
		this.files = recordedForms;
	}

	/**
	 * Returns true if no pre-recorded forms are found
	 * @return boolean
	 */
	public boolean isEmpty() {
		return files.isEmpty();
	}

	public void addRecordedForm(String file) {
		this.files.add(new File(file));
	}

	public File next() {
		File result = files.get(current);
		if (current < (files.size() - 1)) {
			current++;
		}
		return result;
	}

}
