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

import org.w3c.dom.Element;

public class IndexEntry implements NamedEntry {

	QName qname = null;

	IndexEntry() {
	}

	IndexEntry(Element el) {
		String prefix = el.getAttribute("prefix");
		String namespaceUri = el.getAttribute("namespace-uri");
		String localName = el.getAttribute("local-name");
		setQName(new QName(namespaceUri, localName, prefix));
	}

	public String getName() {
		if (qname == null) {
			return null;
		}
		String prefix = qname.getPrefix();
		if (prefix == null) {
			return qname.getLocalPart();
		}
		else {
			return prefix + ":" + qname.getLocalPart();
		}
	}

	public String getLocalName() {
		return qname.getLocalPart();
	}

	public String getNamespaceURI() {
		return qname.getNamespaceURI();
	}

	public String getPrefix() {
		return qname.getPrefix();
	}

	public QName getQName() {
		return qname;
	}

	public void setQName(QName qname) {
		this.qname = qname;
	}

	public String getId() {
		return "{" + qname.getNamespaceURI() + "}" + qname.getLocalPart();
	}

	public String toString() {
		if (qname == null) {
			return super.toString();
		}
		return qname.toString();
	}

}
