package com.occamlab.te.parsers.xml;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Automated unit tests for {@link FileSchemaSupplier}.
 */
public class FileSchemaSupplierTest {

	@Test
	public void testToString() {
		final String filename = "abcd.txt";
		final FileSchemaSupplier iut = new FileSchemaSupplier(new File(filename));

		assertTrue("should contain filename", iut.toString().contains(filename));
	}

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(FileSchemaSupplier.class).withNonnullFields("file").verify();
	}

}
