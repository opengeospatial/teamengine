package net.sf.teamengine.spatial.kml2;

import java.util.Hashtable;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

/**
 * Checks that a geometry instance satisfies all relevant validity constraints.
 * See <a target="_blank" href="http://portal.opengeospatial.org/files/?artifact_id=18241">
 * OGC 06-103r3</a> or the <a target="_blank" href="http://www.vividsolutions.com/jts/javadoc/index.html">
 * JTS API documentation</a> for a general description of these.
 */
public class KmlGeometryValidator {

    private static Hashtable<String,String> validationMessages;
    private KmlGeometryUnmarshaller kmlUnmarshaller;

    public KmlGeometryValidator() {
        this.kmlUnmarshaller = new KmlGeometryUnmarshaller();
        this.validationMessages = new Hashtable<String,String>();
    }

    /**
     * Checks that a geometry element is topologically correct. 
     *
     * @param node
     *            a DOM node (Document or Element) parsed from a KML geometry 
     *            element.
     * @param msgKey
     *            a String value specifying a key for accessing a validation  
     *            error message (using the {@link #getMessage(String) getMessage} 
     *            method).
     * @return true if the given node represents a valid geometry instance.
     */
    public boolean isValidGeometry(Node node, String msgKey) {

        Geometry geom = null;
        TopologyValidationError err = null;
        try {
          geom = this.kmlUnmarshaller.unmarshalKmlGeometry(node);
          err = validate(geom);
          if (null != err) {
            validationMessages.put(msgKey, err.getMessage());
          }
        } catch (RuntimeException rex) {
            validationMessages.put(msgKey, rex.getMessage());
        }
        return ((null != geom) && (null == err));
    }
    
    
    /**
     * Retrieves the message describing a validation error. The message is
     * discarded following retrieval.
     *
     * @param msgKey
     *          a String specifying the key to look up a given message
     * @return  a String containing the corresponding error message (an empty 
     *          string if no detail message is available).
     */
    public static String getErrorMessage(String msgKey) {
        String msg = null;
        if (validationMessages.containsKey(msgKey)) {
            msg = validationMessages.remove(msgKey);
        } else {
            msg = new String();
        }
        return msg;
    }
    
    
    /**
     *  Checks that the given geometry is topologically valid. 
     *
     * @param geom
     *          the JTS Geometry instance to be validated
     * @return  a TopologyValidationError object that dsecribes a validation  
     *          error, or <code>null</code> if there is no error.
     */
    private TopologyValidationError validate(Geometry geom) {
        IsValidOp isValidOp = new IsValidOp(geom);
        return isValidOp.getValidationError();
    }

}
