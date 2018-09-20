package com.occamlab.te.parsers.xml;

import java.net.URL;
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Supplies an XML schema from a URL.
 */
public class UrlSchemaSupplier implements SchemaSupplier {

	private final URL url;

	/**
	 * Constructs an instance.
	 * 
	 * @param pUrl
	 *            the {@link URL} of the schema. Must not be null.
	 */
	public UrlSchemaSupplier(final URL pUrl) {
		url = Objects.requireNonNull(pUrl);
	}

	@Override
	public Source makeSource() {
		return new StreamSource(url.toString());
	}
}
