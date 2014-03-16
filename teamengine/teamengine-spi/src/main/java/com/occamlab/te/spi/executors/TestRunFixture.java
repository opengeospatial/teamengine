package com.occamlab.te.spi.executors;

import java.util.Set;

/**
 * A test run fixture. Tests can reuse items in this shared fixture, but beware
 * of undesirable interactions occurring if any tests modify the fixture. Such a
 * fixture is best suited for providing "immutable" items that only need to be
 * created once, such as metadata about the test subject or its environment,
 * pre-compiled Schema objects, etc.
 */
public interface TestRunFixture {

    /**
     * Adds an item to the fixture. If an item with the given name already
     * exists it is replaced.
     * 
     * @param name
     *            The name of the item.
     * @param value
     *            The actual item to add.
     */
    public void addItem(String name, Object value);

    /**
     * Retrieves an item from the fixture by name.
     * 
     * @param name
     *            The name of the item to return.
     * @return The item, or <code>null</code> if no corresponding item exists.
     */
    public Object getItem(String name);

    /**
     * Returns a set of all item names.
     * 
     * @return A Set containing the names of all items in the fixture.
     */
    public Set<String> listItemNames();

    /**
     * Removes an item specified by name.
     * 
     * @param name
     *            The name of the item to remove.
     */
    public void removeItem(String name);

    /**
     * Removes all items in the fixture.
     */
    public void removeAllItems();
}
