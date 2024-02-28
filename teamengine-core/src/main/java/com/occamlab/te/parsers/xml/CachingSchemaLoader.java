package com.occamlab.te.parsers.xml;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.xml.validation.Schema;

import org.xml.sax.SAXException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

/**
 * Wraps a {@link SchemaLoader}, caching loaded {@link Schema}s to avoid repeat downloads.
 * Each instance maintains its own independent cache.
 */
public class CachingSchemaLoader implements SchemaLoader {

	private final SchemaLoader wrappedLoader;

	private final LoadingCache<ImmutableList<SchemaSupplier>, Schema> cache = CacheBuilder.newBuilder()
		.build(new CacheLoader<>() {
			@Override
			public Schema load(ImmutableList<SchemaSupplier> suppliers) throws SAXException {
				return wrappedLoader.loadSchema(suppliers);
			}
		});

	/**
	 * Constructs an instance.
	 * @param pWrappedLoader the {@link SchemaLoader} whose results will be cached. Must
	 * not be null.
	 */
	public CachingSchemaLoader(final SchemaLoader pWrappedLoader) {
		wrappedLoader = Objects.requireNonNull(pWrappedLoader);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * Returns the cached Schema for the given references, first loading the schema if not
	 * yet cached.
	 * </p>
	 */
	@Override
	public Schema loadSchema(final ImmutableList<SchemaSupplier> suppliers) throws SAXException {
		try {
			return cache.get(suppliers);
		}
		catch (final ExecutionException ex) {
			// The loader can only throw a SAXException. (See above.)
			throw (SAXException) ex.getCause();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation does not cache results. The wrapped loader will be called every
	 * time.
	 * </p>
	 */
	@Override
	public Schema defaultSchema() throws SAXException {
		return wrappedLoader.defaultSchema();
	}

}
