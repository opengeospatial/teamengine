package com.occamlab.te.index;

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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import com.occamlab.te.TECore;

/**
 * Describes a test in a test suite. It corresponds to a &lt;test&gt; element in an index
 * file.
 */
public class TestEntry extends TemplateEntry {

	private static final Logger LOGR = Logger.getLogger(TestEntry.class.getName());

	private int defaultResult = TECore.PASS;

	private int result = TECore.PASS;

	private String context;

	private String type;

	private String assertion;

	private boolean isConformanceClass;

	private boolean isBasic;

	public TestEntry() {
		super();
	}

	TestEntry(Element test) {
		super(test);
		if (usesContext()) {
			setContext(test.getElementsByTagName("context").item(0).getTextContent());
		}
		setType(test.getElementsByTagName("type").item(0).getTextContent());
		setAssertion(test.getElementsByTagName("assertion").item(0).getTextContent());
		String defaultResultName = test.getElementsByTagName("defaultResult").item(0).getTextContent();
		setDefaultResult(defaultResultName.equals("BestPractice") ? TECore.BEST_PRACTICE : TECore.PASS);
		setResult(getDefaultResult());

		this.isConformanceClass = Boolean.parseBoolean(test.getAttribute("isConformanceClass"));
		this.isBasic = Boolean.parseBoolean(test.getAttribute("isBasic"));
	}

	public boolean isConformanceClass() {
		return isConformanceClass;
	}

	public boolean isBasic() {
		return isBasic;
	}

	public int getDefaultResult() {
		return defaultResult;
	}

	public void setDefaultResult(int defaultResult) {
		this.defaultResult = defaultResult;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		if (LOGR.isLoggable(Level.FINE)) {
			LOGR.fine(String.format("Setting test result for %s: %d", getQName(), result));
		}
		this.result = result;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAssertion() {
		return assertion;
	}

	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}

	public String toString() {
		return super.toString() + "[result=" + this.result + "]";
	}

}
