package com.occamlab.te.parsers.xml;

import java.io.CharArrayReader;
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
	 * 
	 * @param pChars
	 *            the characters making up the schema document. Must not be null.
	 */
	public InMemorySchemaSupplier(final char[] pChars) {
		chars = Objects.requireNonNull(pChars);
	}

	@Override
	public Source makeSource() {
		return new StreamSource(new CharArrayReader(chars));
	}
}
