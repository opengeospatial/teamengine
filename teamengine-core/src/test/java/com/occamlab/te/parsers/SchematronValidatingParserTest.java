package com.occamlab.te.parsers;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
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

}
