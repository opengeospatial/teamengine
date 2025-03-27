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

import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

/**
 * Loads {@link Schema} objects from {@link SchemaSupplier}s.
 */
public interface SchemaLoader {

	/**
	 * Creates a {@link Schema} object for the specified schema documents.
	 * @param suppliers a list of {@link SchemaSupplier}s supplying the schemas. Must not
	 * be null or contain any nulls.
	 * @return the created {@link Schema} object. Never null.
	 * @throws SAXException if the Schema cannot be created
	 */
	Schema loadSchema(ImmutableList<SchemaSupplier> suppliers) throws SAXException;

	/**
	 * Returns a {@link Schema} object that performs validation by using location hints
	 * specified in documents, as in
	 * {@link javax.xml.validation.SchemaFactory#newSchema()}.
	 * @return never null, and a new object each time
	 * @throws SAXException if the Schema object cannot be created
	 */
	Schema defaultSchema() throws SAXException;

}
