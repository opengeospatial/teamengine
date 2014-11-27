package com.occamlab.te.parsers;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class ImageParserTest {

    private static DocumentBuilder docBuilder;
    private static final String PARSERS_NS = "http://www.occamlab.com/te/parsers";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initFixture() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void parseText() throws SAXException, IOException {
        URL url = getClass().getResource("/jabberwocky.txt");
        Document instruct = docBuilder.parse(getClass().getResourceAsStream(
                "/img/model-argb-transparent.xml"));
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        Document result = ImageParser.parse(url.openConnection(),
                instruct.getDocumentElement(), logger);
        assertNull(result);
        assertTrue("Unexpected error message",
                strWriter.toString().startsWith("No image handlers available"));
    }

    @Test
    public void parsePNG_noAlphaChannel() throws SAXException, IOException {
        URL url = getClass().getResource("/img/square-white.png");
        Document instruct = docBuilder.parse(getClass().getResourceAsStream(
                "/img/model-argb-transparent.xml"));
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        Document result = ImageParser.parse(url.openConnection(),
                instruct.getDocumentElement(), logger);
        assertNotNull(result);
        Element countElem = (Element) result.getElementsByTagNameNS(PARSERS_NS,
                "count").item(0);
        int pixelCount = Integer.parseInt(countElem.getTextContent().trim());
        assertEquals("Unexpected number of transparent pixels.", pixelCount, 0);
    }

    @Test
    public void parsePNG_tRNSChunk() throws SAXException, IOException {
        URL url = getClass().getResource("/img/square-white.png");
        Document instruct = docBuilder.parse(getClass().getResourceAsStream(
                "/img/metadata.xml"));
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        Document result = ImageParser.parse(url.openConnection(),
                instruct.getDocumentElement(), logger);
        assertNotNull(result);
        Element tRNSElem = (Element) result.getElementsByTagName("tRNS")
                .item(0);
        // used to specify a single color as fully transparent
        assertTrue("Expected tRNS chunk in image.", tRNSElem.hasChildNodes());
    }

    @Test
    public void parsePNG_hasAlphaChannelWithTransparentPixels()
            throws SAXException, IOException {
        URL url = getClass().getResource("/img/dice-alpha-transparency.png");
        Document instruct = docBuilder.parse(getClass().getResourceAsStream(
                "/img/model-argb-transparent.xml"));
        PrintWriter logger = new PrintWriter(new StringWriter());
        Document result = ImageParser.parse(url.openConnection(),
                instruct.getDocumentElement(), logger);
        assertNotNull(result);
        Element countElem = (Element) result.getElementsByTagNameNS(PARSERS_NS,
                "count").item(0);
        int pixelCount = Integer.parseInt(countElem.getTextContent().trim());
        assertTrue("Expected transparent pixels in image.", pixelCount > 0);
    }

    @Test
    public void parseUnsupportedImageFormat_WebP() throws SAXException,
            IOException {
        URL url = getClass().getResource("/img/fjord.webp");
        Document instruct = docBuilder.parse(getClass().getResourceAsStream(
                "/img/metadata.xml"));
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        Document result = ImageParser.parse(url.openConnection(),
                instruct.getDocumentElement(), logger);
        assertNull(result);
        assertTrue("Unexpected error message",
                strWriter.toString().startsWith("No image handlers available"));
    }

    @Test
    public void supportedImageFormats() throws SAXException, IOException {
        String types = ImageParser.getSupportedImageTypes();
        String[] formats = types.split(",");
        // https://docs.oracle.com/javase/7/docs/api/javax/imageio/package-summary.html
        assertTrue("Expected at least 5 supported image types",
                formats.length >= 5);
    }
}
