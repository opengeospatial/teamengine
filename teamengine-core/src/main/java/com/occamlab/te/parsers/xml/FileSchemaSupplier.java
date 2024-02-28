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
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Supplies an XML schema from a file.
 */
public class FileSchemaSupplier implements SchemaSupplier {

	private final File file;

	/**
	 * Constructs an instance.
	 * @param pFile the {@link File} containing the schema. Must not be null.
	 */
	public FileSchemaSupplier(final File pFile) {
		file = Objects.requireNonNull(pFile);
	}

	public File getFile() {
		return file;
	}

	@Override
	public Source makeSource() {
		return new StreamSource(file);
	}

	/**
	 * Returns a string representation of this instance. The format is unspecified, but it
	 * will contain the file path.
	 */
	@Override
	public String toString() {
		return String.format("%s{%s}", getClass().getSimpleName(), file);
	}

	/**
	 * Implements value equality based on the file reference.
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof FileSchemaSupplier) {
			final FileSchemaSupplier other = (FileSchemaSupplier) obj;
			return this.getFile().equals(other.getFile());
		}

		return false;
	}

	@Override
	public final int hashCode() {
		return getFile().hashCode();
	}

}
