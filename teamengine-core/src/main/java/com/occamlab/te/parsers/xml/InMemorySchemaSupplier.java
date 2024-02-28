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

import java.io.CharArrayReader;
import java.util.Arrays;
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Supplies an XML schema that is stored in memory.
 */
public class InMemorySchemaSupplier implements SchemaSupplier {

	// Is there any reason not to just use a String here?
	private final char[] chars;

	/**
	 * Constructs an instance.
	 * @param pChars the characters making up the schema document. Must not be null.
	 */
	public InMemorySchemaSupplier(final char[] pChars) {
		chars = Objects.requireNonNull(pChars);
	}

	@Override
	public Source makeSource() {
		return new StreamSource(new CharArrayReader(chars));
	}

	/**
	 * Returns a string representation of this instance. The format is unspecified, but it
	 * will contain the number of stored characters.
	 */
	@Override
	public String toString() {
		return String.format("%s{%d chars}", getClass().getSimpleName(), chars.length);
	}

	/**
	 * Implements value equality based on the stored characters.
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof InMemorySchemaSupplier) {
			final InMemorySchemaSupplier other = (InMemorySchemaSupplier) obj;
			return Arrays.equals(this.chars, other.chars);
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Arrays.hashCode(chars);
	}

}
