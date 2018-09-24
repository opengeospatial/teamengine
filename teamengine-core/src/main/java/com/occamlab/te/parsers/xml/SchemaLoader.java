package com.occamlab.te.parsers.xml;

import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;

/**
 * Loads {@link Schema} objects from {@link SchemaSupplier}s.
 */
public interface SchemaLoader {

	/**
	 * Creates a {@link Schema} object for the specified schema documents.
	 * 
	 * @param suppliers
	 *            a list of {@link SchemaSupplier}s supplying the schemas. Must not
	 *            be null or contain any nulls.
	 * @return the created {@link Schema} object. Never null.
	 * @throws SAXException
	 *             if the Schema cannot be created
	 */
	Schema loadSchema(ImmutableList<SchemaSupplier> suppliers) throws SAXException;

	/**
	 * Returns a {@link Schema} object that performs validation by using location
	 * hints specified in documents, as in
	 * {@link javax.xml.validation.SchemaFactory#newSchema()}.
	 * 
	 * @return never null, and a new object each time
	 * @throws SAXException
	 *             if the Schema object cannot be created
	 */
	Schema defaultSchema() throws SAXException;

}