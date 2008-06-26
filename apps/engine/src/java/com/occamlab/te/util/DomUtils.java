package com.occamlab.te.util;

import java.io.StringWriter;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.OutputKeys;
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
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	try {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();

		DOMSource src = new DOMSource(node);
		StreamResult dest = new StreamResult(baos);
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(src, dest);
	} catch (Exception e) {
		System.out.println("Error serializing node.  "+e.getMessage());
	}

	return baos.toString();
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

    static public Element getElement(Node node) {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            return ((Document)node).getDocumentElement();
        } else if (node.getNodeType() == Node.ELEMENT_NODE){
            return (Element)node;
        }
        return null;
    }

    static public Document createDocument(Node node) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();
        if (node != null) {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(node), new DOMResult(doc));
        }
        return doc;
    }
}