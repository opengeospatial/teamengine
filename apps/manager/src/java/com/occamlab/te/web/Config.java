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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.index.ProfileEntry;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.util.DomUtils;

/**
 * Reads the test harness configuration file.
 */
public class Config {
    private String home;
    private File scriptsDir;
    private File resourcesDir;
    private File usersDir;
    private File workDir;
    private List<String> organizationList;             // A list of organizations
    private Map<String, List<String>> standardMap;     // Key is org, value is a list of standards
    private Map<String, List<String>> versionMap;      // Key is org_std, value is a list of versions
    private Map<String, List<String>> revisionMap;     // Key is org_std_ver, value is a list of revisions
    private Map<String, SuiteEntry> suites;            // Key is org_std_ver_rev, value is a SuiteEntry 
    private Map<String, List<ProfileEntry>> profiles;  // Key is org_std_ver_rev, value is a list of profiles 
    private Map<String, List<File>> sources;           // Key is org_std_ver_rev, value is a list of sources
    private Map<String, String> webdirs;               // Key is org_std_ver_rev, value a webdir
    private Map<String, File> resources;               // Key is org_std_ver_rev, value a resource directory

    public Config() {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Document doc = db.parse(cl.getResourceAsStream("config.xml"));
            Element configElem = (Element) (doc.getElementsByTagName("config").item(0));
            Element homeElem = (Element) (configElem.getElementsByTagName("home").item(0));
            home = homeElem.getTextContent();

            Element scriptsDirEl = DomUtils.getElementByTagName(configElem, "scriptsdir");
            scriptsDir = findFile(scriptsDirEl.getTextContent(), cl);
            if (!scriptsDir.isDirectory()) {
                System.out.println("Error: Directory " + scriptsDirEl.getTextContent() + " does not exist.");
            }

            Element resourcesDirEl = DomUtils.getElementByTagName(configElem, "resourcesdir");
            resourcesDir = findFile(resourcesDirEl.getTextContent(), cl);
            if (!resourcesDir.isDirectory()) {
                System.out.println("Error: Directory " + resourcesDirEl.getTextContent() + " does not exist.");
            }

            Element usersDirEl = DomUtils.getElementByTagName(configElem, "usersdir");
            usersDir = findFile(usersDirEl.getTextContent(), cl);
            if (!usersDir.isDirectory()) {
                System.out.println("Error: Directory " + usersDirEl.getTextContent() + " does not exist.");
            }

            Element workDirEl = DomUtils.getElementByTagName(configElem, "workdir");
            workDir = findFile(workDirEl.getTextContent(), cl);
            if (!workDir.isDirectory()) {
                System.out.println("Error: Directory " + workDirEl.getTextContent() + " does not exist.");
            }
            
            organizationList = new ArrayList<String>();
            standardMap = new HashMap<String, List<String>>();
            versionMap = new HashMap<String, List<String>>();
            revisionMap = new HashMap<String, List<String>>();
            suites = new HashMap<String, SuiteEntry>(); 
            profiles = new HashMap<String, List<ProfileEntry>>(); 
            sources = new HashMap<String, List<File>>(); 
            webdirs = new HashMap<String, String>(); 
            resources = new HashMap<String, File>(); 

            for (Element organizationEl : DomUtils.getElementsByTagName(configElem, "organization")) {
                String organization = DomUtils.getElementByTagName(organizationEl, "name").getTextContent();
                organizationList.add(organization);

                ArrayList<String> standardList = new ArrayList<String>();
                for (Element standardEl : DomUtils.getElementsByTagName(organizationEl, "standard")) {
                    String standard = DomUtils.getElementByTagName(standardEl, "name").getTextContent();
                    standardList.add(standard);
                    standardMap.put(organization, standardList);

                    ArrayList<String> versionList = new ArrayList<String>();
                    for (Element versionEl : DomUtils.getElementsByTagName(standardEl, "version")) {
                        String version = DomUtils.getElementByTagName(versionEl, "name").getTextContent();
                        versionList.add(version);
                        String verKey = organization + "_" + standard;
                        versionMap.put(verKey, versionList);

                        SuiteEntry suite = new SuiteEntry();
                        Element suiteEl = DomUtils.getElementByTagName(versionEl, "suite");
                        String namespaceUri = DomUtils.getElementByTagName(suiteEl, "namespace-uri").getTextContent();
                        String prefix = DomUtils.getElementByTagName(suiteEl, "prefix").getTextContent();
                        String localName = DomUtils.getElementByTagName(suiteEl, "local-name").getTextContent();
                        suite.setQName(new QName(namespaceUri, localName, prefix));
                        suite.setTitle(DomUtils.getElementByTagName(suiteEl, "title").getTextContent());
                        Element descEl = DomUtils.getElementByTagName(suiteEl, "description");
                        if (descEl != null) {
                            suite.setDescription(descEl.getTextContent());
                        }
                        for (Element linkEl : DomUtils.getElementsByTagName(suiteEl, "link")) {
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
                                String revision = DomUtils.getElementByTagName(el, "name").getTextContent();
                                revisionList.add(revision);
                                String revKey = verKey + "_" + version;
                                revisionMap.put(revKey, revisionList);
                            
                                String key = revKey + "_" + revision;
                                suites.put(key, suite);

                                ArrayList<File> list = new ArrayList<File>();
                                for (Element sourceEl : DomUtils.getElementsByTagName(el, "source")) {
                                    list.add(new File(scriptsDir, sourceEl.getTextContent()));
                                }
                                sources.put(key, list);
                                
                                for (Element resourcesEl : DomUtils.getElementsByTagName(el, "resources")) {
                                    resources.put(key, new File(resourcesDir, resourcesEl.getTextContent()));
                                }
                                
                                for (Element webdirEl : DomUtils.getElementsByTagName(el, "webdir")) {
                                    webdirs.put(key, webdirEl.getTextContent());
                                }
                                
                                ArrayList<ProfileEntry> profileList = new ArrayList<ProfileEntry>();
                                for (Element profileEl : DomUtils.getElementsByTagName(el, "profile")) {
                                    ProfileEntry profile = new ProfileEntry();
                                    namespaceUri = DomUtils.getElementByTagName(profileEl, "namespace-uri").getTextContent();
                                    prefix = DomUtils.getElementByTagName(profileEl, "prefix").getTextContent();
                                    localName = DomUtils.getElementByTagName(profileEl, "local-name").getTextContent();
                                    profile.setQName(new QName(namespaceUri, localName, prefix));
                                    profile.setBaseSuite(suite.getQName());
                                    profile.setTitle(DomUtils.getElementByTagName(profileEl, "title").getTextContent());
                                    profileList.add(profile);
                                }
                                profiles.put(key, profileList);
                            }
                        }
                    }
                }
            }

//            File script_dir = new File(URLDecoder.decode(cl.getResource(
//                    "com/occamlab/te/scripts/parsers.ctl").getFile(), "UTF-8")).getParentFile();
//
//            // automatically load extension modules
//            URL modulesURL = cl.getResource("modules/");
//            File modulesDir = null;
//            if (modulesURL != null) {
//                modulesDir = new File(URLDecoder.decode(modulesURL.getFile(), "UTF-8"));
//            }
//
//            availableSuites = new LinkedHashMap<String, List<File>>();
//            NodeList sourcesList = configElem.getElementsByTagName("sources");
//            for (int i = 0; i < sourcesList.getLength(); i++) {
//                ArrayList<File> ctlLocations = new ArrayList<File>();
//                ctlLocations.add(script_dir);
//                if (modulesDir != null) {
//                    ctlLocations.add(modulesDir);
//                }
//                Element sources = (Element) sourcesList.item(i);
//                String id = sources.getAttribute("id");
//                NodeList sourceList = sources.getElementsByTagName("source");
//                for (int j = 0; j < sourceList.getLength(); j++) {
//                    Element source = (Element) sourceList.item(j);
//                    File f = findFile(source.getTextContent(), cl);
//                    if (!f.exists()) {
//                        throw new FileNotFoundException("Source location "
//                                + source.getTextContent() + " does not exist.");
//                    }
//                    ctlLocations.add(f);
//                }
//                availableSuites.put(id, ctlLocations);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getHome() {
        return home;
    }

    public File getScriptsDir() {
        return scriptsDir;
    }

    public File getUsersDir() {
        return usersDir;
    }

    public File getWorkDir() {
        return workDir;
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

//    public static LinkedHashMap<String, List<File>> getAvailableSuites() {
//        return availableSuites;
//    }
//
    /**
     * Finds a source file or directory. The location may be specified using:
     * <ul>
     * <li>an absolute system path;</li>
     * <li>a path relative to the location identified by the
     * <code>catalina.base</code> system property;</li>
     * <li>a classpath location.</li>
     * </ul>
     */
    private static File findFile(String path, ClassLoader loader) {
        File f = new File(path);
        if (!f.exists()) {
            f = new File(System.getProperty("catalina.base"), path);
        }
        if (!f.exists()) {
            URL url = loader.getResource(path);
            if (null != url) {
                f = new File(url.getFile());
            } else {
                System.out.println("Directory is not accessible: " + path);
            }
        }
        return f;
    }
}
