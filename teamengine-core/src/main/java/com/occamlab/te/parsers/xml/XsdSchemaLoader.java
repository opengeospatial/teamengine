package com.occamlab.te.parsers.xml;

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
