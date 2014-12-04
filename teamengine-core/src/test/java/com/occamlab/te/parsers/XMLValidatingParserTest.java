package com.occamlab.te.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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

    @Test
    public void validateWithNullSchemaListAndNoSchemaLocation()
            throws SAXException, IOException {
        Document doc = docBuilder.parse(getClass().getResourceAsStream(
                "/ipo.xml"));
        XmlErrorHandler errHandler = new XmlErrorHandler();
        XMLValidatingParser iut = new XMLValidatingParser();
        iut.validateAgainstXMLSchemaList(doc, null, errHandler);
        assertFalse("Expected one or more validation errors.",
                errHandler.isEmpty());
    }

    @Test
    public void validateWithNullSchemaListAndSchemaLocation()
            throws SAXException, IOException, URISyntaxException {
        URL url = getClass().getResource("/ipo-schemaLoc.xml");
        Document doc = docBuilder.parse(url.openStream());
        doc.setDocumentURI(url.toString());
        XmlErrorHandler errHandler = new XmlErrorHandler();
        XMLValidatingParser iut = new XMLValidatingParser();
        iut.validateAgainstXMLSchemaList(doc, null, errHandler);
        List<String> errList = errHandler.toList();
        assertEquals("Unexpected number of validation errors reported.", 0,
                errList.size());
    }

    @Test
    public void parseWithNullInstructionAndMultipleSchemaLocations()
            throws Exception {
        URL docUrl = getClass().getResource("/ipo-multipleSchemaRefs.xml");
        Document doc = docBuilder.parse(docUrl.openStream());
        doc.setDocumentURI(docUrl.toString());
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(doc, null, logger);
        assertNotNull(result);
        assertTrue("Unexpected validation error(s) were reported.", strWriter
                .toString().isEmpty());
    }

    @Test
    public void parseWithEmptySchemaListAndMultipleSchemaLocations()
            throws Exception {
        Document schemaRefs = docBuilder.parse(getClass().getResourceAsStream(
                "/conf/no-schema-refs.xml"));
        URL docUrl = getClass().getResource("/ipo-multipleSchemaRefs.xml");
        Document doc = docBuilder.parse(docUrl.openStream());
        doc.setDocumentURI(docUrl.toString());
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        XMLValidatingParser iut = new XMLValidatingParser();
        Document result = iut.parse(doc, schemaRefs.getDocumentElement(),
                logger);
        assertNotNull(result);
        assertTrue("Unexpected validation error(s) were reported.", strWriter
                .toString().isEmpty());
    }

}
