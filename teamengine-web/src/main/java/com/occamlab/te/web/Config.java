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

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.web;

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
    private Map<String, SuiteEntry> suites; // Key is org_std_ver_rev, value is
                                            // a SuiteEntry
    private Map<String, List<ProfileEntry>> profiles; // Key is org_std_ver_rev,
                                                      // value is a list of
                                                      // profiles
    private Map<String, List<File>> sources; // Key is org_std_ver_rev, value is
                                             // a list of sources
    private Map<String, String> webdirs; // Key is org_std_ver_rev, value a
                                         // webdir
    /**
     * Collection of directories containing ETS resources. The map key
     * identifies a given ETS revision ("$org_$std_$ver_$rev").
     */
    private Map<String, File> resources;

    public Config() {
        this.baseDir = SetupOptions.getBaseConfigDirectory();
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = db.parse(new File(this.baseDir, "config.xml"));
            Element configElem = (Element) (doc.getElementsByTagName("config")
                    .item(0));
            // use TE_BASE/scripts so no need to move ETS resources
            this.resourcesDir = getScriptsDir();
            organizationList = new ArrayList<String>();
            standardMap = new HashMap<String, List<String>>();
            versionMap = new HashMap<String, List<String>>();
            revisionMap = new HashMap<String, List<String>>();
            suites = new HashMap<String, SuiteEntry>();
            profiles = new HashMap<String, List<ProfileEntry>>();
            sources = new HashMap<String, List<File>>();
            webdirs = new HashMap<String, String>();
            resources = new HashMap<String, File>();

            for (Element organizationEl : DomUtils.getElementsByTagName(
                    configElem, "organization")) {
                String organization = DomUtils.getElementByTagName(
                        organizationEl, "name").getTextContent();
                organizationList.add(organization);

                ArrayList<String> standardList = new ArrayList<String>();
                for (Element standardEl : DomUtils.getElementsByTagName(
                        organizationEl, "standard")) {
                    String standard = DomUtils.getElementByTagName(standardEl,
                            "name").getTextContent();
                    standardList.add(standard);
                    standardMap.put(organization, standardList);

                    ArrayList<String> versionList = new ArrayList<String>();
                    for (Element versionEl : DomUtils.getElementsByTagName(
                            standardEl, "version")) {
                        String version = DomUtils.getElementByTagName(
                                versionEl, "name").getTextContent();
                        versionList.add(version);
                        String verKey = organization + "_" + standard;
                        versionMap.put(verKey, versionList);

                        SuiteEntry suite = new SuiteEntry();
                        Element suiteEl = DomUtils.getElementByTagName(
                                versionEl, "suite");
                        String namespaceUri = DomUtils.getElementByTagName(
                                suiteEl, "namespace-uri").getTextContent();
                        String prefix = DomUtils.getElementByTagName(suiteEl,
                                "prefix").getTextContent();
                        String localName = DomUtils.getElementByTagName(
                                suiteEl, "local-name").getTextContent();
                        suite.setQName(new QName(namespaceUri, localName,
                                prefix));
                        suite.setTitle(DomUtils.getElementByTagName(suiteEl,
                                "title").getTextContent());
                        Element descEl = DomUtils.getElementByTagName(suiteEl,
                                "description");
                        if (descEl != null) {
                            suite.setDescription(descEl.getTextContent());
                        }
                        for (Element linkEl : DomUtils.getElementsByTagName(
                                suiteEl, "link")) {
                            String value = linkEl.getTextContent();
                            if ("data".equals(linkEl.getAttribute("linkType"))) {
                                suite.setDataLink(value);
                            } else if (value.startsWith("data/")) {
                                suite.setDataLink(value);
                            } else {
                                suite.setLink(value);
                            }
                        }

                        ArrayList<String> revisionList = new ArrayList<String>();
                        for (Element el : DomUtils.getChildElements(versionEl)) {
                            if (el.getNodeName().equals("revision")) {
                                String revision = DomUtils.getElementByTagName(
                                        el, "name").getTextContent();
                                revisionList.add(revision);
                                String revKey = verKey + "_" + version;
                                revisionMap.put(revKey, revisionList);

                                String key = revKey + "_" + revision;
                                suites.put(key, suite);

                                ArrayList<File> list = new ArrayList<File>();
                                for (Element sourceEl : DomUtils
                                        .getElementsByTagName(el, "source")) {
                                    if (getScriptsDir() == null) {
                                        list.add(new File(sourceEl
                                                .getTextContent()));
                                    } else {
                                        list.add(new File(getScriptsDir(),
                                                sourceEl.getTextContent()));
                                    }
                                }
                                sources.put(key, list);

                                for (Element resourcesEl : DomUtils
                                        .getElementsByTagName(el, "resources")) {
                                    if (resourcesDir == null) {
                                        // path relative to TE_BASE
                                        resources
                                                .put(key,
                                                        new File(
                                                                this.baseDir,
                                                                resourcesEl
                                                                        .getTextContent()));
                                    } else {
                                        resources
                                                .put(key,
                                                        new File(
                                                                this.resourcesDir,
                                                                resourcesEl
                                                                        .getTextContent()));
                                    }
                                }

                                for (Element webdirEl : DomUtils
                                        .getElementsByTagName(el, "webdir")) {
                                    webdirs.put(key, webdirEl.getTextContent());
                                }

                                ArrayList<ProfileEntry> profileList = new ArrayList<ProfileEntry>();
                                for (Element profileEl : DomUtils
                                        .getElementsByTagName(el, "profile")) {
                                    ProfileEntry profile = new ProfileEntry();
                                    namespaceUri = DomUtils
                                            .getElementByTagName(profileEl,
                                                    "namespace-uri")
                                            .getTextContent();
                                    prefix = DomUtils.getElementByTagName(
                                            profileEl, "prefix")
                                            .getTextContent();
                                    localName = DomUtils.getElementByTagName(
                                            profileEl, "local-name")
                                            .getTextContent();
                                    profile.setQName(new QName(namespaceUri,
                                            localName, prefix));
                                    profile.setBaseSuite(suite.getQName());
                                    profile.setTitle(DomUtils
                                            .getElementByTagName(profileEl,
                                                    "title").getTextContent());
                                    profileList.add(profile);
                                }
                                profiles.put(key, profileList);
                            }
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

    public Map<String, List<File>> getSources() {
        return sources;
    }

    public Map<String, List<String>> getStandardMap() {
        return standardMap;
    }

    public Map<String, SuiteEntry> getSuites() {
        return suites;
    }

    public Map<String, List<ProfileEntry>> getProfiles() {
        return profiles;
    }

    public Map<String, List<String>> getVersionMap() {
        return versionMap;
    }

    public Map<String, String> getWebDirs() {
        return webdirs;
    }

    public Map<String, File> getResources() {
        return resources;
    }
}
