package net.sf.teamengine.spatial.kml2;

import java.util.Hashtable;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Checks that a geometry instance satisfies all relevant topological constraints.
 */
public class KmlGeometryValidator {

    private static Hashtable<String,String> validationMessages;
    private KmlGeometryUnmarshaller kmlUnmarshaller;

    public KmlGeometryValidator() {
        this.kmlUnmarshaller = new KmlGeometryUnmarshaller();
        this.validationMessages = new Hashtable<String,String>();
    }

    /**
     * Checks that a geometry element is topologically correct. A failure to
     * construct the corresponding JTS geometry object is taken as evidence 
     * that it is not.
     *
     * @param node
     *            a DOM node (Document or Element) parsed from a KML geometry 
     *            element.
     * @param key
     *            a String value specifying a key for accessing an exception 
     *            message that may arise (using the {@link #getMessage(String) getMessage} 
     *            method).
     * @return true if the given node represents a valid geometry instance.
     */
    public boolean validGeometry(Node node, String key) {

        Geometry geom = null;
        try {
          geom = this.kmlUnmarshaller.unmarshalKmlGeometry(node);
        } catch (RuntimeException rex) {
            validationMessages.put(key, rex.getMessage());
        }
        return (geom != null);
    }
    
    /**
     * Retrieves the message describing why a geometry instance is invalid. The
     * message is discarded following retrieval.
     *
     * @param key
     *          a String specifying the key to look up a given message
     * @return  a String containing the corresponding error message (an empty 
     *          string if no detail message is available).
     */
    public static String getMessage(String key) {
        String msg = null;
        if (validationMessages.containsKey(key)) {
            msg = validationMessages.remove(key);
        } else {
            msg = new String();
        }
        return msg;
    }
}
