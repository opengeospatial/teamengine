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

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.Test;
import com.occamlab.te.util.DomUtils;

public class SuiteEntry extends IndexEntry {

	String defaultResult = "Pass";

	QName startingTest;

	Document form = null;

	String title = null;

	String description = null;

	String link;

	String dataLink;

	public SuiteEntry() {
		super();
	}

	SuiteEntry(Element suite) throws Exception {
		super(suite);
		title = DomUtils.getElementByTagName(suite, "title").getTextContent();
		description = DomUtils.getElementByTagName(suite, "description").getTextContent();
		Element e = DomUtils.getElementByTagName(suite, "starting-test");
		String prefix = e.getAttribute("prefix");
		String namespaceUri = e.getAttribute("namespace-uri");
		String localName = e.getAttribute("local-name");
		setDefaultResult(DomUtils.getElementByTagName(suite, "defaultResult").getTextContent());
		startingTest = new QName(namespaceUri, localName, prefix);
		Element form_e = DomUtils.getElementByTagNameNS(suite, Test.CTL_NS, "form");
		if (form_e != null) {
			form = DomUtils.createDocument(form_e);
		}
	}

	public QName getStartingTest() {
		return startingTest;
	}

	public void setStartingTest(QName startingTest) {
		this.startingTest = startingTest;
	}

	public Document getForm() {
		return form;
	}

	public void setForm(Document form) {
		this.form = form;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDataLink() {
		return dataLink;
	}

	public void setDataLink(String dataLink) {
		this.dataLink = dataLink;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDefaultResult() {
		return defaultResult;
	}

	public void setDefaultResult(String defaultResult) {
		this.defaultResult = defaultResult;
	}

}
