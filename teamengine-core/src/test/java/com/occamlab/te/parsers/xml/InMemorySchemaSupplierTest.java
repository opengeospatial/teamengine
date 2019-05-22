package com.occamlab.te.parsers.xml;

import static org.junit.Assert.*;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Automated unit tests for {@link InMemorySchemaSupplier}.
 */
public class InMemorySchemaSupplierTest {

	@Test
	public void testToString() {
		final char[] array = new char[] { '1', '2', '3', '4', '5' };
		final InMemorySchemaSupplier iut = new InMemorySchemaSupplier(array);
		assertTrue("string should contain number of chars",
				iut.toString().contains(String.valueOf(array.length)));
	}

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(InMemorySchemaSupplier.class).verify();
	}

}
