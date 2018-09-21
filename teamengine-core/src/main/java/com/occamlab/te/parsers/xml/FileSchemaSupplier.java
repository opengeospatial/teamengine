package com.occamlab.te.parsers.xml;

import java.io.File;
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Supplies an XML schema from a file.
 */
public class FileSchemaSupplier implements SchemaSupplier {

	private final File file;

	/**
	 * Constructs an instance.
	 * 
	 * @param pFile
	 *            the {@link File} containing the schema. Must not be null.
	 */
	public FileSchemaSupplier(final File pFile) {
		file = Objects.requireNonNull(pFile);
	}

	public File getFile() {
		return file;
	}

	@Override
	public Source makeSource() {
		return new StreamSource(file);
	}

	/**
	 * Returns a string representation of this instance. The format is unspecified,
	 * but it will contain the file path.
	 */
	@Override
	public String toString() {
		return String.format("%s{%s}", getClass().getSimpleName(), file);
	}

	/**
	 * Implements value equality based on the file reference.
	 */
	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof FileSchemaSupplier) {
			final FileSchemaSupplier other = (FileSchemaSupplier)obj;
			return this.getFile().equals(other.getFile());
		}

		return false;
	}

	@Override
	public final int hashCode() {
		return getFile().hashCode();
	}
}
