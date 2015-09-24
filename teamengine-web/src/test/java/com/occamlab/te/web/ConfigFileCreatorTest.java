package com.occamlab.te.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
    public void createDocBuilder() throws ParserConfigurationException, URISyntaxException {
        this.classp = new File(getClass().getResource("/").toURI());
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        builder = documentBuilderFactory.newDocumentBuilder();
    }

    @Test
    public void testMultipleTestsUnderTEBASE() throws URISyntaxException {
        File tebase = new File(classp, "tebase");
        ConfigFileCreator configFileCreator = new ConfigFileCreator();
        configFileCreator.create(tebase);
        File configFile = new File(tebase, "config.xml");
        try {
            Document config = builder.parse(configFile);
            Node node = XMLUtils.getFirstNode(config,
                    "/config/scripts/organization[child::name/text()='OGC']/standard[child::name/text()='Catalogue Service - Web (CSW)']/"
                            + "version[child::name/text()='3.0.0']/suite/namespace-uri");
            assertTrue("The config file of a test (e.g. CAT 3.0) was copied deepely",
                    node.getTextContent().equals("http://www.opengis.net/cite/cat30"));
            NodeList orgs = XMLUtils.getAllNodes(config, "/config/scripts/organization");
            assertEquals("Expected 1 organization", 1, orgs.getLength());
            String xpath = "/config/scripts/organization[child::name/text()='OGC']/standard[child::name/text()='Catalogue Service - Web (CSW)']/version";
            NodeList vers = XMLUtils.getAllNodes(config, xpath);
            assertEquals("Expected 2 CSW standards", 2, vers.getLength());
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

            NodeList orgs = XMLUtils.getAllNodes(config, "/config/scripts/organization");
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

            NodeList orgs = XMLUtils.getAllNodes(config, "/config/scripts/organization");
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

            NodeList orgs = XMLUtils.getAllNodes(config, "/config/scripts/organization");
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

            NodeList orgs = XMLUtils.getAllNodes(config, "/config/scripts/organization");
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
        try {
            configFileCreator.create(tebase);
        } catch (TEException e) {
            assertEquals("com.occamlab.te.web.TEBaseNotFoundException", e.getClass().getName());
            return;

        }
        fail("it should throw aTEBaseNotFoundException Exception");
    }

    /**
     * Tests to properly parsed when multiple versions of a test exist. for
     * example SFS 1.2,1.2,1.3 etc, and they are all under SFS directory.
     */
    @Test
    public void testMultipleVersionsOfATest() {
        try {

            File tebase = new File(classp, "tebase-multiple-versions-of-a-test");
            ConfigFileCreator configFileCreator = new ConfigFileCreator();
            configFileCreator.create(tebase);
            File configFile = new File(tebase + File.separator + "config.xml");
            Document config = builder.parse(configFile);
            NodeList numberOfTests = XMLUtils.getAllNodes(config, "/config/scripts/organization/standard/version");
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
