package com.occamlab.te.parsers.xml;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

/**
 * Wraps a {@link SchemaLoader}, caching loaded {@link Schema}s to avoid repeat
 * downloads. Each instance maintains its own independent cache.
 */
public class CachingSchemaLoader implements SchemaLoader {

	private final SchemaLoader wrappedLoader;

	private final LoadingCache<ImmutableList<SchemaSupplier>, Schema> cache =
			CacheBuilder.newBuilder().build(new CacheLoader<>() {
                @Override
                public Schema load(ImmutableList<SchemaSupplier> suppliers) throws SAXException {
                    return wrappedLoader.loadSchema(suppliers);
                }
            });

	/**
	 * Constructs an instance.
	 * 
	 * @param pWrappedLoader
	 *            the {@link SchemaLoader} whose results will be cached. Must not be
	 *            null.
	 */
	public CachingSchemaLoader(final SchemaLoader pWrappedLoader) {
		wrappedLoader = Objects.requireNonNull(pWrappedLoader);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Returns the cached Schema for the given references, first loading the schema
	 * if not yet cached.
	 * </p>
	 */
	@Override
	public Schema loadSchema(final ImmutableList<SchemaSupplier> suppliers)
			throws SAXException {
		try {
			return cache.get(suppliers);
		} catch (final ExecutionException ex) {
			// The loader can only throw a SAXException. (See above.)
			throw (SAXException)ex.getCause();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implementation does not cache results. The wrapped loader will be called
	 * every time.
	 * </p>
	 */
	@Override
	public Schema defaultSchema() throws SAXException {
		return wrappedLoader.defaultSchema();
	}

}
