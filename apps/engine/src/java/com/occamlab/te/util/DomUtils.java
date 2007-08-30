package com.occamlab.te.util;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;

/**
 * Allows for manipulating of a DOM Document by adding/removing/etc elements and attributes.
 *
 * @author jparrpearson
 */
public class DomUtils {

    /**
     * Adds the attribute to each node in the Document with the given name.
     *
     * @param doc
     *		the Document to add attributes to
     * @param tagName
     *		the local name of the nodes to add the attribute to
     * @param tagNamespaceURI
     *		the namespace uri of the nodes to add the attribute to
     * @param attrName
     *		the name of the attribute to add
     * @param attrValue
     *		the value of the attribute to add
     *
     * @return the original Document with the update attribute nodes
     */
    public static Node addDomAttr(Document doc, String tagName, String tagNamespaceURI, String attrName, String attrValue) {

	// Create a Document to work on
	Document newDoc = null;
	try {
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		newDoc = db.newDocument();
	} catch (Exception e) {
		e.printStackTrace();
	}

	Transformer identity = null;
	try {
    	    	TransformerFactory TF = TransformerFactory.newInstance();
		identity = TF.newTransformer();
		identity.transform(new DOMSource(doc), new DOMResult(newDoc));
	} catch (Exception ex) {
		System.out.println("ERROR: "+ex.getMessage());
	}

	// Get all named nodes in the doucment
	NodeList namedTags = newDoc.getElementsByTagNameNS(tagNamespaceURI, tagName);
	for (int i = 0; i < namedTags.getLength(); i++) {
		// Add the attribute to each one
		Element element = (Element)namedTags.item(i);
		element.setAttribute(attrName, attrValue);
	}

	//displayNode(newDoc);
	return (Node)newDoc;
    }

    /**
     * Determines if there is a comment Node that contains the given string.
     *
     * @param node
     *		the Node to look in
     * @param str
     *		the string value to match in the comment nodes
     *
       CTL declaration, if we ever want to use it
       <!--Sample Usage: ctl:checkCommentNodes($xml.resp, 'complexContent')-->
       <ctl:function name="ctl:checkCommentNodes">
	  <ctl:param name="node"/>
	  <ctl:param name="string"/>
	  <ctl:description>Checks a Node for comments that contain the given string.</ctl:description>
	  <ctl:java class="com.occamlab.te.util.DomUtils" 
					method="checkCommentNodes"/>
  	</ctl:function>

     * @return the original Document with the update attribute nodes
     */
    public static boolean checkCommentNodes(Node node, String str) {

	// Get nodes of node and go through them
	NodeList children = node.getChildNodes();
	for (int i = 0; i < children.getLength(); i++) {
		Node child = children.item(i);
		NodeList childChildren = child.getChildNodes();
		if (childChildren.getLength() > 0) {
			// Recurse for all children
			boolean okDownThere = checkCommentNodes(child, str);
			if (okDownThere == true) {
				return true;
			}
		}
		// Investigate comments
		if (child.getNodeType() == Node.COMMENT_NODE) {
			// If we got a comment that contains the string we are happy
			Comment comment = (Comment)child;
			if (comment.getNodeValue().contains(str)) {
				return true;
			}
		}
	}
	return false;
    }

    /**
     * Serializes a Node to a String
     */
    public static String serializeNode(Node node) {
        
        try {
	        OutputFormat format = new OutputFormat();
	        StringWriter result = new StringWriter();   
	        XMLSerializer serializer = new XMLSerializer(result, format);         
	        switch (node.getNodeType()) {
		        case Node.DOCUMENT_NODE:               
		        	serializer.serialize((Document) node);
		        	break;
		        case Node.ELEMENT_NODE:
		        	serializer.serialize((Element) node);
		        	break;
		        case Node.DOCUMENT_FRAGMENT_NODE:
		        	serializer.serialize((DocumentFragment) node);
		        	break;
		}
		return result.toString();
        } catch (Exception e) {
        	System.out.println("Error serializing DOM Node.  "+e.getMessage());
        }
        
        return null;
    }


    /** HELPER METHOD TO PRINT A DOM TO STDOUT */
    static public void displayNode(Node node) {
	try {
	    	TransformerFactory TF = TransformerFactory.newInstance();
		Transformer identity = TF.newTransformer();
		identity.transform(new DOMSource(node), new StreamResult(System.out));
	} catch (Exception ex) {
		System.out.println("ERROR: "+ex.getMessage());
	}
    }

}