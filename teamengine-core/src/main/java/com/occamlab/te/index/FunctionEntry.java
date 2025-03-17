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

public class FunctionEntry extends TemplateEntry {

	boolean java;

	boolean initialized;

	String className;

	String method;

	int minArgs;

	int maxArgs;

	List<Node> classParams = null;

	FunctionEntry() {
		super();
	}

	// public void persistAttributes(PrintWriter out) {
	// super.persistAttributes(out);
	// out.print(" type=\"" + (java ? "java" : "xsl") + "\"");
	// }
	//
	// public void persistTags(PrintWriter out) {
	// super.persistTags(out);
	// if (minArgs != maxArgs) {
	// int min = minArgs;
	// int max = maxArgs;
	// if (getParams() != null) {
	// min -= getParams().size();
	// max -= getParams().size();
	// }
	// if (usesContext()) {
	// min--;
	// max--;
	// }
	// out.println("<varags min=\"" + Integer.toString(min) + "\"" +
	// " max=\"" + Integer.toString(max) + "\"/>");
	// }
	// }
	//
	// public void persist(PrintWriter out) {
	// persist(out, "function");
	// }

	FunctionEntry(Element function) {
		super(function);
		// System.out.println(DomUtils.serializeNode(function));
		// try {
		String type = function.getAttribute("type");
		if (type.equals("xsl")) {
			setJava(false);
			// setTemplateFile(new File(new
			// URI(function.getAttribute("file"))));
		}
		else if (type.equals("java")) {
			// System.out.println(this.getId());
			setJava(true);
		}
		else {
			throw new RuntimeException("Invalid function type");
		}
		// NodeList nl = function.getElementsByTagName("param");
		// minArgs = nl.getLength();
		minArgs = 0;
		if (this.getParams() != null) {
			minArgs = this.getParams().size();
		}
		maxArgs = minArgs;
		// params = new ArrayList<QName>();
		// if (minArgs > 0) {
		// for (int i = 0; i < minArgs; i++) {
		// Element el = (Element)nl.item(i);
		// String prefix = el.getAttribute("prefix");
		// String namespaceUri = el.getAttribute("namespace-uri");
		// String localName = el.getAttribute("local-name");
		// params.add(new QName(namespaceUri, localName, prefix));
		// }
		// }
		NodeList nl = function.getElementsByTagName("var-params");
		if (nl.getLength() > 0) {
			Element varParams = (Element) nl.item(0);
			String min = varParams.getAttribute("min");
			if (min != null) {
				minArgs += Integer.parseInt(min);
			}
			String max = varParams.getAttribute("max");
			if (max != null) {
				maxArgs += Integer.parseInt(max);
			}
		}
		// setUsesContext(Boolean.parseBoolean(function.getAttribute("uses-context")));
		if (usesContext()) {
			minArgs++;
			maxArgs++;
		}
		Element e = (Element) function.getElementsByTagName("java").item(0);
		if (e != null) {
			setClassName(e.getAttribute("class"));
			setMethod(e.getAttribute("method"));
			setInitialized(Boolean.parseBoolean(e.getAttribute("initialized")));
			nl = e.getElementsByTagName("with-param");
			if (initialized && nl.getLength() > 0) {
				classParams = new ArrayList<>();
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element) nl.item(i);
					Node value = null;
					NodeList children = el.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Node n = children.item(j);
						if (n.getNodeType() == Node.TEXT_NODE) {
							value = n;
						}
						if (n.getNodeType() == Node.ELEMENT_NODE) {
							value = n;
							break;
						}
					}
					classParams.add(value);
				}
			}
		}
		// } catch (URISyntaxException e) {
		// throw new RuntimeException(e);
		// }
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

	public boolean isJava() {
		return java;
	}

	public void setJava(boolean java) {
		this.java = java;
	}

	public int getMaxArgs() {
		return maxArgs;
	}

	public void setMaxArgs(int maxArgs) {
		this.maxArgs = maxArgs;
	}

	public int getMinArgs() {
		return minArgs;
	}

	public void setMinArgs(int minArgs) {
		this.minArgs = minArgs;
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
