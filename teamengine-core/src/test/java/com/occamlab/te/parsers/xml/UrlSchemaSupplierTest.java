package com.occamlab.te.parsers.xml;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Automated unit tests for {@link UrlSchemaSupplier}.
 */
public class UrlSchemaSupplierTest {

	@Test
	public void testToString() throws MalformedURLException {
		final URL url = new URL("http://att.com");
		final UrlSchemaSupplier iut = new UrlSchemaSupplier(url);

		assertTrue("should contain filename", iut.toString().contains(url.toString()));
	}

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(UrlSchemaSupplier.class)
			.withNonnullFields("url")
			.verify();
	}

}
