
/****************************************************************************
 The Original Code is TEAM Engine.
 
 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.
 
 Contributor(s): 
 	C. Heazel (WiSC): Added Fortify adjudication changes
        C. Heazel (WiSC): Moved vocabulary package from spi to core
 
 ****************************************************************************/
package com.occamlab.te.vocabulary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the test harness configuration file at TE_BASE/config.xml.
 */
public class Config {
	
	private static final Logger LOGR = Logger.getLogger(Config.class.getName());

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
    private Map<String, List<String>> conformanceClassMap; // Key is testname, value is a
														   // list of conformance classes
    /**
     * Collection of directories containing ETS resources. The map key
     * identifies a given ETS revision ("$org_$std_$ver_$rev").
     */
    private Map<String, File> resources;

    public Config() {
    	String basePath = System.getProperty("TE_BASE");
        if (null == basePath) {
          basePath = System.getenv("TE_BASE");
        }
        if (null == basePath) {
          basePath = System.getProperty("user.home")
                  + System.getProperty("file.separator") + "teamengine";
        }    	
        this.baseDir = new File(basePath);        
        File configFileExternal = new File(this.baseDir, "config.xml");        
        NodeList organizationNodes = null;        
		try {
			organizationNodes = getOrganizations(configFileExternal);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LOGR.log(Level.SEVERE, String.format("Could not parse config file from path: %s.", configFileExternal.getAbsolutePath()), e);
			return;
		}		
        try {
            // use TE_BASE/scripts so no need to move ETS resources TODO: is this used?
        	try {
                this.resourcesDir = getScriptsDir();
			} catch (Exception e) {
				LOGR.log(Level.WARNING, "Could not get directory for scripts. TE_BASE not set.");
			}
            organizationList = new ArrayList<String>();
            standardMap = new HashMap<String, List<String>>();
            versionMap = new HashMap<String, List<String>>();
            revisionMap = new HashMap<String, List<String>>();
            conformanceClassMap = new HashMap<String, List<String>>();
            String suiteLocalName = null;			
            for (int i = 0; i < organizationNodes.getLength(); i++) {
				Node organizationEl = organizationNodes.item(i);
                String organization = getElementByTagName(
                        organizationEl, "name").getTextContent();

                for (Element standardEl : getElementsByTagName(
                        organizationEl, "standard")) {
                    String standard = getElementByTagName(standardEl,
                            "name").getTextContent();

                    ArrayList<String> versionList = new ArrayList<String>();
                    for (Element versionEl : getElementsByTagName(
                            standardEl, "version")) {
                        String version = getElementByTagName(
                                versionEl, "name").getTextContent();
                        versionList.add(version);
                        String verKey = organization + "_" + standard;
                        Element suiteEl = getElementByTagName(
                                versionEl, "suite");
                        suiteLocalName = getElementByTagName(
                                suiteEl, "local-name").getTextContent();
                        
                        String key = null;
                        ArrayList<String> revisionList = new ArrayList<String>();
                        for (Element el : getChildElements(versionEl)) {
                            if (el.getNodeName().equals("revision")) {
                                String revision = getElementByTagName(
                                        el, "name").getTextContent();
                                revisionList.add(revision);
                                String revKey = verKey + "_" + version;
                                key = revKey + "_" + revision;
                            }
                        }
                        
                        ArrayList<String> ccList = new ArrayList<String>(); // Conformance class list.
                        Element conformanceClasses = getElementByTagName(suiteEl, "BasicConformanceClasses");
                        
                        if(null != conformanceClasses){
                            for (Element ccElement : getChildElements(conformanceClasses)) {
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
			LOGR.log(Level.SEVERE, "Could not extract infos from config file.", e);
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
    
    public List<Element> getElementsByTagName(Node node, String tagname) {
        ArrayList<Element> list = new ArrayList<Element>();
        NodeList nl;
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            nl = ((Document) node).getElementsByTagName(tagname);
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            nl = ((Element) node).getElementsByTagName(tagname);
        } else {
            return null;
        }
        for (int i = 0; i < nl.getLength(); i++) {
            list.add((Element) nl.item(i));
        }
        return list;
    }
    
    public Element getElementByTagName(Node node, String tagname) {
        NodeList nl;
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            nl = ((Document) node).getElementsByTagName(tagname);
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            nl = ((Element) node).getElementsByTagName(tagname);
        } else {
            return null;
        }
        if (nl.getLength() >= 0) {
            return (Element) nl.item(0);
        } else {
            return null;
        }
    }
    
    public List<Element> getChildElements(Node node) {
        ArrayList<Element> list = new ArrayList<Element>();
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                list.add((Element) nl.item(i));
            }
        }
        return list;
    }

	private NodeList getOrganizations(File configFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = null;
        NodeList organizations = null;
        if(configFile.exists()) {
        	doc = db.parse(configFile);
            Element configElem = (Element) (doc.getElementsByTagName("config")
                    .item(0));
            organizations = configElem.getElementsByTagName("organization");
        } else {
        	//get config from classpath

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream configFileStream = cl.getResourceAsStream("config.xml");
            try {
            	doc = db.parse(configFileStream);				
            	//config files from test suite class paths start directly with "organization" element
            	organizations = doc.getElementsByTagName("organization");
			} catch (IllegalArgumentException e) {
				LOGR.log(Level.SEVERE, "Could not parse config file from class path.", e);
			}
        }
		return organizations;
	}
}
