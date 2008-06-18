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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.XdmNode;

import org.w3c.dom.Element;

public class RuntimeOptions {
    int mode = Test.TEST_MODE;
    File logDir = null;
    File workDir = null;
    String sessionId = null;
    String testName = null;
    String suiteName = null;
    ArrayList<String> testPaths = new ArrayList<String>();
    ArrayList<String> params = new ArrayList<String>();

    public File getLogDir() {
        return logDir;
    }

    public void setLogDir(File logDir) {
        this.logDir = logDir;
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
    
    public XdmNode getParamsNode() throws Exception {
        String paramsXML = "<params>";
        for (int i = 0; i < params.size(); i++){
            String param = params.get(i);
            String name = param.substring(0, param.indexOf('='));
            String value = param.substring(param.indexOf('=') + 1);
            if (params.get(i).indexOf('=') != 0) {
                paramsXML += "<param local-name=\"" + name + "\" namespace-uri=\"\" prefix=\"\" type=\"xs:string\">";
                paramsXML += "<value>" + value + "</value>";
                paramsXML += "</param>";
            }
        }
        paramsXML += "</params>";
//System.out.println("paramsXML: "+paramsXML);

        return Globals.builder.build(new StreamSource(new StringReader(paramsXML)));
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }
}
