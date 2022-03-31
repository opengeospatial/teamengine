/*

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 January 2018 - Modified all set operations to validate the input prior
 to updating the runtime properties.  This included converting these 
 operations to return a boolean vs. a void.

 Contributor(s): 
     C. Heazel (WiSC): 
        - Modifications to address Fortify issues
        - Modifications to validate parameters on set operations
 */

package com.occamlab.te;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import com.occamlab.te.util.TEPath;  // Fortify addition
import com.occamlab.te.util.LogUtils;
import java.util.regex.Pattern;      // Supports UUID validation

import net.sf.saxon.s9api.XdmNode;

/**
 * The RuntimeOptions class provides runtime configuration settings for use
 * by a test.  It is not enough for this class to hold the settings. It also
 * must assure that the settings are correct and valid.  A bad configuration
 * value will result in incorrect behavior for the test and may even crash
 * the Engine.  Therefor, this class implements the following requirements:
 * 1) A RuntimeOptions object may be used without setting any of the settings.
 *    Therefore, the constructor shall initialize all settings to valid values.
 * 2) Users may overwrite the RuntimeOption settings  Therefore, each "set" 
 *    operation shall validate its argument prior to modifying the setting.
 * 3) The integrity of the settings must be protected.  Therefore the settings
 *    can only be modified through the "set" operations.
 * 4) Users must know the state of the runtime settings.  Therefore, all
 *    operations shall return a value. 
 * <p>
 * The Runtime settings are:
 *
 *   testLogDir: This is the directory where the log file will be written.  
 *   workDir:
 *   sessionId: 
 *   suiteName: A suite is a set of tests. This is the name of the current suite
 *   testName: The name of the current test.
 *   sourcesName: 
 *   baseURI: 
 *   profiles: 
 *   testPaths:
 *   params:
 *   recordedForms: 
 * <p>
 * The configuration settings are:
 *
 *   testLogDir:  
 *     -- Constraint: Must be a valid TE directory path.
 *     -- Constraint: Null is not allowed.
 *     -- Comment: Initialized to the TE_BASE/users/<user name> directory 
 *   workDir:
 *     -- Constraint: Must be a valid TE directory path.
 *     -- Constraint: Null is not allowed.
 *     -- Comment: Initialized from setupOptions
 *   sessionId: 
 *     -- Constraint: Required format is UUID although others are tolerated for now
 *     -- Constraint: Null is not allowed.
 *   suiteName:
 *     -- Constraint: Cannot be null
 *     -- Comment: initialized to an empty string
 *   testName:
 *     -- Constraint: Cannot be null
 *     -- Comment: initialized to an empty string
 *   sourcesName: 
 *     -- Constraint: Cannot be null
 *     -- Comment: initialized to "default"
 *     -- TO-DO: determine why any other default breaks TE.
 *   baseURI: 
 *     -- Constraint: Cannot be null
 *     -- Comment: initialized to an empty string
 *   profiles: 
 *     -- Comment: initialized to an empty array list 
 *   testPaths:
 *     -- Comment: initialized to an empty array list 
 *   params:
 *     -- Comment: initialized to an empty array list 
 *   recordedForms: 
 *     -- Comment: initialized to an empty array list 
 */

public class RuntimeOptions {
    private int mode = Test.TEST_MODE;
    private File testLogDir = null;
    private File workDir = null;
    private String sessionId = UUID.randomUUID().toString();
    private String testName = "";
    private String suiteName = "";
    private String sourcesName = "default";
    private String baseURI = "";
    private ArrayList<String> profiles = new ArrayList<String>();
    private ArrayList<String> testPaths = new ArrayList<String>();
    private ArrayList<String> params = new ArrayList<String>();
    private List<File> recordedForms = new ArrayList<>();

    private static Logger jLogger = Logger.getLogger("com.occamlab.te.RuntimeOptions");
    private static final String UUID_PATTERN = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";

    /**
     * Default constructor sets the location of the test log directory to
     * TE_BASE/users/{user.name}; it is created if it does not exist.
     */
    public RuntimeOptions() {
        File baseDir = SetupOptions.getBaseConfigDirectory();
        File usersDir = new File(baseDir, "users");
        File userDir = new File(usersDir, System.getProperty("user.name"));
        if (!userDir.exists()) {
            userDir.mkdirs();
        }
        this.testLogDir = userDir;
        SetupOptions sopts = new SetupOptions();
        this.workDir = sopts.getWorkDir();
        jLogger.setLevel(Level.INFO);
    }

    public String getBaseURI() {
        return baseURI;
    }

    // Validate the baseURI argument then update the runtime parameter
    public boolean setBaseURI(String baseURI) {
        Logger.getLogger(RuntimeOptions.class.getName()).log(Level.CONFIG,
                "Setting baseURI = " + baseURI);
        if( baseURI == null ) return false;
        this.baseURI = baseURI;
        return true;
    }

    public String getSourcesName() {
        return sourcesName;
    }

    // Validate the sourcesName argument then update the runtime parameter
    public boolean setSourcesName(String sourcesName) {
        if( sourcesName == null ) return false;
        this.sourcesName = sourcesName;
        return true;
    }

    /**
     * Returns the location of the directory for writing test logs to.
     * 
     * @return A File denoting a directory.
     */
    public File getLogDir() {
        return testLogDir;
    }

    // Validate the logDir argument then update the runtime parameter
    public boolean setLogDir(File logDir) {
        // Fortify Mod: validate that this is a legal path
        if( logDir == null ) return false;
        TEPath tpath = new TEPath( logDir.getAbsolutePath() );
        if( tpath.isValid() ) {
            this.testLogDir = logDir;
            return true;
            }
        return false;
    }

    public File getWorkDir() {
        return workDir;
    }

    // Validate the workDir argument then update the runtime parameter
    public boolean setWorkDir(File workDir) {
        // Fortify Mod: validate that this is a legal path
        if( workDir == null ) return false;
        TEPath tpath = new TEPath( workDir.getAbsolutePath() );
        if( tpath.isValid() ) {
            this.workDir = workDir;
            return true;
            }
        return false;
    }

    public int getMode() {
        return mode;
    }

    // Validate the mode argument then update the runtime parameter
    public boolean setMode(int mode) {
        if(mode != Test.TEST_MODE &&
           mode != Test.RETEST_MODE &&
           mode != Test.RESUME_MODE &&
           mode != Test.REDO_FROM_CACHE_MODE &&
           mode != Test.DOC_MODE &&
           mode != Test.CHECK_MODE &&
           mode != Test.PRETTYLOG_MODE) return false;
        this.mode = mode;
        return true;
    }

    public String getSessionId() {
        return sessionId;
    }

    // Validate the sessionId argument then update the runtime parameter
    public boolean setSessionId(String sessionId) {
        jLogger.log(Level.INFO, "RuntimeOptions: Setting session to " + sessionId);
        if( sessionId == null ) return false;
        if( Pattern.matches( UUID_PATTERN, sessionId )) {
            this.sessionId = sessionId;
            return true;
            }
        return false;
    }

    public String getSuiteName() {
        return suiteName;
    }

    // Validate the suiteName argument then update the runtime parameter
    public boolean setSuiteName(String suiteName) {
        if( suiteName == null ) return false;
        this.suiteName = suiteName;
        return true;
    }

    public ArrayList<String> getProfiles() {
        return profiles;
    }

    // Validate the profile argument then update the runtime parameter
    public boolean addProfile(String profile) {
        if( profile == null ) return false;
        this.profiles.add(profile);
        return true;
    }

    public ArrayList<String> getTestPaths() {
        return testPaths;
    }

    // Validate the testPath argument then update the runtime parameter
    public boolean addTestPath(String testPath) {
        if( testPath == null ) return false;
        this.testPaths.add(testPath);
        return true;
    }

    public ArrayList<String> getParams() {
        return params;
    }

    // Validate the param argument then update the runtime parameter
    public boolean addParam(String param) {
        if( param == null ) return false;
        this.params.add(param);
        return true;
    }

    public XdmNode getContextNode() {
        return null;
    }

    public String getTestName() {
        return testName;
    }

    // Validate the testName argument then update the runtime parameter
    public boolean setTestName(String testName) {
        if( testName == null ) return false;
        this.testName = testName;
        return true;
    }
    
    // Validate the recordedForm argument then update the runtime parameter
    public boolean addRecordedForm(String recordedForm) {
        if( recordedForm == null ) return false;
        recordedForms.add(new File(recordedForm));
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RuntimeOptions {\n");
        sb.append("mode=").append(mode).append(",\n");
        sb.append("testLogDir=").append(testLogDir).append(",\n");
        sb.append("workDir=").append(workDir).append(",\n");
        sb.append("sessionId=").append(sessionId).append(",\n");
        sb.append("testName=").append(testName).append(",\n");
        sb.append("suiteName=").append(suiteName).append(",\n");
        sb.append("sourcesName=").append(sourcesName).append(",\n");
        sb.append("baseURI=").append(baseURI).append(",\n");
        sb.append("profiles=").append(profiles).append(",\n");
        sb.append("testPaths=").append(testPaths).append(",\n");
        sb.append("recordedFroms=").append(recordedForms).append(",\n");
        sb.append("params=").append(params).append("\n}");
        return sb.toString();
    }

    /**
     * @return the recordedForms
     */
    public List<File> getRecordedForms() {
      return recordedForms;
    }

}
