package com.occamlab.te.parsers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

public class XMLValidatingParserTest {

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void initFixture() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void createParserWithCswSchemaAsResource() throws Exception {
        Document schemaRefs = docBuilder.parse(getClass().getResourceAsStream(
                "/schema-refs.xml"));
        XMLValidatingParser iut = new XMLValidatingParser(schemaRefs);
        assertNotNull(iut);
        assertEquals("Unexpected number of schema references", 1,
                iut.schemaList.size());
        assertTrue("Expected File instance.",
                File.class.isInstance(iut.schemaList.get(0)));
    }

    @Test
    public void parseCswCapabilities_valid() throws Exception {
        Document schemaRefs = docBuilder.parse(getClass().getResourceAsStream(
                "/schema-refs-ipo.xml"));
        URL url = getClass().getResource("/ipo.xml");
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(url.openConnection(),
                schemaRefs.getDocumentElement(), logger);
        assertNotNull(result);
        assertTrue("Unexpected validation error(s) reported.", strWriter
                .toString().isEmpty());
    }

    @Test
    public void parseCswCapabilities_invalid() throws Exception {
        Document schemaRefs = docBuilder.parse(getClass().getResourceAsStream(
                "/schema-refs-ipo.xml"));
        URL url = getClass().getResource("/ipo-invalid.xml");
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(url.openConnection(),
                schemaRefs.getDocumentElement(), logger);
        assertNull(result);
        assertFalse("Expected validation error(s).", strWriter.toString()
                .isEmpty());
    }

}
