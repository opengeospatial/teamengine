package com.occamlab.te.util;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.impl.Constants;

/**
 * Provides various utility methods for constructing JAXP parsers.
 * 
 */
public class XMLParserUtils {

    /**
     * Creates a SAXParser that is configured to resolve XInclude references but
     * not perform schema validation.
     * 
     * @param doBaseURIFixup
     *            A boolean value that specifies whether or not to add xml:base
     *            attributes when resolving xi:include elements; adding these
     *            attributes may render an instance document schema-invalid.
     * @return An XInclude-aware SAXParser instance.
     * 
     * @see <a href="http://www.w3.org/TR/xinclude/">XML Inclusions (XInclude)
     *      Version 1.0, Second Edition</a>
     */
    public static SAXParser createXIncludeAwareSAXParser(boolean doBaseURIFixup) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);
        SAXParser parser = null;
        try {
            factory.setFeature(Constants.XERCES_FEATURE_PREFIX
                    + Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE,
                    doBaseURIFixup);
            parser = factory.newSAXParser();
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
        return parser;
    }

    private XMLParserUtils() {
    }
}
