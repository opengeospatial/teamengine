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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TemplateEntry extends IndexEntry {

	File templateFile = null;

	boolean usesContext;

	List<QName> params = null;

	TemplateEntry() {
		super();
	}

	TemplateEntry(Element template) {
		super(template);
		try {
			String file = template.getAttribute("file");
			if (file != null && file.length() > 0) {
				setTemplateFile(new File(new URI(template.getAttribute("file"))));
			}
			NodeList nl = template.getElementsByTagName("param");
			params = new ArrayList<>();
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				String prefix = el.getAttribute("prefix");
				String namespaceUri = el.getAttribute("namespace-uri");
				String localName = el.getAttribute("local-name");
				params.add(new QName(namespaceUri, localName, prefix));
			}
			setUsesContext(Boolean.parseBoolean(template.getAttribute("uses-context")));
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	// public void persistAttributes(PrintWriter out) {
	// super.persistAttributes(out);
	// try {
	// out.print(" file=\"" + templateFile.toURI().toURL().toString() + "\""
	// + " uses-context=\"" + Boolean.toString(usesContext) + "\"");
	// } catch (MalformedURLException e) {
	// throw new RuntimeException(e);
	// }
	// }
	//
	// public void persistTags(PrintWriter out) {
	// super.persistTags(out);
	// for (QName qname : params) {
	// out.println("<param prefix=\"" + qname.getPrefix() + "\"" +
	// " namespace-uri=\"" + qname.getNamespaceURI() + "\"" +
	// " local-name=\"" + qname.getLocalPart() + "\"/>");
	// }
	// }

	public File getTemplateFile() {
		return templateFile;
	}

	public void setTemplateFile(File templateFile) {
		this.templateFile = templateFile;
	}

	public List<QName> getParams() {
		return params;
	}

	public void setParams(List<QName> params) {
		this.params = params;
	}

	public boolean usesContext() {
		return usesContext;
	}

	public void setUsesContext(boolean usesContext) {
		this.usesContext = usesContext;
	}

	// static boolean freeExecutable() {
	// Set<String> keys = Globals.loadedExecutables.keySet();
	// synchronized(Globals.loadedExecutables) {
	// Iterator<String> it = keys.iterator();
	// if (it.hasNext()) {
	// Globals.loadedExecutables.remove(it.next());
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// public XsltExecutable loadExecutable() throws SaxonApiException {
	// String key = getId();
	// XsltExecutable executable = Globals.loadedExecutables.get(key);
	// while (executable == null) {
	// try {
	// // System.out.println(template.getTemplateFile().getAbsolutePath());
	// Source source = new StreamSource(getTemplateFile());
	// executable = Globals.compiler.compile(source);
	// Globals.loadedExecutables.put(key, executable);
	// } catch (OutOfMemoryError e) {
	// boolean freed = freeExecutable();
	// if (!freed) {
	// throw e;
	// }
	// }
	// }
	//
	// Runtime rt = Runtime.getRuntime();
	// while (rt.totalMemory() - rt.freeMemory() > Globals.memThreshhold) {
	// boolean freed = freeExecutable();
	// if (!freed) {
	// break;
	// }
	// }
	//
	// return executable;
	// }

}
