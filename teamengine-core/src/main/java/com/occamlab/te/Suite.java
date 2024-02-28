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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides metadata about a test suite.
 *
 */
public class Suite {

	private String prefix;

	private String namespaceUri;

	private String localName;

	private String title;

	private String description;

	private String startingTestPrefix;

	private String startingTestNamespaceUri;

	private String startingTestLocalName;

	private String link;

	private String dataLink;

	private String version;

	public Suite(Element suiteElement) {
		String name = suiteElement.getAttribute("name");
		this.version = suiteElement.getAttribute("version");

		int colon = name.indexOf(":");
		prefix = name.substring(0, colon);
		localName = name.substring(colon + 1);
		namespaceUri = suiteElement.lookupNamespaceURI(prefix);

		NodeList titleElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "title");
		title = ((Element) titleElements.item(0)).getTextContent();

		NodeList descElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "description");
		if (descElements.getLength() > 0) {
			description = ((Element) descElements.item(0)).getTextContent();
		}
		else {
			description = null;
		}

		NodeList linkElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "link");
		for (int i = 0; i < linkElements.getLength(); i++) {
			Element linkElem = (Element) linkElements.item(i);
			String linkText = linkElem.getTextContent();
			if (linkText.startsWith("data")) {
				this.dataLink = linkText;
			}
			else {
				this.link = linkText;
			}
		}

		NodeList startingTestElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "starting-test");
		name = ((Element) startingTestElements.item(0)).getTextContent();
		colon = name.indexOf(":");
		startingTestPrefix = name.substring(0, colon);
		startingTestLocalName = name.substring(colon + 1);
		startingTestNamespaceUri = suiteElement.lookupNamespaceURI(startingTestPrefix);
	}

	public String getKey() {
		return namespaceUri + "," + localName;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	public String getLocalName() {
		return localName;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getLink() {
		return this.link;
	}

	public String getDataLink() {
		return this.dataLink;
	}

	public String getStartingTestPrefix() {
		return startingTestPrefix;
	}

	public String getStartingTestNamespaceUri() {
		return startingTestNamespaceUri;
	}

	public String getStartingTestLocalName() {
		return startingTestLocalName;
	}

	public String getVersion() {
		return this.version;
	}

}
