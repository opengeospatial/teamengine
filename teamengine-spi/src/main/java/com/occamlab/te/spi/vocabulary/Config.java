
/****************************************************************************
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
 
 Contributor(s): 
 	C. Heazel (WiSC): Added Fortify adjudication changes
 
 ****************************************************************************/
package com.occamlab.te.spi.vocabulary;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.SetupOptions;
import com.occamlab.te.index.ProfileEntry;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.util.DomUtils;

/**
 * Reads the test harness configuration file at TE_BASE/config.xml.
 */
public class Config {

    /** Location of base configuration directory (TE_BASE) */
    private File baseDir;
    private File scriptsDir;
    private File resourcesDir;
    private File usersDir;
    private List<String> organizationList; // A list of organizations
    private Map<String, List<String>> standardMap; // Key is org, value is a
                                                   // list of standards
    private Map<String, List<String>> versionMap; // Key is org_std, value is a
                                                  // list of versions
    private Map<String, List<String>> revisionMap; // Key is org_std_ver, value
                                                   // is a list of revisions
//    private Map<String, SuiteEntry> suites; // Key is org_std_ver_rev, value is
                                            // a SuiteEntry
//    private Map<String, List<ProfileEntry>> profiles; // Key is org_std_ver_rev,
                                                      // value is a list of
                                                      // profiles
//    private Map<String, List<File>> sources; // Key is org_std_ver_rev, value is
                                             // a list of sources
//    private Map<String, String> webdirs; // Key is org_std_ver_rev, value a
                                         // webdir
    private Map<String, List<String>> conformanceClassMap; // Key is testname, value is a
														   // list of conformance classes
    /**
     * Collection of directories containing ETS resources. The map key
     * identifies a given ETS revision ("$org_$std_$ver_$rev").
     */
    private Map<String, File> resources;

    public Config() {
        this.baseDir = SetupOptions.getBaseConfigDirectory();
        try {
                // Fortify Mod: prevent external entity injection
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            // DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(new File(this.baseDir, "config.xml"));
            Element configElem = (Element) (doc.getElementsByTagName("config")
                    .item(0));
            // use TE_BASE/scripts so no need to move ETS resources
            this.resourcesDir = getScriptsDir();
            organizationList = new ArrayList<String>();
            standardMap = new HashMap<String, List<String>>();
            versionMap = new HashMap<String, List<String>>();
            revisionMap = new HashMap<String, List<String>>();
//            suites = new HashMap<String, SuiteEntry>();
//            profiles = new HashMap<String, List<ProfileEntry>>();
//            sources = new HashMap<String, List<File>>();
//            webdirs = new HashMap<String, String>();
//            resources = new HashMap<String, File>();
            conformanceClassMap = new HashMap<String, List<String>>();
            String suiteLocalName = null;
            for (Element organizationEl : DomUtils.getElementsByTagName(
                    configElem, "organization")) {
                String organization = DomUtils.getElementByTagName(
                        organizationEl, "name").getTextContent();

                for (Element standardEl : DomUtils.getElementsByTagName(
                        organizationEl, "standard")) {
                    String standard = DomUtils.getElementByTagName(standardEl,
                            "name").getTextContent();

                    ArrayList<String> versionList = new ArrayList<String>();
                    for (Element versionEl : DomUtils.getElementsByTagName(
                            standardEl, "version")) {
                        String version = DomUtils.getElementByTagName(
                                versionEl, "name").getTextContent();
                        versionList.add(version);
                        String verKey = organization + "_" + standard;
                        Element suiteEl = DomUtils.getElementByTagName(
                                versionEl, "suite");
                        suiteLocalName = DomUtils.getElementByTagName(
                                suiteEl, "local-name").getTextContent();
                        
                        String key = null;
                        ArrayList<String> revisionList = new ArrayList<String>();
                        for (Element el : DomUtils.getChildElements(versionEl)) {
                            if (el.getNodeName().equals("revision")) {
                                String revision = DomUtils.getElementByTagName(
                                        el, "name").getTextContent();
                                revisionList.add(revision);
                                String revKey = verKey + "_" + version;
                                key = revKey + "_" + revision;
                            }
                        }
                        
                        ArrayList<String> ccList = new ArrayList<String>(); // Conformance class list.
                        Element conformanceClasses = DomUtils.getElementByTagName(suiteEl, "conformanceClasses");
                        
                        if(null != conformanceClasses){
                            for (Element ccElement : DomUtils.getChildElements(conformanceClasses)) {
                                if (ccElement.getNodeName().equals("conformanceClass")) {
                                    String confClass = ccElement.getTextContent();
                                    ccList.add(confClass);
                                }
                            }
                                conformanceClassMap.put(suiteLocalName, ccList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getScriptsDir() {
        if (null == this.scriptsDir) {
            File dir = new File(this.baseDir, "scripts");
            if (!dir.exists() && !dir.mkdir()) {
                throw new RuntimeException("Failed to create directory at "
                        + dir.getAbsolutePath());
            }
            this.scriptsDir = dir;
        }
        return scriptsDir;
    }

    public File getUsersDir() {
        if (null == this.usersDir) {
            File dir = new File(this.baseDir, "users");
            if (!dir.exists() && !dir.mkdir()) {
                throw new RuntimeException("Failed to create directory at "
                        + dir.getAbsolutePath());
            }
            this.usersDir = dir;
        }
        return usersDir;
    }

    public List<String> getOrganizationList() {
        return organizationList;
    }

    public Map<String, List<String>> getRevisionMap() {
        return revisionMap;
    }

    public Map<String, List<String>> getStandardMap() {
        return standardMap;
    }

    public Map<String, List<String>> getVersionMap() {
        return versionMap;
    }

    public Map<String, File> getResources() {
        return resources;
    }
    
    public Map<String, List<String>> getConformanceClassMap() {
        return conformanceClassMap;
    }
}