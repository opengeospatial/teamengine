package com.occamlab.te.spi.executors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages test fixtures that provide data to support the execution of a test
 * run. Such data often describe the test subject or its environment.
 */
public class FixtureManager {

    /**
     * A singleton instance of the manager.
     */
    private static volatile FixtureManager manager;
    /**
     * A collection of TestRunFixture objects, keyed by identifier (e.g. a UUID
     * value).
     */
    private Map<String, TestRunFixture> fixtures;

    /**
     * Returns a singleton manager in a lazy (but thread-safe) manner.
     * 
     * @return the <code>FixtureManager</code> instance.
     */
    public static FixtureManager getInstance() {

        // Employ a "double-checked locking" strategy because a lock is only
        // needed upon initialization; synchronize on the monitor belonging to
        // the class itself.
        if (null == manager) {
            synchronized (FixtureManager.class) {
                // check again, because the thread might have been preempted
                // just after the outer if was processed but before the
                // synchronized statement was executed
                if (manager == null) {
                    manager = new FixtureManager();
                }
            }
        }
        return manager;
    }

    /**
     * Gets the fixture for the specified test run. If runId is an empty String
     * and only one fixture exists this is returned.
     * 
     * @param runId
     *            The test run identifier (may be an empty String).
     * @return A TestRunFixture, or <code>null></code> if a matching one cannot
     *         be found.
     */
    public TestRunFixture getFixture(String runId) {
        if (runId.isEmpty() && this.fixtures.size() == 1) {
            runId = this.fixtures.keySet().iterator().next();
        }
        return fixtures.get(runId);
    }

    /**
     * Adds a fixture.
     * 
     * @param runId
     *            The test run identifier.
     * @param fixture
     *            The TestRunFixture to be added (or replaced).
     */
    public void addFixture(String runId, TestRunFixture fixture) {
        this.fixtures.put(runId, fixture);
    }

    /**
     * Removes a fixture.
     * 
     * @param runId
     *            The test run identifier.
     */
    public void removeFixture(String runId) {
        this.fixtures.remove(runId);
    }

    /**
     * Lists the identifiers of registered test run fixtures.
     * 
     * @return A Set containing fixture identifiers.
     */
    public Set<String> listFixtureIdentifiers() {
        return fixtures.keySet();
    }

    private FixtureManager() {
        this.fixtures = new HashMap<String, TestRunFixture>();
    }
}
