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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.Test;
import com.occamlab.te.util.DomUtils;

public class ProfileEntry extends IndexEntry {

	String defaultResult = "Pass";

	QName baseSuite;

	List<List<QName>> excludes = new ArrayList<>();

	QName startingTest;

	Document form = null;

	String title = null;

	String description = null;

	public ProfileEntry() {
		super();
	}

	public ProfileEntry(Element profile) throws Exception {
		super(profile);
		title = DomUtils.getElementByTagName(profile, "title").getTextContent();
		description = DomUtils.getElementByTagName(profile, "description").getTextContent();
		Element base = DomUtils.getElementByTagName(profile, "base");
		baseSuite = getQName(base);
		for (Element exclude : DomUtils.getElementsByTagName(profile, "exclude")) {
			ArrayList<QName> list = new ArrayList<>();
			for (Element test : DomUtils.getElementsByTagName(exclude, "test")) {
				list.add(getQName(test));
			}
			excludes.add(list);
		}
		setDefaultResult(DomUtils.getElementByTagName(profile, "defaultResult").getTextContent());
		Element e = DomUtils.getElementByTagName(profile, "starting-test");
		startingTest = getQName(e);
		Element form_e = DomUtils.getElementByTagNameNS(profile, Test.CTL_NS, "form");
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

	public QName getBaseSuite() {
		return baseSuite;
	}

	public void setBaseSuite(QName baseSuite) {
		this.baseSuite = baseSuite;
	}

	public List<List<QName>> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<List<QName>> excludes) {
		this.excludes = excludes;
	}

	QName getQName(Element e) {
		String prefix = e.getAttribute("prefix");
		String namespaceUri = e.getAttribute("namespace-uri");
		String localName = e.getAttribute("local-name");
		return new QName(namespaceUri, localName, prefix);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefaultResult() {
		return defaultResult;
	}

	public void setDefaultResult(String defaultResult) {
		this.defaultResult = defaultResult;
	}

}
