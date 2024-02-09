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
	 * @param pUrl the {@link URL} of the schema. Must not be null.
	 */
	public UrlSchemaSupplier(final URL pUrl) {
		url = Objects.requireNonNull(pUrl);
	}

	public URL getUrl() {
		return url;
	}

	@Override
	public Source makeSource() {
		return new StreamSource(url.toString());
	}

	/**
	 * Returns a string representation of this instance. The format is unspecified, but it
	 * will contain the URL.
	 */
	@Override
	public String toString() {
		return String.format("%s{%s}", getClass().getSimpleName(), url);
	}

	/**
	 * Implements value equality based on the URL itself.
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof UrlSchemaSupplier) {
			final UrlSchemaSupplier other = (UrlSchemaSupplier) obj;
			return this.getUrl().equals(other.getUrl());
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return getUrl().hashCode();
	}

}
