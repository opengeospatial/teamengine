/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.spi.executors;

/*-
 * #%L
 * TEAM Engine - Service Providers
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

import java.util.Set;

/**
 * A test run fixture. Tests can reuse items in this shared fixture, but beware of
 * undesirable interactions occurring if any tests modify the fixture. Such a fixture is
 * best suited for providing "immutable" items that only need to be created once, such as
 * metadata about the test subject or its environment, pre-compiled Schema objects, etc.
 */
public interface TestRunFixture {

	/**
	 * Adds an item to the fixture. If an item with the given name already exists it is
	 * replaced.
	 * @param name The name of the item.
	 * @param value The actual item to add.
	 */
	void addItem(String name, Object value);

	/**
	 * Retrieves an item from the fixture by name.
	 * @param name The name of the item to return.
	 * @return The item, or <code>null</code> if no corresponding item exists.
	 */
	Object getItem(String name);

	/**
	 * Returns a set of all item names.
	 * @return A Set containing the names of all items in the fixture.
	 */
	Set<String> listItemNames();

	/**
	 * Removes an item specified by name.
	 * @param name The name of the item to remove.
	 */
	void removeItem(String name);

	/**
	 * Removes all items in the fixture.
	 */
	void removeAllItems();

}
