package net.sf.teamengine.spatial.kml2;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

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
 * Assesses unmarshalling behaviour using a variety of valid and invalid  
 * KML geometry instances.
 */
public class KmlUnmarshallingBehavior {
    private static final String KML21_NS_URI = "http://earth.google.com/kml/2.1";
    private static DocumentBuilder domBuilder;
    private static CatalogResolver resolver;
    private static KmlGeometryUnmarshaller kmlUnmarshaller = 
        new KmlGeometryUnmarshaller();

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
    }

    /**
     * Tears down the test fixture after every test case method.
     * 
     */
   @After
    public void afterword() {
    }

    @Test
    public void unmarshalPolygon() {
        Document kmlDoc = null;
        try {
          kmlDoc = domBuilder.parse(resolver.resolveEntity(null, "kml-2.1/ValidPolygon"));
        } catch (SAXException e) {
          System.err.println(e.getMessage());
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
        NodeList polys = kmlDoc.getElementsByTagNameNS(KML21_NS_URI, "Polygon");
        Geometry geom = kmlUnmarshaller.unmarshalKmlGeometry(polys.item(0));
        Assert.assertEquals("Wrong geometry type", "Polygon", geom.getGeometryType());
        Polygon poly = (Polygon) geom; 
        Assert.assertEquals("Too many interior rings", 1, poly.getNumInteriorRing());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void unmarshalUnsupportedGeometry() {
        Document kmlDoc = null;
        try {
          kmlDoc = domBuilder.parse(resolver.resolveEntity(null, "kml-2.1/UnsupportedGeometry"));
        } catch (SAXException e) {
          System.err.println(e.getMessage());
        } catch (IOException e) {
          System.err.println(e.getMessage());
        }
        NodeList boxes = kmlDoc.getElementsByTagNameNS(KML21_NS_URI, "Box");
        Geometry geom = kmlUnmarshaller.unmarshalKmlGeometry(boxes.item(0));
    }
}