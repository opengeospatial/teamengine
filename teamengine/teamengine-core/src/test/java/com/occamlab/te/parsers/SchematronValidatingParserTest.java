package com.occamlab.te.parsers;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SchematronValidatingParserTest {

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void setUpClass() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void validateAbbreviatedContentPhase() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities-pycsw.xml"));
        SchematronValidatingParser validator = new SchematronValidatingParser();
        NodeList errList = validator.validate(doc, "/sch/csw-capabilities.sch",
                "AbbreviatedContentPhase");
        assertEquals("Unexpected number of errors.", 3, errList.getLength());
    }

    @Test
    public void validateDefaultPhase() throws SAXException, IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/capabilities-pycsw.xml"));
        SchematronValidatingParser validator = new SchematronValidatingParser();
        NodeList errList = validator.validate(doc, "/sch/csw-capabilities.sch",
                null);
        assertEquals("Unexpected number of errors.", 1, errList.getLength());
    }

    @Test
    public void validateVersionNegotiationFailedPhase() throws SAXException,
            IOException {
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/exception.xml"));
        SchematronValidatingParser validator = new SchematronValidatingParser();
        NodeList errList = validator.validate(doc, "/sch/ExceptionReport.sch",
                "VersionNegotiationFailedPhase");
        assertEquals("Unexpected number of errors.", 0, errList.getLength());
    }

    @Test
    public void parseDocumentOk() throws Exception {
        Writer strWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(strWriter);
        StringBuilder xml = new StringBuilder(
                "<tep:schemas xmlns:tep='http://www.occamlab.com/te/parsers'>");
        xml.append("<tep:schema type='resource' phase='VersionNegotiationFailedPhase'>");
        xml.append("/sch/ExceptionReport.sch");
        xml.append("</tep:schema></tep:schemas>");
        Document instr = docBuilder.parse(new InputSource(new StringReader(xml
                .toString())));
        Document doc = docBuilder.parse(this.getClass().getResourceAsStream(
                "/exception.xml"));
        SchematronValidatingParser parser = new SchematronValidatingParser();
        Document result = parser.parse(doc, instr.getDocumentElement(), writer);
        assertNotNull(result);
        assertTrue("Unexpected error messages.", strWriter.toString().isEmpty());
    }

    @Test
    public void parseInputStreamExpectViolations() throws Exception {
        Writer strWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(strWriter);
        StringBuilder xml = new StringBuilder(
                "<tep:schemas xmlns:tep='http://www.occamlab.com/te/parsers'>");
        xml.append("<tep:schema type='resource' phase='#ALL'>");
        xml.append("/sch/csw-capabilities.sch");
        xml.append("</tep:schema></tep:schemas>");
        Document instr = docBuilder.parse(new InputSource(new StringReader(xml
                .toString())));
        InputStream input = this.getClass().getResourceAsStream(
                "/capabilities-pycsw.xml");
        SchematronValidatingParser parser = new SchematronValidatingParser();
        Document result = parser.parse(input, instr.getDocumentElement(),
                writer);
        assertNull(result);
        assertFalse("Expected error messages.", strWriter.toString().isEmpty());
    }
}
