/*

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s):
 	C. Heazel (WiSC): Added Fortify adjudication changes
        C. Heazel (WiSC): Modified setElements() to correctly handle a null argumet
 */

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

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.occamlab.te.util.DomUtils;

public class Index {

	File indexFile = null;

	List<File> dependencies = new ArrayList<>();

	Map<String, List<FunctionEntry>> functionsMap = new HashMap<>();

	Map<String, ParserEntry> parserMap = new HashMap<>();

	Map<String, SuiteEntry> suiteMap = new HashMap<>();

	Map<String, ProfileEntry> profileMap = new HashMap<>();

	Map<String, TestEntry> testMap = new HashMap<>();

	List<Element> elements = new ArrayList<>();

	public Index() {
	}

	public Index(File indexFile) throws Exception {
		if (null == indexFile || !indexFile.exists()) {
			throw new IllegalArgumentException("indexFile is null or does not exist.");
		}
		this.indexFile = indexFile;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		// Fortify Mod: prevent external entity injection
		dbf.setExpandEntityReferences(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(indexFile);
		Element index = (Element) doc.getDocumentElement();
		NodeList nodes = index.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element el = (Element) node;
				elements.add(el);
				String name = el.getNodeName();
				if (name.equals("dependency")) {
					File file = new File(el.getAttribute("file").substring(5));
					dependencies.add(file);
				}
				else if (name.equals("suite")) {
					SuiteEntry se = new SuiteEntry(el);
					suiteMap.put(se.getId(), se);
				}
				else if (name.equals("profile")) {
					ProfileEntry pe = new ProfileEntry(el);
					profileMap.put(pe.getId(), pe);
				}
				else if (name.equals("test")) {
					TestEntry te = new TestEntry(el);
					testMap.put(te.getId(), te);
				}
				else if (name.equals("function")) {
					FunctionEntry fe = new FunctionEntry(el);
					List<FunctionEntry> functions = functionsMap.get(fe.getId());
					if (functions == null) {
						functions = new ArrayList<>();
						functions.add(fe);
						functionsMap.put(fe.getId(), functions);
					}
					else {
						functions.add(fe);
					}
				}
				else if (name.equals("parser")) {
					ParserEntry pe = new ParserEntry(el);
					parserMap.put(pe.getId(), pe);
				}
			}
		}
	}

	public void persist(File file) throws Exception {
		file.getParentFile().mkdirs();
		PrintWriter out = new PrintWriter(file);
		out.println("<index>");
		for (Element el : elements) {
			out.println(DomUtils.serializeNode(el));
		}
		out.println("</index>");
		out.close();
	}

	public boolean outOfDate() {
		if (indexFile != null) {
			long indexDate = indexFile.lastModified();
			for (File file : dependencies) {
				if (file.lastModified() + 1000 > indexDate) {
					return true;
				}
			}
		}
		return false;
	}

	public void add(Index index) {
		elements.addAll(index.elements);
		dependencies.addAll(index.dependencies);
		functionsMap.putAll(index.functionsMap);
		suiteMap.putAll(index.suiteMap);
		profileMap.putAll(index.profileMap);
		testMap.putAll(index.testMap);
		parserMap.putAll(index.parserMap);
	}

	public List<FunctionEntry> getFunctions(String name) {
		if (name.startsWith("{")) {
			return functionsMap.get(name);
		}
		throw new RuntimeException("Invalid function name");
	}

	public List<FunctionEntry> getFunctions(QName qname) {
		return getFunctions("{" + qname.getNamespaceURI() + "}" + qname.getLocalPart());
	}

	public Set<String> getFunctionKeys() {
		return functionsMap.keySet();
	}

	public ParserEntry getParser(String name) {
		return (ParserEntry) getEntry(parserMap, name);
	}

	public ParserEntry getParser(QName qname) {
		return (ParserEntry) getEntry(parserMap, qname);
	}

	public Set<String> getParserKeys() {
		return parserMap.keySet();
	}

	public SuiteEntry getSuite(String name) {
		return (SuiteEntry) getEntry(suiteMap, name);
	}

	public SuiteEntry getSuite(QName qname) {
		return (SuiteEntry) getEntry(suiteMap, qname);
	}

	public Set<String> getSuiteKeys() {
		return suiteMap.keySet();
	}

	public ProfileEntry getProfile(String name) {
		return (ProfileEntry) getEntry(profileMap, name);
	}

	public ProfileEntry getProfile(QName qname) {
		return (ProfileEntry) getEntry(profileMap, qname);
	}

	public Set<String> getProfileKeys() {
		return profileMap.keySet();
	}

	public Collection<ProfileEntry> getProfiles() {
		return profileMap.values();
	}

	public TestEntry getTest(String name) {
		return (TestEntry) getEntry(testMap, name);
	}

	public TestEntry getTest(QName qname) {
		return (TestEntry) getEntry(testMap, qname);
	}

	public Set<String> getTestKeys() {
		return testMap.keySet();
	}

	private IndexEntry getEntry(Map<String, ? extends IndexEntry> map, QName qname) {
		return getEntry(map, "{" + qname.getNamespaceURI() + "}" + qname.getLocalPart());
	}

	private IndexEntry getEntry(Map<String, ? extends IndexEntry> map, String name) {
		if (name == null) {
			return map.values().iterator().next();
		}

		if (name.startsWith("{")) {
			return map.get(name);
		}

		int i = name.lastIndexOf(',');
		if (i >= 0) {
			String key = "{" + name.substring(0, i) + "}" + name.substring(i + 1);
			return map.get(key);
		}

		String prefix = null;
		String localName = name;
		i = name.indexOf(':');
		if (i >= 0) {
			prefix = name.substring(0, i);
			localName = name.substring(i + 1);
		}

		Iterator<? extends IndexEntry> it = map.values().iterator();
		while (it.hasNext()) {
			IndexEntry entry = it.next();
			if (entry.getLocalName().equals(localName)) {
				if (prefix == null) {
					return entry;
				}
				else {
					if (entry.getPrefix().equals(prefix)) {
						return entry;
					}
				}
			}
		}

		return null;
	}

	public void setElements(List<Element> elements) {
		// Mod to handle a null argument
		if (elements != null)
			this.elements = elements;
		else
			this.elements = new ArrayList<>();
	}

	public List<File> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<File> dependencies) {
		this.dependencies = dependencies;
	}

}
