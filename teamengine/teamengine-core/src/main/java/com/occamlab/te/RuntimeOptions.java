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

 Contributor(s): No additional contributors to date
 */

package com.occamlab.te;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.saxon.s9api.XdmNode;

/**
 * Provides runtime configuration settings.
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
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        Logger.getLogger(RuntimeOptions.class.getName()).log(Level.CONFIG,
                "Setting baseURI = " + baseURI);
        this.baseURI = baseURI;
    }

    public String getSourcesName() {
        return sourcesName;
    }

    public void setSourcesName(String sourcesName) {
        this.sourcesName = sourcesName;
    }

    /**
     * Returns the location of the directory for writing test logs to.
     * 
     * @return A File denoting a directory.
     */
    public File getLogDir() {
        return testLogDir;
    }

    public void setLogDir(File logDir) {
        this.testLogDir = logDir;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public ArrayList<String> getProfiles() {
        return profiles;
    }

    public void addProfile(String profile) {
        this.profiles.add(profile);
    }

    public ArrayList<String> getTestPaths() {
        return testPaths;
    }

    public void addTestPath(String testPath) {
        this.testPaths.add(testPath);
    }

    public ArrayList<String> getParams() {
        return params;
    }

    public void addParam(String param) {
        this.params.add(param);
    }

    public XdmNode getContextNode() {
        return null;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
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
        sb.append("params=").append(params).append("\n}");
        return sb.toString();
    }

}
