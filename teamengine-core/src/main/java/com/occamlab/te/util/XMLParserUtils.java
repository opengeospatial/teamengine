package com.occamlab.te.util;

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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.impl.Constants;

/**
 * Provides various utility methods for constructing JAXP parsers.
 *
 */
public class XMLParserUtils {

	/**
	 * Creates a SAXParser that is configured to resolve XInclude references but not
	 * perform schema validation.
	 * @param doBaseURIFixup A boolean value that specifies whether or not to add xml:base
	 * attributes when resolving xi:include elements; adding these attributes may render
	 * an instance document schema-invalid.
	 * @return An XInclude-aware SAXParser instance.
	 *
	 * @see <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude) Version
	 * 1.0, Second Edition</a>
	 */
	public static SAXParser createXIncludeAwareSAXParser(boolean doBaseURIFixup) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		SAXParser parser = null;
		try {
			factory.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE,
					doBaseURIFixup);
			parser = factory.newSAXParser();
		}
		catch (Exception x) {
			throw new RuntimeException(x);
		}
		return parser;
	}

	private XMLParserUtils() {
	}

}
