package com.occamlab.te.parsers.xml;

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

import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

/**
 * <p>
 * Loads W3C XML {@link Schema} objects from sources.
 * </p>
 *
 * <p>
 * Immutable and thread-safe: instances of this class can be used by multiple threads
 * concurrently.
 * </p>
 */
public class XsdSchemaLoader implements SchemaLoader {

	private static final Logger LOGGER = Logger.getLogger(XsdSchemaLoader.class.getName());

	// Guarded by "this"
	private final SchemaFactory schemaFactory;

	public XsdSchemaLoader() {
		String property_name = "javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
		String oldprop = System.getProperty(property_name);
		System.setProperty(property_name, "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
		schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			schemaFactory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
		}
		catch (Exception e) {
			LOGGER.warning("Unable to set feature '*/schema-full-checking'");
		}
		if (oldprop == null) {
			System.clearProperty(property_name);
		}
		else {
			System.setProperty(property_name, oldprop);
		}
	}

	@Override
	public Schema loadSchema(final ImmutableList<SchemaSupplier> suppliers) throws SAXException {
		LOGGER.fine("Loading unified schema from suppliers: " + suppliers);
		Source[] schemaSources = new Source[suppliers.size()];
		for (int i = 0; i < suppliers.size(); i++) {
			schemaSources[i] = suppliers.get(i).makeSource();
		}
		synchronized (this) {
			return schemaFactory.newSchema(schemaSources);
		}
	}

	@Override
	public Schema defaultSchema() throws SAXException {
		synchronized (this) {
			return schemaFactory.newSchema();
		}
	}

}
