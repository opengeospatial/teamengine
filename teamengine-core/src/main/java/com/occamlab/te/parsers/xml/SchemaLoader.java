package com.occamlab.te.parsers.xml;

import java.io.CharArrayReader;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

/**
 * <p>
 * Loads W3C XML {@link Schema} objects from sources.
 * </p>
 * 
 * <p>
 * Immutable and thread-safe: instances of this class can be used by multiple
 * threads concurrently.
 * </p>
 */
public class SchemaLoader {

	private static final Logger LOGGER =
			Logger.getLogger(SchemaLoader.class.getName());

	// Guarded by "this"
	private final SchemaFactory schemaFactory;

	public SchemaLoader() {
        String property_name = "javax.xml.validation.SchemaFactory:"
                + XMLConstants.W3C_XML_SCHEMA_NS_URI;
        String oldprop = System.getProperty(property_name);
        System.setProperty(property_name,
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schemaFactory.setFeature(
                    "http://apache.org/xml/features/validation/schema-full-checking",
                    false);
        } catch (Exception e) {
            LOGGER.warning("Unable to set feature '*/schema-full-checking'");
        }
        if (oldprop == null) {
            System.clearProperty(property_name);
        } else {
            System.setProperty(property_name, oldprop);
        }
	}

	/**
	 * Creates a {@link Schema} object for the specified schema documents.
	 * 
	 * @param xsdList
	 *            a list of {@link File}s, {@link URL}s, or char arrays providing
	 *            the schema documents. Must not be null.
	 * @return the created {@link Schema} object. Never null.
	 * @throws SAXException
	 *             if the Schema cannot be created
	 * @throws IllegalArgumentException
	 *             if the xsdList contains an invalid object type
	 */
	public Schema loadSchema(List<Object> xsdList) throws SAXException {
		Source[] schemaSources = new Source[xsdList.size()];
		for (int i = 0; i < xsdList.size(); i++) {
			Object ref = xsdList.get(i);
			if (ref instanceof File) {
				schemaSources[i] = new StreamSource((File) ref);
			} else if (ref instanceof URL) {
				schemaSources[i] = new StreamSource(ref.toString());
			} else if (ref instanceof char[]) {
				schemaSources[i] = new StreamSource(new CharArrayReader(
						(char[]) ref));
			} else {
				throw new IllegalArgumentException(
						"Unknown schema reference: " + ref.toString());
			}
		}
		synchronized (this) {
            return schemaFactory.newSchema(schemaSources);
		}
	}

	/**
	 * Returns a {@link Schema} object that performs validation by using location
	 * hints specified in documents, as in {@link SchemaFactory#newSchema()}.
	 * 
	 * @return never null, and a new object each time
	 * @throws SAXException
	 *             if the Schema object cannot be created
	 */
	public Schema defaultSchema() throws SAXException {
		synchronized (this) {
            return schemaFactory.newSchema();
		}
	}
}
