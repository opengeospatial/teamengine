package com.occamlab.te.parsers.xml;

import java.io.File;
import java.net.URL;

import javax.xml.transform.Source;

/**
 * Supplies an XML schema document. Instances may store the document itself or
 * just a reference to it. Call {@link #makeSource()} either way to get access
 * to the document.
 */
public interface SchemaSupplier {

	/**
	 * Constructs a new XML Source supplying the schema document.
	 * @return never null
	 */
	public Source makeSource();

	/**
	 * Returns a {@link SchemaSupplier} that will use the given object.
	 * 
	 * @param schemaObject
	 *            must be a {@link File}, {@link URL}, or char array
	 * @return never null
	 * @throws IllegalArgumentException
	 *             if schemaObject is not one of the specified types
	 */
	public static SchemaSupplier makeSupplier(Object schemaObject) {
		if (schemaObject instanceof File) {
			return new FileSchemaSupplier((File)schemaObject);
		} else if (schemaObject instanceof URL) {
			return new UrlSchemaSupplier((URL)schemaObject);
		} else if (schemaObject instanceof char[]) {
			return new InMemorySchemaSupplier((char[])schemaObject);
		} else {
			throw new IllegalArgumentException(
					"Unknown schema reference: " + schemaObject.toString());
		}
	}

}
