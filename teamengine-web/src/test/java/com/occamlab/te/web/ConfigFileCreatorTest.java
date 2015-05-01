package com.occamlab.te.web;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigFileCreatorTest extends TestCase {

	private DocumentBuilder builder;
	private File localPath;

	protected void setUp() throws Exception {
		super.setUp();
		localPath = new File(ConfigFileCreatorTest.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath());
		createBuilder();

	}

	private void createBuilder() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		try {
			builder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {

			e.printStackTrace();
		}
	}

	@Test
	public void testMultipleTestsUnderTEBASE() {
		File tebase = new File(localPath + File.separator + "tebase");
		ConfigFileCreator configFileCreator = new ConfigFileCreator();
		configFileCreator.create(tebase);
		File configFile = new File(tebase + File.separator +"config.xml");
		try {
			Document config = builder.parse(configFile);
			Node node = XMLUtils.getFirstNode(
					config,
					"/config/scripts/organization[child::name/text()='OGC']/standard[child::name/text()='Catalogue Service - Web (CSW)']/"+
					"version[child::name/text()='3.0.0']/suite/namespace-uri");
			assertTrue("The config file of a test (e.g. CAT 3.0) was copied deepely", node.getTextContent().equals("http://www.opengis.net/cite/cat30"));
			
			
			NodeList orgs = XMLUtils.getAllNodes(config, "/config/scripts/organization");
			assertEquals("Only 2 organizations where created",2, orgs.getLength());
			
			String xpath = "/config/scripts/organization[child::name/text()='OGC']/standard[child::name/text()='Catalogue Service - Web (CSW)']/version";
			System.out.println(xpath);
			NodeList vers = XMLUtils.getAllNodes(config,xpath);
			assertEquals("Only 2 standards where created under CSW" , 2,vers.getLength());
			
			

		} catch (SAXException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		configFile.delete();

	}
	@Test
	public void testSingleTestUnderTEBASE() {
		
		File tebase = new File(localPath + File.separator + "tebase-onlyone");
		ConfigFileCreator configFileCreator = new ConfigFileCreator();
		configFileCreator.create(tebase);
		File configFile = new File(tebase + File.separator +"config.xml");
		try {
			Document config = builder.parse(configFile);
				
			NodeList orgs = XMLUtils.getAllNodes(config, "/config/scripts/organization");
			assertEquals(1, orgs.getLength());	
			

		} catch (SAXException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		configFile.delete();
		
		
		
	}

	
	@Test
	public void testWithOutConfig() {

	}

}
