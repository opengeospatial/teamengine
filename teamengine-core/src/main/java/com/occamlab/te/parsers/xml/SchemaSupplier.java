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

import java.io.File;
import java.net.URL;

import javax.xml.transform.Source;

/**
 * Supplies an XML schema document. Instances may store the document itself or just a
 * reference to it. Call {@link #makeSource()} either way to get access to the document.
 */
public interface SchemaSupplier {

	/**
	 * Constructs a new XML Source supplying the schema document.
	 * @return never null
	 */
	Source makeSource();

	/**
	 * Returns a {@link SchemaSupplier} that will use the given object.
	 * @param schemaObject must be a {@link File}, {@link URL}, or char array
	 * @return never null
	 * @throws IllegalArgumentException if schemaObject is not one of the specified types
	 */
	static SchemaSupplier makeSupplier(Object schemaObject) {
		if (schemaObject instanceof File) {
			return new FileSchemaSupplier((File) schemaObject);
		}
		else if (schemaObject instanceof URL) {
			return new UrlSchemaSupplier((URL) schemaObject);
		}
		else if (schemaObject instanceof char[]) {
			return new InMemorySchemaSupplier((char[]) schemaObject);
		}
		else {
			throw new IllegalArgumentException("Unknown schema reference: " + schemaObject.toString());
		}
	}

}
