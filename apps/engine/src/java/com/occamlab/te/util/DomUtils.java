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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
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