package com.occamlab.te.parsers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hamcrest.core.StringContains;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

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
                "/conf/schema-refs.xml"));
        XMLValidatingParser iut = new XMLValidatingParser(schemaRefs);
        assertNotNull(iut);
        assertEquals("Unexpected number of schema references", 1,
                iut.schemaList.size());
        assertTrue("Expected File instance.",
                File.class.isInstance(iut.schemaList.get(0)));
    }

    @Test
    public void parsePurchaseOrder_valid() throws Exception {
        Document schemaRefs = docBuilder.parse(getClass().getResourceAsStream(
                "/conf/schema-refs-ipo.xml"));
        URL url = getClass().getResource("/ipo.xml");
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(url.openConnection(),
                schemaRefs.getDocumentElement(), logger);
        assertNotNull(result);
        assertTrue("Unexpected validation error(s) reported.", strWriter
                .toString().isEmpty());
        assertEquals("Document element has unexpected local name", result
                .getDocumentElement().getLocalName(), "purchaseOrder");
    }

    @Test
    public void parsePurchaseOrder_invalid() throws Exception {
        Document schemaRefs = docBuilder.parse(getClass().getResourceAsStream(
                "/conf/schema-refs-ipo.xml"));
        URL url = getClass().getResource("/ipo-invalid.xml");
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(url.openConnection(),
                schemaRefs.getDocumentElement(), logger);
        assertNull(result);
        assertFalse("Expected validation error(s) but none reported.",
                strWriter.toString().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void parseWithNullInstructionThrowsNPE() throws IOException,
            Exception {
        URL docUrl = getClass().getResource("/ipo-multipleSchemaRefs.xml");
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(docUrl.openConnection(), null, logger);
        assertNull(result);
    }

    @Test
    public void parseWithoutSchemasReportsNoGrammarFound() throws IOException,
            Exception {
        Document schemaRefs = docBuilder.parse(getClass().getResourceAsStream(
                "/conf/no-schema-refs.xml"));
        URL docUrl = getClass().getResource("/ipo-multipleSchemaRefs.xml");
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(docUrl.openConnection(),
                schemaRefs.getDocumentElement(), logger);
        assertNull(result);
        assertFalse("Expected validation error(s) but none reported.",
                strWriter.toString().isEmpty());
        assertThat("Expected error message containing: no grammar found",
                strWriter.toString(),
                StringContains.containsString("no grammar found"));
    }

}
