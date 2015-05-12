package com.occamlab.te.web;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConfigFileCreatorTest {
	private File classp;
	private DocumentBuilder builder;

	@Before
	public void getcp() {

		classp = new File(ConfigFileCreatorTest.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath());

		// System.out.println(classp.getPath()+" exists "+classp.exists());
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
		File tebase = new File(classp, "tebase");

		ConfigFileCreator configFileCreator = new ConfigFileCreator();
		configFileCreator.create(tebase);
		File configFile = new File(tebase + File.separator + "config.xml");
		try {
			Document config = builder.parse(configFile);
			Node node = XMLUtils
					.getFirstNode(
							config,
							"/config/scripts/organization[child::name/text()='OGC']/standard[child::name/text()='Catalogue Service - Web (CSW)']/"
									+ "version[child::name/text()='3.0.0']/suite/namespace-uri");
			assertTrue(
					"The config file of a test (e.g. CAT 3.0) was copied deepely",
					node.getTextContent().equals(
							"http://www.opengis.net/cite/cat30"));

			NodeList orgs = XMLUtils.getAllNodes(config,
					"/config/scripts/organization");
			assertEquals("Only 2 organizations where created", 2,
					orgs.getLength());

			String xpath = "/config/scripts/organization[child::name/text()='OGC']/standard[child::name/text()='Catalogue Service - Web (CSW)']/version";
			System.out.println(xpath);
			NodeList vers = XMLUtils.getAllNodes(config, xpath);
			assertEquals("Only 2 standards where created under CSW", 2,
					vers.getLength());
			configFile.delete();

		} catch (SAXException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	@Test
	public void testParsingMultipleOranizations() {

		try {
			File tebase = new File(classp, "tebase-multiple-organizations");
			ConfigFileCreator configFileCreator = new ConfigFileCreator();
			configFileCreator.create(tebase);
			File configFile = new File(tebase + File.separator + "config.xml");

			Document config = builder.parse(configFile);

			NodeList orgs = XMLUtils.getAllNodes(config,
					"/config/scripts/organization");
			assertEquals(2, orgs.getLength());

			configFile.delete();

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}
	
	@Test
	public void testParsingFoldersWithHiddenFiles() {

		try {
			File tebase = new File(classp, "tebase-hidden-files");
			ConfigFileCreator configFileCreator = new ConfigFileCreator();
			configFileCreator.create(tebase);
			File configFile = new File(tebase + File.separator + "config.xml");

			Document config = builder.parse(configFile);

			NodeList orgs = XMLUtils.getAllNodes(config,
					"/config/scripts/organization");
			assertEquals(2, orgs.getLength());

			configFile.delete();

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}
	
	@Test
	public void testSingleTestTEBASEwithNoConfigFileInATest() {

		try {
			File tebase = new File(classp, "tebase-no-config-in-test");
			ConfigFileCreator configFileCreator = new ConfigFileCreator();
			configFileCreator.create(tebase);
			File configFile = new File(tebase + File.separator + "config.xml");

			Document config = builder.parse(configFile);

			NodeList orgs = XMLUtils.getAllNodes(config,
					"/config/scripts/organization");
			assertEquals(0, orgs.getLength());

			configFile.delete();

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}
	
	@Test
	public void testSingleTestTEBASEwithScriptsEmpty() {

		try {
			File tebase = new File(classp, "tebase-empty");
			ConfigFileCreator configFileCreator = new ConfigFileCreator();
			configFileCreator.create(tebase);
			File configFile = new File(tebase + File.separator + "config.xml");

			Document config = builder.parse(configFile);

			NodeList orgs = XMLUtils.getAllNodes(config,
					"/config/scripts/organization");
			assertEquals(0, orgs.getLength());

			configFile.delete();

		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}

	}
	
	@Test
	public void testTEBASEnotfound() {


			File tebase = new File(classp, "xyzzxsyss");
			ConfigFileCreator configFileCreator = new ConfigFileCreator();
			try{
				configFileCreator.create(tebase);
			}catch (TEException e){
				assertEquals("com.occamlab.te.web.TEBaseNotFoundException", e.getClass().getName());
				return;
				
			}
			fail("it should throw aTEBaseNotFoundException Exception");
	}
	
	/**
	 * Tests to properly parsed when multiple versions of a test exist. for example SFS 1.2,1.2,1.3 etc, 
	 * and they are all under SFS directory.
	 */
	@Test
	public void testMultipleVersionsOfATest(){
		try {
		
		File tebase = new File(classp, "tebase-multiple-versions-of-a-test");
		ConfigFileCreator configFileCreator = new ConfigFileCreator();
		configFileCreator.create(tebase);
		File configFile = new File(tebase + File.separator + "config.xml");
		Document config = builder.parse(configFile);
		NodeList numberOfTests = XMLUtils.getAllNodes(config,
				"/config/scripts/organization/standard/version");
		assertEquals(3, numberOfTests.getLength());
		
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
	


}
