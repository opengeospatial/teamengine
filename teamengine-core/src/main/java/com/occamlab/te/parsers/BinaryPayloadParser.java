/**
 * **************************************************************************
 *
 * Contributor(s):
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */
package com.occamlab.te.parsers;

import java.io.PrintWriter;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.XMLConstants; // Addition for Fortify modifications

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BinaryPayloadParser {

	public Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// Fortify Mod: prevent external entity injection
		dbf.setExpandEntityReferences(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElement("payload");

		// Fortify Mod: prevent external entity injection
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		Transformer t = tf.newTransformer();
		// Transformer t = TransformerFactory.newInstance().newTransformer();

		return doc;
	}

}
