/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * **************************************************************************
 *
 * Contributor(s):
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */
package com.occamlab.te.config;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.XMLConstants; // Addition for Fortify modifications

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.occamlab.te.SetupOptions;
import com.occamlab.te.util.XMLUtils;

/**
 * Creates the main Config File reading the tests under TE_BASE/scripts config files It
 * Aggregates by organization and by standard. The name of the organization and standards
 * needs to match to be able to aggregate the tests. For example, two tests using
 * Organization/name = 'OGC', will appear both under the 'OGC' organization. IF no tests
 * exist under TE_BASE/scripts , still creates a config file with two elements
 * config/scripts.
 *
 * If a previous config file exists, it will get deleted.
 *
 */
public class ConfigFileCreator {

	private static Logger LOGR = Logger.getLogger("com.occamlab.te.web.ConfigFileCreator");

	private DocumentBuilder builder;

	private Document docMain;

	private Element config;

	private Element scripts;

	public ConfigFileCreator() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			// Fortify Mod: prevent external entity injection
			documentBuilderFactory.setExpandEntityReferences(false);
			builder = documentBuilderFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Creates the main config file. IF <code>tebase</code> is not found it will throw a
	 * <code>TEBaseNotFoundException</code>.
	 * @param tebase
	 */
	public void create(String tebase) throws TEConfigException {
		File f = new File(tebase);
		if (f.exists()) {
			try {
				process(tebase);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			throw new TEBaseNotFoundException(tebase);
		}
	}

	/**
	 * Creates the main config file.
	 * @param tebase
	 */
	public void create(File tebase) throws TEConfigException {
		try {
			create(tebase.toString() + File.separator);
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void deleteConfigFile(String tebase) {
		File f = new File(tebase + "config.xml");
		if (f.exists()) {
			f.delete();
			LOGR.info("Old config file removed " + f);
		}
		else {
			LOGR.info("Config file not removed, since there was no file at " + f);
		}
	}

	/**
	 * Process the the tests under the TEBASE folder, and creates an integrated
	 * config.xml. If no tests are found, it stills create a basic config file with two
	 * elements configs/scripts.
	 * @param tebase - path to TEBASE. Expected to have under TEBASE/scripts
	 */
	private void process(String tebase) {
		deleteConfigFile(tebase);
		docMain = builder.newDocument();
		config = docMain.createElement("config");
		docMain.appendChild(config);
		scripts = docMain.createElement("scripts");
		config.appendChild(scripts);
		String scriptsDir = tebase + "scripts";
		LOGR.info("Scripts directory found at " + scriptsDir);

		File[] testScriptsDir = new File(scriptsDir).listFiles();
		if (testScriptsDir != null) {
			// if no tests are found under scripts, listFiles will return null,
			// if not iterated over the scripts directories
			for (File dir : testScriptsDir) {
				processDir(dir);
			}
		}
		String mainconfig = tebase + "config.xml";
		saveConfigFile(docMain, mainconfig);
	}

	public void processDir(File dir) {

		if (dir.isDirectory() && !dir.getName().startsWith(".")) {
			List<File> configFiles = getConfigFiles(dir);
			for (Iterator<File> iterator = configFiles.iterator(); iterator.hasNext();) {
				File file = iterator.next();
				processTestConfigFile(file);
			}
		}

	}

	private void processTestConfigFile(File configFile) {
		String configFilePath = null;
		Document docTest = null;
		if (configFile != null) {
			configFilePath = configFile.getAbsolutePath();
			docTest = getDocument(configFile);
		}
		if (docTest != null) {
			Node orgInTest = XMLUtils.getFirstNode(docTest, "/organization/name[1]");
			String org = orgInTest.getTextContent();
			String xpath = "/config/scripts/organization/name[text()='" + org + "']";
			Node orgInMainConfig = XMLUtils.getFirstNode(docMain, xpath);
			// org doesn't exist
			if (orgInMainConfig == null) {
				// append to scripts
				Node orgInTestImported = docMain.importNode(orgInTest.getParentNode(), true);
				scripts.appendChild(orgInTestImported);
			}
			else {
				Node standardInTest = XMLUtils.getFirstNode(docTest, "/organization/standard[1]");
				String standardInTestName = XMLUtils.getFirstNode(docTest, "/organization/standard[1]/name")
					.getTextContent();
				// check if a standard with this name already exists in main
				xpath = "/config/scripts/organization/standard/name[text()='" + standardInTestName + "']";

				Node standardInMain = XMLUtils.getFirstNode(docMain, xpath);
				if (standardInMain == null) {
					// append to an existing organization
					Node orgInMain = orgInMainConfig.getParentNode();
					orgInMain.appendChild(docMain.importNode(standardInTest, true));

				}
				else {
					// standard already exists in main config, so append to
					// standard

					Node versionInTest = XMLUtils.getFirstNode(docTest, "/organization/standard/version[1]");

					standardInMain.getParentNode().appendChild(docMain.importNode(versionInTest, true));
				}
			}
			LOGR.config("Added " + configFile.getAbsolutePath() + " to config file");
		}
		else {
			LOGR.config("No config file was found in dir " + configFilePath
					+ ". It was not registered in the main config file.");
		}
	}

	public void saveConfigFile(Document docMain, String mainconfig) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			// Fortify Mod: prevent external entity injection
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(docMain);

			StreamResult result = new StreamResult(new FileOutputStream(mainconfig));
			LOGR.info("SUCCESSFULLY created config.xml at " + mainconfig);
			transformer.transform(source, result);
			// Fortify Mod: Close the OutputStream associated with the StreamResult
			result.getOutputStream().close();
		}
		catch (Exception e) {
			LOGR.warning("The main config file was not created at " + mainconfig);
			e.printStackTrace();
		}
	}

	public Document getDocument(File xml) {
		try {
			return builder.parse(xml);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the all config file found under a directory
	 * @param dir
	 * @return A list of files found. Length = 0 if not found files.
	 */
	private List<File> getConfigFiles(File dir) {
		String[] extensions = { "xml" };
		List<File> configFiles = new ArrayList<>();

		Collection<File> files = FileUtils.listFiles(dir, extensions, true);
		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();
			if (file.getName().equals("config.xml")) {
				configFiles.add(file);
			}

		}
		return configFiles;
	}

	public static void main(String[] args) throws Exception {
		new SetupOptions();
		(new ConfigFileCreator()).create(SetupOptions.getBaseConfigDirectory());
	}

}
