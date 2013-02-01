package com.occamlab.te.spi.executors;

import javax.xml.transform.Source;
import org.w3c.dom.Document;

/**
 * An object responsible for executing a test run.
 */
public interface TestRunExecutor {

    /**
     * Executes a test suite using the supplied test run arguments.
     * 
     * @param testRunArgs
     *            A DOM Document node that contains the test run arguments. The
     *            content of the document is implementation-specific.
     * @return A Source object that provides the (XML) results of the test run.
     * 
     * @see Source
     */
    public Source execute(Document testRunArgs);
}
