package com.occamlab.te.spi.jaxrs;

import javax.xml.transform.Source;
import org.w3c.dom.Document;

/**
 * Controls execution of a test suite. An executable test suite (ETS) is
 * distinguished by an alphanumeric code and a version.
 */
public interface TestSuiteController {

    /**
     * Returns the ETS code.
     * 
     * @return A String containing an alphanumeric code value. It cannot start
     *         with a digit.
     */
    public String getCode();

    /**
     * Returns the version of this ETS.
     * 
     * @return A String indicating the version; it complies with the Maven
     *         versioning scheme.
     */
    public String getVersion();

    /**
     * Returns the title of this ETS.
     * 
     * @return A String denoting the title.
     */
    public String getTitle();

    /**
     * Executes a test run and returns the results.
     * 
     * @param testRunArgs
     *            A DOM Document conveying the test run arguments. The content
     *            is implementation-specific.
     * @return A Source object that supplies an XML representation of the test
     *         results.
     * 
     * @throws Exception
     *             If the supplied test run arguments are invalid for any
     *             reason.
     */
    public Source doTestRun(Document testRunArgs) throws Exception;
}
