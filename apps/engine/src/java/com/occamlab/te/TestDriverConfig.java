package com.occamlab.te;

import java.io.File;
import java.util.List;

/**
 * Specifies the configuration of a test driver.
 * 
 * @author rmartell
 */
public class TestDriverConfig {

    private String suiteName;

    private String sessionId;

    private List<File> ctlFiles;

    private File logDir;

    private int mode = Test.TEST_MODE;

    private boolean validationFlag = true;

    private boolean webAppContext = false;

    /**
     * Default (empty) constructor for TestDriverConfig.
     */
    public TestDriverConfig() {
    }

    /**
     * Creates a new instance of TestDriverConfig with a core set of
     * configuration data elements.
     */
    public TestDriverConfig(String suiteName, String sessionId,
            List<File> ctlFiles, File logDir, boolean validate, int mode) {
        this.suiteName = suiteName;
        this.ctlFiles = ctlFiles;
        this.logDir = logDir;
        if (null == logDir) {
            this.logDir = new File(System.getProperty("java.io.tmpdir"));
        }
        this.validationFlag = validate;
        this.mode = mode;
        setSessionId(sessionId);
    }

    /**
     * Returns the name of the test suite configured for the driver.
     * 
     * @return a String specifying the name of the test suite.
     */
    public String getSuiteName() {
        return suiteName;
    }

    /**
     * Sets the name of the test suite for the driver.
     * 
     * @param suiteName
     *            the name of the test suite.
     */
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    /**
     * 
     * @return a String specifying the session identifier.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the sessionId.
     * 
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        if ((sessionId == null) || (sessionId.length() == 0)) {
            if (mode != Test.RESUME_MODE) {
                this.sessionId = generateSessionId(this.logDir);
            }
        }
    }

    /**
     * Returns the list of files and directories containing CTL packages.
     * 
     * @return a List of File items.
     */
    public List<File> getSources() {
        return ctlFiles;
    }

    /**
     * 
     * @param sources
     *            a List<File> containing CTL sources.
     */
    public void setSources(List<File> files) {
        this.ctlFiles = files;
    }

    /**
     * 
     * @return
     */
    public File getLogDir() {
        return logDir;
    }

    /**
     * Sets the root directory for storing all test logs.
     * 
     * @param logDir
     */
    public void setLogDir(File logDir) {
        this.logDir = logDir;
    }

    /**
     * Get the test session directory. It will be created if it does not exist,
     * including any nonexistent parent directories.
     * 
     * @return a File object representing the location of the test session
     *         directory.
     */
    public File getSessionDir() {
        File sessionDir = new File(this.logDir, sessionId);
        if (!sessionDir.isDirectory()) {
            boolean created = sessionDir.mkdirs();
            if (!created) {
                // TODO: log failure to create sessionDir
            }
        }
        return sessionDir;
    }

    /**
     * 
     * @return the operational mode for the test engine (0 indicates 'test'
     *         mode).
     */
    public int getMode() {
        return mode;
    }

    /**
     * 
     * @param mode
     *            the operational mode for the test engine.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * 
     * @return true if the CTL source files will be validated; false otherwise.
     */
    public boolean hasValidationFlag() {
        return validationFlag;
    }

    /**
     * 
     * @param webApp
     *            indicates the presence a web application context.
     */
    public void setWebAppContext(boolean webApp) {
        this.webAppContext = webApp;
    }

    /**
     * Indicates whether the tests shall be run through a web-based front end.
     * 
     * @return true if a web application context exists; false otherwise.
     */
    public boolean hasWebAppContext() {
        return webAppContext;
    }

    /**
     * 
     * @param validate
     *            indicates whether CTL source files will be validated.
     */
    public void setValidationFlag(boolean validate) {
        this.validationFlag = validate;
    }

    /**
     * Presents a summary of test session metadata.
     * 
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        final String newLine = System.getProperty("line.separator");

        result.append(this.getClass().getName() + " {");
        result.append(newLine);
        result.append("Suite name: ");
        result.append(suiteName);
        result.append(newLine);

        result.append(" Session ID: ");
        result.append(sessionId);
        result.append(newLine);

        result.append(" Log directory: ");
        result.append(logDir);
        result.append(newLine);

        result.append(" Validation flag: ");
        result.append(validationFlag);
        result.append(newLine);

        result.append(" Mode: ");
        result.append(mode);
        result.append(newLine);

        result.append(" CTL sources: ");
        result.append(ctlFiles);
        result.append(newLine);

        result.append("}");

        return result.toString();
    }

    /**
     * Checks that all class invariants are satisfied.
     * <ul>
     * <li>sessionId value must exist.</li>
     * <li>logDir must identify an existing directory.</li>
     * <li>ctlFiles must contain more than 1 item.</li>
     * </ul>
     * 
     * @return true if all constraints are satisfied; false otherwise.
     */
    private boolean validState() {
        // TODO: Implement these checks.
        return true;
    }

    /**
     * Generates a session identifier. The value corresponds to the name of a
     * sub-directory (session) in the root test log directory.
     * 
     * @return a session id string ("s0001" by default, unless the session
     *         sub-directory already exists).
     */
    public static String generateSessionId(File logDir) {
        int i = 1;
        String session = "s0001";
        while (new File(logDir, session).exists() && i < 10000) {
            i++;
            session = "s" + Integer.toString(10000 + i).substring(1);
        }
        return session;
    }

}
