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

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.occamlab.te.Test;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.URLConnectionUtils;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class XSLTransformationParser {

	private static final Logger LOGR = Logger.getLogger(XSLTransformationParser.class.getName());

	DocumentBuilder db = null;

	TransformerFactory tf = null;

	Templates defaultTemplates = null;

	HashMap<String, String> defaultProperties = null;

	HashMap<String, String> defaultParams = null;

	Boolean defaultIgnoreErrors;

	Boolean defaultIgnoreWarnings;

	public XSLTransformationParser() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		db = dbf.newDocumentBuilder();
		tf = TransformerFactory.newInstance();
		defaultProperties = new HashMap<>();
		defaultParams = new HashMap<>();
		defaultIgnoreErrors = FALSE;
		defaultIgnoreWarnings = TRUE;
	}

	public XSLTransformationParser(Node node) throws Exception {
		super();
		defaultTemplates = parseInstruction(DomUtils.getElement(node), defaultProperties, defaultParams,
				defaultIgnoreErrors, defaultIgnoreWarnings);
	}

	public XSLTransformationParser(String reftype, String ref) throws Exception {
		super();
		defaultTemplates = tf.newTemplates(getSource(reftype, ref));
	}

	private Source getSource(String reftype, String ref) throws Exception {
		if (reftype.equals("url")) {
			URL url = new URL(ref);
			return new StreamSource(url.openStream());
		}
		else if (reftype.equals("file")) {
			return new StreamSource(ref);
		}
		else if (reftype.equals("resource")) {
			ClassLoader cl = getClass().getClassLoader();
			return new StreamSource(cl.getResourceAsStream(ref));
		}
		return null;
	}

	private Templates parseInstruction(Element instruction, HashMap<String, String> properties,
			HashMap<String, String> params, Boolean ignoreErrors, Boolean ignoreWarnings) throws Exception {
		Templates templates = null;
		final String[] atts = { "url", "file", "resource" };
		for (String att : atts) {
			String val = instruction.getAttribute(att);
			if (val.length() > 0) {
				templates = tf.newTemplates(getSource(att, val));
				break;
			}
		}
		Element stylesheet = DomUtils.getElementByTagNameNS(instruction, Test.XSL_NS, "stylesheet");
		if (stylesheet == null) {
			stylesheet = DomUtils.getElementByTagNameNS(instruction, Test.XSL_NS, "transform");
		}
		if (stylesheet != null) {
			templates = tf.newTemplates(new DOMSource(stylesheet));
		}
		List<Element> children = DomUtils.getChildElements(instruction);
		for (Element e : children) {
			if (e.getLocalName().equals("property") && e.getNamespaceURI().equals(Test.CTLP_NS)) {
				properties.put(e.getAttribute("name"), e.getTextContent());
			}
			if (e.getLocalName().equals("with-param") && e.getNamespaceURI().equals(Test.CTLP_NS)) {
				params.put(e.getAttribute("name"), e.getTextContent());
			}
		}
		String ignoreErrorsAtt = instruction.getAttribute("ignoreErrors");
		if (ignoreErrorsAtt != null) {
			ignoreErrors = Boolean.parseBoolean(ignoreErrorsAtt);
		}
		String ignoreWarningsAtt = instruction.getAttribute("ignoreWarnings");
		if (ignoreWarningsAtt != null) {
			ignoreWarnings = Boolean.parseBoolean(ignoreWarningsAtt);
		}
		return templates;
	}

	public Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
		HashMap<String, String> properties = new HashMap<>(defaultProperties);
		HashMap<String, String> params = new HashMap<>(defaultParams);
		Boolean ignoreErrors = defaultIgnoreErrors;
		Boolean ignoreWarnings = defaultIgnoreWarnings;
		Templates templates = parseInstruction(instruction, properties, params, ignoreErrors, ignoreWarnings);
		Transformer t = null;
		if (templates != null) {
			t = templates.newTransformer();
		}
		else if (defaultTemplates != null) {
			t = defaultTemplates.newTransformer();
		}
		else {
			t = tf.newTransformer();
		}
		for (Entry<String, String> prop : properties.entrySet()) {
			t.setOutputProperty(prop.getKey(), prop.getValue());
		}
		for (Entry<String, String> param : params.entrySet()) {
			t.setParameter(param.getKey(), param.getValue());
		}
		XSLTransformationErrorHandler el = new XSLTransformationErrorHandler(logger, ignoreErrors, ignoreWarnings);
		t.setErrorListener(el);
		Document doc = db.newDocument();
		InputStream is = null;
		try {
			if (LOGR.isLoggable(Level.FINER)) {
				String msg = String.format("Attempting to transform source from %s using instruction set:%n %s",
						uc.getURL(), DomUtils.serializeNode(instruction));
				LOGR.finer(msg);
			}
			// may return error stream
			is = URLConnectionUtils.getInputStream(uc);
			t.transform(new StreamSource(is), new DOMResult(doc));
		}
		catch (TransformerException e) {
			el.error(e);
		}
		finally {
			if (null != is)
				is.close();
		}
		if (el.getErrorCount() > 0 && !ignoreErrors) {
			return null;
		}
		if (el.getWarningCount() > 0 && !ignoreWarnings) {
			return null;
		}
		return doc;
	}

}
