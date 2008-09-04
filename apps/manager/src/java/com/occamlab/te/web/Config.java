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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.lang.ClassLoader;
import java.net.URLDecoder;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads the test harness configuration file. The file is structured as follows:
 * 
 * <pre>
 *    &lt;config&gt;
 *      &lt;home&gt;${base-url}&lt;/home&gt;
 *      &lt;usersdir&gt;${users.dir}&lt;/usersdir&gt;
 *      &lt;!-- one or more test suites --&gt;
 *      &lt;sources id=&quot;${test-suite-id}&quot;&gt;
 *        &lt;source&gt;${ctl.source.location}&lt;/source&gt;
 *        &lt;!-- additional CTL source locations --&gt;
 *      &lt;/sources&gt;
 *    &lt;/config&gt;
 * </pre>
 */
public class Config {
    private String home;
    private File scriptsDir;
    private File usersDir;
    private File workDir;

//    private static LinkedHashMap<String, List<File>> availableSuites;

    public Config() {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Document doc = db.parse(cl.getResourceAsStream("config.xml"));
            Element configElem = (Element) (doc.getElementsByTagName("config").item(0));
            Element homeElem = (Element) (configElem.getElementsByTagName("home").item(0));
            home = homeElem.getTextContent();

            Element scriptsDirEl = (Element) (configElem.getElementsByTagName("scriptsdir").item(0));
            scriptsDir = findFile(scriptsDirEl.getTextContent(), cl);
            if (!scriptsDir.isDirectory()) {
                System.out.println("Error: Directory " + scriptsDirEl.getTextContent() + " does not exist.");
            }

            Element usersDirEl = (Element) (configElem.getElementsByTagName("usersdir").item(0));
            usersDir = findFile(usersDirEl.getTextContent(), cl);
            if (!usersDir.isDirectory()) {
                System.out.println("Error: Directory " + usersDirEl.getTextContent() + " does not exist.");
            }

            Element workDirEl = (Element) (configElem.getElementsByTagName("workdir").item(0));
            workDir = findFile(workDirEl.getTextContent(), cl);
            if (!workDir.isDirectory()) {
                System.out.println("Error: Directory " + workDirEl.getTextContent() + " does not exist.");
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
//                        // TODO: Log this
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
