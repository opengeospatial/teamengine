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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.occamlab.te.util.DomUtils;

public class ParserEntry extends IndexEntry {

	boolean initialized;

	String className;

	String method;

	List<Node> classParams = null;

	ParserEntry() {
		super();
	}

	ParserEntry(Element parser) throws Exception {
		super(parser);
		Element e = (Element) parser.getElementsByTagName("java").item(0);
		if (e != null) {
			setClassName(e.getAttribute("class"));
			setMethod(e.getAttribute("method"));
			setInitialized(Boolean.parseBoolean(e.getAttribute("initialized")));
			NodeList nl = e.getElementsByTagName("with-param");
			if (nl.getLength() > 0) {
				setInitialized(true);
				classParams = new ArrayList<>();
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element) nl.item(i);
					// System.out.println(DomUtils.serializeNode(el));
					Node value = null;
					NodeList children = el.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Node n = children.item(j);
						if (n.getNodeType() == Node.TEXT_NODE) {
							value = n;
						}
						if (n.getNodeType() == Node.ELEMENT_NODE) {
							value = DomUtils.createDocument(n);
							break;
						}
					}
					classParams.add(value);
				}
			}
		}
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<Node> getClassParams() {
		return classParams;
	}

	public void setClassParams(List<Node> classParams) {
		this.classParams = classParams;
	}

}
