package com.occamlab.te.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;

import org.apache.xerces.impl.Constants;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParserUtilsTest {

    @Test
    public void xincludeParserNoBaseURIFixup() throws SAXException {
        SAXParser parser = XMLParserUtils.createXIncludeAwareSAXParser(false);
        assertNotNull(parser);
        XMLReader reader = parser.getXMLReader();
        boolean baseURIFixup = reader
                .getFeature(Constants.XERCES_FEATURE_PREFIX
                        + Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE);
        assertFalse("Expected feature to be false: "
                + Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE, baseURIFixup);
    }

    @Test
    public void resolveXInclude_omitXMLBase() throws SAXException, IOException {
        File file = new File("src/test/resources/article.xml");
        SAXParser parser = XMLParserUtils.createXIncludeAwareSAXParser(false);
        LegalNoticeHandler handler = new LegalNoticeHandler();
        parser.parse(file, handler);
    }

    @Test(expected = AssertionError.class)
    public void resolveXInclude_keepXMLBase() throws SAXException, IOException {
        File file = new File("src/test/resources/article.xml");
        SAXParser parser = XMLParserUtils.createXIncludeAwareSAXParser(true);
        LegalNoticeHandler handler = new LegalNoticeHandler();
        parser.parse(file, handler);
    }

    class LegalNoticeHandler extends DefaultHandler {
        public void startElement(String uri, String localName, String qName,
                Attributes attribs) throws SAXException {
            if (localName.equals("legalnotice")
                    && (attribs.getIndex(XMLConstants.XML_NS_URI, "base") > 0)) {
                throw new AssertionError("Found xml:base attribute.");
            }
        }
    }

}
