package com.occamlab.te.spi.jaxrs;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A registry of executable test suites that implements a discovery and
 * instantiation mechanism for known TestSuiteController implementations. The
 * service provider mechanism is used to discover test suites. Only one registry
 * is created.
 * 
 * @see java.util.ServiceLoader ServiceLoader
 */
public class TestSuiteRegistry {

    private static final Logger logger = Logger
            .getLogger(TestSuiteRegistry.class.getPackage().getName());
    /**
     * A singleton instance of the registry.
     */
    private static volatile TestSuiteRegistry registry;
    /**
     * The set of available TestSuiteController implementations.
     */
    private Set<TestSuiteController> controllers;

    /**
     * Returns a singleton registry instance in a lazy (but thread-safe) manner.
     * 
     * @return the <code>TestSuiteRegistry</code> instance.
     */
    public static TestSuiteRegistry getInstance() {

        // Employ a "double-checked locking" strategy because a lock is only
        // needed upon initialization; synchronize on the monitor belonging to
        // the class itself.
        if (null == registry) {
            synchronized (TestSuiteRegistry.class) {
                // check again, because the thread might have been preempted
                // just after the outer if was processed but before the
                // synchronized statement was executed
                if (registry == null) {
                    registry = new TestSuiteRegistry();
                }
            }
        }
        return registry;
    }

    public Set<TestSuiteController> getControllers() {
        return controllers;
    }

    /**
     * Gets the controller for a specified executable test suite (ETS).
     * 
     * @param etsCode
     *            The alphanumeric code for the ETS.
     * @param etsVersion
     *            The version of the ETS.
     * @return A TestSuiteController, or <code>null></code> if one cannot be
     *         found.
     */
    public TestSuiteController getController(String etsCode, String etsVersion) {
        if (etsCode.length() == 0 || etsCode == null) {
            throw new IllegalArgumentException("ETS code not specified.");
        }
        if (etsVersion.length() == 0 || etsVersion == null) {
            throw new IllegalArgumentException("ETS version not specified.");
        }
        TestSuiteController controller = null;
        if (!controllers.isEmpty()) {
            for (TestSuiteController ets : this.controllers) {
                if (ets.getCode().equalsIgnoreCase(etsCode)
                        && ets.getVersion().equalsIgnoreCase(etsVersion)) {
                    controller = ets;
                    break;
                }
            }
        }
        return controller;
    }

    private TestSuiteRegistry() {
        this.controllers = new HashSet<TestSuiteController>();
        loadControllers();
    }

    private void loadControllers() {
        ClassLoader loader = this.getClass().getClassLoader();
        ServiceLoader<TestSuiteController> srvLoader = ServiceLoader.load(
                TestSuiteController.class, loader);
        for (TestSuiteController controller : srvLoader) {
            this.controllers.add(controller);
        }
        logger.log(Level.CONFIG,
                "Loaded {0} TestSuiteController implementations.",
                controllers.size());
    }
}
