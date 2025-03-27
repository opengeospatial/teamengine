/**
 * **************************************************************************
 *
 * Contributor(s):
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */
package com.occamlab.te.parsers;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
