package com.occamlab.te.web;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Creates the main Config File reading the tests under TE_BASE/scripts config
 * files It Aggregates by organization and by standard. The name of the
 * organization and standards needs to match to be able to aggregate the tests.
 * Fir example to test using Organization/name =OGC, will appear both under the
 * OGC organization
 * 
 * @author lbermudez
 */
public class ConfigFileCreator {

	private static Logger LOGR = Logger
			.getLogger("com.occamlab.te.web.ConfigFileCreator");

	private DocumentBuilder builder;

	public ConfigFileCreator() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			builder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		}
	}

	public void create(String tebase) {

		try {
			
			process(tebase);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void create(File tebase) {
		try {
			process(tebase.toString() + File.separator);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void deleteConfigFile(String tebase){
		File f=  new File(tebase + "config.xml");
		if (f.exists()){
			f.delete();
			LOGR.info("old onfig file removed");
		}else{
			LOGR.info("config file not removed, since there was no file");
		}
	}

	/**
	 * Process the the tests under the scripts folder, and creates an integrated
	 * config.xml
	 * 
	 * @param tebase
	 * @throws Exception
	 */
	private void process(String tebase) throws Exception {
		deleteConfigFile(tebase);
		

		Document docMain = builder.newDocument();
		Element config = docMain.createElement("config");
		docMain.appendChild(config);
		Element scripts = docMain.createElement("scripts");
		config.appendChild(scripts);
		String scriptsDir = tebase + "scripts";

		File[] testScriptsDir = new File(scriptsDir)
				.listFiles();

		for (File dir : testScriptsDir) {
			if (dir.isDirectory() && !dir.getName().startsWith(".")) {
				LOGR.info("processing dir " + dir);
				File configFile = getFirstConfigFileFound(dir);
			

				Document docTest = getDocument(configFile);
				Node orgInTest = XMLUtils.getFirstNode(docTest,
						"/organization/name[1]");
				String org = orgInTest.getTextContent();

				String xpath = "/config/scripts/organization/name[text()='"
						+ org + "']";
				Node orgInMainConfig = XMLUtils.getFirstNode(docMain, xpath);

				// org doesn't exist
				if (orgInMainConfig == null) {
					// append to scripts
					Node orgInTestImported = docMain.importNode(
							orgInTest.getParentNode(), true);

					scripts.appendChild(orgInTestImported);

				} else {
					Node standardInTest = XMLUtils.getFirstNode(docTest,
							"/organization/standard[1]");
					String standardInTestName = XMLUtils.getFirstNode(docTest,
							"/organization/standard[1]/name").getTextContent();

					// check if a standard with this name already exists in main
					// config
					xpath = "/config/scripts/organization/standard/name[text()='"
							+ standardInTestName + "']";

					Node standardInMain = XMLUtils.getFirstNode(docMain, xpath);
					if (standardInMain == null) {
						// append to an existing organization
						Node orgInMain = orgInMainConfig.getParentNode();
						orgInMain.appendChild(docMain.importNode(
								standardInTest, true));

					} else {
						// standard already exists in main config, so append to
						// a
						// standard element
						Node versionInTest = XMLUtils.getFirstNode(docTest,
								"/organization/standard/version[1]");

						standardInMain.getParentNode().appendChild(
								docMain.importNode(versionInTest, true));

					}
				}
			}
		}

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(docMain);
		String mainconfig = tebase + "config.xml";
		LOGR.info("Creating the config.xml at " + mainconfig);
		StreamResult result = new StreamResult(new FileOutputStream(mainconfig));
		transformer.transform(source, result);

	}

	public Document getDocument(File xml) {
		try {

			Document doc = builder.parse(xml);
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Returns the first config file founc in each test
	 * 
	 * @param dir
	 * @return the file found or null if not found
	 */
	private File getFirstConfigFileFound(File dir) {
		String[] extensions = {"xml"};
		
		Collection<File> files = FileUtils.listFiles(dir,extensions, true);
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();
			if (file.getName().equals("config.xml")) {
				return file;
			}
			
		}
	
		return null;
	
	}

}
