package net.sf.teamengine.spatial.kml2;

import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.resolver.tools.CatalogResolver;

import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass; 
import org.junit.After;
import org.junit.Assert;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Checks validation behaviour using a variety of valid and invalid Polygon 
 * geometry instances.
 */
public class PolygonTestSuite {
    private static final String KML21_NS_URI = "http://earth.google.com/kml/2.1";
    private static DocumentBuilder domBuilder;
    private static CatalogResolver resolver;
    private static KmlGeometryValidator geoValidator = 
        new KmlGeometryValidator();
    private String msgKey;

    /**
     * Initializes a DocumentBuilder and a CatalogResolver that uses OASIS entity 
     * catalogs to resolve system identifiers or URIs. The catalogs to load are 
     * read from the "xml.catalog.files" system property, the value of which 
     * must be a semicolon-separated list of (absolute) catalog file locations.
     */
    @BeforeClass
    public static void init() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
          domBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
          System.err.println(e.getMessage());
        }
        resolver = new CatalogResolver();
        String baseURI = resolver.getCatalog().getCurrentBase();
        System.out.println("Catalog baseURI = " + baseURI );
    }
    
    /**
     * Sets up the test fixture before every test case method.
     * 
     */
    @Before
    public void foreword() {
        msgKey = new String(UUID.randomUUID().toString());
        System.out.println("Message key = " + msgKey);
    }

    /**
     * Tears down the test fixture after every test case method.
     * 
     */
   @After
    public void afterword() {
        msgKey = null;
    }

    @Test
    public void checkSelfIntersectingPolygon() {
        Document kmlDoc = null;
        try {
          kmlDoc = domBuilder.parse(resolver.resolveEntity(null, "kml-2.1/SelfIntersectingPolygon"));
        } catch (SAXException e) {
          System.err.println(e.getMessage());
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
        NodeList polys = kmlDoc.getElementsByTagNameNS(KML21_NS_URI, "Polygon");
        Assert.assertFalse("A Polygon with crossing rings is invalid", 
            geoValidator.isValidGeometry(polys.item(0), msgKey));
        Assert.assertTrue("No error message provided", 
            geoValidator.getErrorMessage(msgKey).length() > 0);
    }
    
    @Test
    public void checkValidPolygon() {
        Document kmlDoc = null;
        try {
          kmlDoc = domBuilder.parse(resolver.resolveEntity(null, "kml-2.1/ValidPolygon"));
        } catch (SAXException e) {
          System.err.println(e.getMessage());
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
        NodeList polys = kmlDoc.getElementsByTagNameNS(KML21_NS_URI, "Polygon");
        Assert.assertTrue("Polygon is valid", 
            geoValidator.isValidGeometry(polys.item(0), msgKey));
        Assert.assertFalse("Error message exists", 
            geoValidator.getErrorMessage(msgKey).length() > 0);
    }
}