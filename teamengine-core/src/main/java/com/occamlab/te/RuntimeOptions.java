/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

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
import com.occamlab.te.util.TEPath;  // Fortify addition

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
 * 3) Users must know the state of the runtime settings.  Therefors, all
 *    operations shall return a value. 
 * <p>
 * The configuration settings are:
 *   testLogDir: This is the directory where the log file will be written.  
 *     -- Constraint: Must be a valid TE directory path.
 *     -- Constraint: Null is not allowed.
 *   workDir:
 *     -- Constraint: Must be a valid TE directory path.
 *     -- Constraint: Null is not allowed.
 *   sessionId: 
 *     -- Constraint: Required format "s"<0..9>
 *     -- Constraint: Null is not allowed.
 *   testName: 
 *     -- Constraint:
 *   suiteName:
 *     -- Constraint:
 *   sourcesName: 
 *     -- Constraint:
 *   baseURI: 
 *     -- Constraint:
 *   profiles: 
 *     -- Constraint:
 *   testPaths:
 *     -- Constraint:
 *   params:
 *     -- Constraint:
 *   recordedForms: 
 *     -- Constraint:
 */
public class RuntimeOptions {
    int mode = Test.TEST_MODE;
    File testLogDir = null;
    File workDir = null;
    String sessionId = null;
    String testName = null;
    String suiteName = null;
    String sourcesName = "default";
    String baseURI = "";
    ArrayList<String> profiles = new ArrayList<String>();
    ArrayList<String> testPaths = new ArrayList<String>();
    ArrayList<String> params = new ArrayList<String>();
    List<File> recordedForms = new ArrayList<>();
    private static Logger jLogger = Logger.getLogger("com.occamlab.te.RuntimeOptions");

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
        jLogger.setLevel(Level.INFO);
    }

    public String getBaseURI() {
        return baseURI;
    }

    // Validate the baseURI argument then update the runtime parameter
    public boolean setBaseURI(String baseURI) {
        Logger.getLogger(RuntimeOptions.class.getName()).log(Level.CONFIG,
                "Setting baseURI = " + baseURI);
        this.baseURI = baseURI;
        return true;
    }

    public String getSourcesName() {
        return sourcesName;
    }

    // Validate the sourcesName argument then update the runtime parameter
    public boolean setSourcesName(String sourcesName) {
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
        // null is considered a legal path in this case
        jLogger.log(Level.INFO, "RuntimeOptions: Setting Work Dir to " + workDir.getAbsolutePath() );
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
        this.mode = mode;
        return true;
    }

    public String getSessionId() {
        jLogger.log(Level.INFO, "RuntimeOptions: Getting sessionId " + sessionId);
        if(sessionId == null) sessionId = new String("null_session");
        return sessionId;
    }

    // Validate the sessionId argument then update the runtime parameter
    public boolean setSessionId(String sessionId) {
        jLogger.log(Level.INFO, "RuntimeOptions: Setting session to " + sessionId);
        if( sessionId == null ) return false;
        this.sessionId = sessionId;
        return true;
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
