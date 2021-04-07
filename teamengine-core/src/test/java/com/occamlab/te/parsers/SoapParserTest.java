package com.occamlab.te.parsers;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SoapParserTest {

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void initFixture() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void parseWithSchema() throws Exception {
        Document schemaRefs = parseResourceDocument("/conf/soap-schema.xml");
        Document doc = parseResourceDocument("/soap-response.xml");
        StringWriter strWriter = new StringWriter();
        PrintWriter logger = new PrintWriter(strWriter);
        SoapParser iut = new SoapParser();
        Document result = iut.parse(doc, schemaRefs.getDocumentElement(), logger);
        assertNotNull(result);
    }

    /**
     * Parses an XML Document from a classpath resource.
     * 
     * @param resourcePath
     *            relative to the current class
     * @return never null
     */
    private Document parseResourceDocument(final String resourcePath)
            throws SAXException, IOException {
        final URL url = getClass().getResource(resourcePath);
        final Document doc = docBuilder.parse(url.toString());
        doc.setDocumentURI(url.toString());
        return doc;
    }
}
