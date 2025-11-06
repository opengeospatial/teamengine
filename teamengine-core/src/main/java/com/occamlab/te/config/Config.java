/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
/****************************************************************************
 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s):
     C. Heazel (WiSC): Added Fortify adjudication changes

 ****************************************************************************/
package com.occamlab.te.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.occamlab.te.SetupOptions;
import com.occamlab.te.index.ProfileEntry;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.util.DomUtils;

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
	 * Collection of directories containing ETS resources. The map key identifies a given
	 * ETS revision ("$org_$std_$ver_$rev").
	 */
	private Map<String, File> resources;

	public Config() {
		this.baseDir = SetupOptions.getBaseConfigDirectory();
		File configFileExternal = new File(this.baseDir, "config.xml");
		NodeList organizationNodes = null;
		try {
			organizationNodes = getOrganizations(configFileExternal);
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			LOGR.log(Level.SEVERE,
					String.format("Could not parse config file from path: %s.", configFileExternal.getAbsolutePath()),
					e);
			return;
		}
		try {
			// Fortify Mod: prevent external entity injection
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setExpandEntityReferences(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			// use TE_BASE/scripts so no need to move ETS resources
			try {
				this.resourcesDir = getScriptsDir();
			}
			catch (Exception e) {
				LOGR.log(Level.WARNING, "Could not get directory for scripts. TE_BASE not set.");
			}
			organizationList = new ArrayList<>();
			standardMap = new HashMap<>();
			versionMap = new HashMap<>();
			revisionMap = new HashMap<>();
			conformanceClassMap = new HashMap<>();
			suites = new HashMap<>();
			profiles = new HashMap<>();
			sources = new HashMap<>();
			webdirs = new HashMap<>();
			resources = new HashMap<>();

			for (int i = 0; i < organizationNodes.getLength(); i++) {
				Node organizationEl = organizationNodes.item(i);
				String organization = DomUtils.getElementByTagName(organizationEl, "name").getTextContent();
				organizationList.add(organization);

				ArrayList<String> standardList = new ArrayList<>();
				for (Element standardEl : DomUtils.getElementsByTagName(organizationEl, "standard")) {
					String standard = DomUtils.getElementByTagName(standardEl, "name").getTextContent();
					standardList.add(standard);
					standardMap.put(organization, standardList);

					ArrayList<String> versionList = new ArrayList<>();
					for (Element versionEl : DomUtils.getElementsByTagName(standardEl, "version")) {
						String version = DomUtils.getElementByTagName(versionEl, "name").getTextContent();
						versionList.add(version);
						String verKey = organization + "_" + standard;
						versionMap.put(verKey, versionList);

						SuiteEntry suite = new SuiteEntry();
						Element suiteEl = DomUtils.getElementByTagName(versionEl, "suite");
						String suiteNamespaceUri = DomUtils.getElementByTagName(suiteEl, "namespace-uri")
							.getTextContent();
						String suitePrefix = DomUtils.getElementByTagName(suiteEl, "prefix").getTextContent();
						String suiteLocalName = DomUtils.getElementByTagName(suiteEl, "local-name").getTextContent();
						suite.setQName(new QName(suiteNamespaceUri, suiteLocalName, suitePrefix));
						suite.setTitle(DomUtils.getElementByTagName(suiteEl, "title").getTextContent());
						Element descEl = DomUtils.getElementByTagName(suiteEl, "description");
						if (descEl != null) {
							suite.setDescription(descEl.getTextContent());
						}
						for (Element linkEl : DomUtils.getElementsByTagName(suiteEl, "link")) {
							String value = linkEl.getTextContent();
							if ("data".equals(linkEl.getAttribute("linkType"))) {
								suite.setDataLink(value);
							}
							else if (value.startsWith("data/")) {
								suite.setDataLink(value);
							}
							else {
								suite.setLink(value);
							}
						}

						ArrayList<String> revisionList = new ArrayList<>();
						for (Element el : DomUtils.getChildElements(versionEl)) {
							if (el.getNodeName().equals("revision")) {
								String revision = DomUtils.getElementByTagName(el, "name").getTextContent();
								revisionList.add(revision);
								String revKey = verKey + "_" + version;
								revisionMap.put(revKey, revisionList);

								String key = revKey + "_" + revision;
								suites.put(key, suite);

								ArrayList<File> list = new ArrayList<>();
								for (Element sourceEl : DomUtils.getElementsByTagName(el, "source")) {
									if (getScriptsDir() == null) {
										list.add(new File(sourceEl.getTextContent()));
									}
									else {
										list.add(new File(getScriptsDir(), sourceEl.getTextContent()));
									}
								}
								sources.put(key, list);

								for (Element resourcesEl : DomUtils.getElementsByTagName(el, "resources")) {
									if (resourcesDir == null) {
										// path relative to TE_BASE
										resources.put(key, new File(this.baseDir, resourcesEl.getTextContent()));
									}
									else {
										resources.put(key, new File(this.resourcesDir, resourcesEl.getTextContent()));
									}
								}

								for (Element webdirEl : DomUtils.getElementsByTagName(el, "webdir")) {
									webdirs.put(key, webdirEl.getTextContent());
								}

								ArrayList<ProfileEntry> profileList = new ArrayList<>();
								for (Element profileEl : DomUtils.getElementsByTagName(el, "profile")) {
									ProfileEntry profile = new ProfileEntry();
									String namespaceUri = DomUtils.getElementByTagName(profileEl, "namespace-uri")
										.getTextContent();
									String prefix = DomUtils.getElementByTagName(profileEl, "prefix").getTextContent();
									String localName = DomUtils.getElementByTagName(profileEl, "local-name")
										.getTextContent();
									profile.setQName(new QName(namespaceUri, localName, prefix));
									profile.setBaseSuite(suite.getQName());
									profile.setTitle(DomUtils.getElementByTagName(profileEl, "title").getTextContent());
									profileList.add(profile);
								}
								profiles.put(key, profileList);
							}
						}

						ArrayList<String> ccList = new ArrayList<>(); // Conformance class
																		// list.
						Element conformanceClasses = DomUtils.getElementByTagName(suiteEl, "BasicConformanceClasses");

						if (null != conformanceClasses) {
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
		}
		catch (Exception e) {
			LOGR.log(Level.SEVERE, "Could not extract infos from config file.", e);
		}
	}

	public File getScriptsDir() {
		if (null == this.scriptsDir) {
			File dir = new File(this.baseDir, "scripts");
			if (!dir.exists() && !dir.mkdir()) {
				throw new RuntimeException("Failed to create directory at " + dir.getAbsolutePath());
			}
			this.scriptsDir = dir;
		}
		return scriptsDir;
	}

	public File getUsersDir() {
		if (null == this.usersDir) {
			File dir = new File(this.baseDir, "users");
			if (!dir.exists() && !dir.mkdir()) {
				throw new RuntimeException("Failed to create directory at " + dir.getAbsolutePath());
			}
			this.usersDir = dir;
		}
		return usersDir;
	}

	private NodeList getOrganizations(File configFile) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setExpandEntityReferences(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = null;
		NodeList organizations = null;
		if (configFile.exists()) {
			doc = db.parse(configFile);
			Element configElem = (Element) (doc.getElementsByTagName("config").item(0));
			organizations = configElem.getElementsByTagName("organization");
		}
		else {
			// get config from classpath

			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			InputStream configFileStream = cl.getResourceAsStream("config.xml");
			try {
				doc = db.parse(configFileStream);
				// config files from test suite class paths start directly with
				// "organization" element
				organizations = doc.getElementsByTagName("organization");
			}
			catch (IllegalArgumentException e) {
				LOGR.log(Level.SEVERE, "Could not parse config file from class path.", e);
			}
		}
		return organizations;
	}

	public List<String> getOrganizationList() {
		return organizationList;
	}

	public Map<String, List<String>> getRevisionMap() {
		return revisionMap;
	}

	public Map<String, List<String>> getConformanceClassMap() {
		return conformanceClassMap;
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
