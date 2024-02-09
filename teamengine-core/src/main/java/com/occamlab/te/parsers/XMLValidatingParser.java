/**
 * **************************************************************************
 *
 * Contributor(s):
 *	C. Heazel (WiSC): Added Fortify adjudication changes
 *
 ***************************************************************************
 */
package com.occamlab.te.parsers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLProtocolException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.occamlab.te.ErrorHandlerImpl;
import com.occamlab.te.parsers.xml.CachingSchemaLoader;
import com.occamlab.te.parsers.xml.InMemorySchemaSupplier;
import com.occamlab.te.parsers.xml.XsdSchemaLoader;
import com.occamlab.te.parsers.xml.SchemaSupplier;
import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.URLConnectionUtils;

/**
 * Validates an XML resource against a set of W3C XML Schema or DTD schemas.
 *
 */
public class XMLValidatingParser {

	static TransformerFactory TF = null;
	static DocumentBuilderFactory nonValidatingDBF = null;
	static DocumentBuilderFactory schemaValidatingDBF = null;
	static DocumentBuilderFactory dtdValidatingDBF = null;

	ArrayList<SchemaSupplier> schemaList = new ArrayList<>();

	ArrayList<Object> dtdList = new ArrayList<>();

	/*
	 * For now we create a new cache per instance of XMLValidatingParser, which means a
	 * new cache per test run. These schemas could be cached for a longer period than
	 * that, but then the question because "how long?" Until the web app shuts down? Try
	 * to obey the caching headers in the HTTP responses?
	 *
	 * This solution at least fixes the major performance issue.
	 */
	private final CachingSchemaLoader schemaLoader = new CachingSchemaLoader(new XsdSchemaLoader());

	private static final Logger jlogger = Logger.getLogger("com.occamlab.te.parsers.XMLValidatingParser");

	private List<Object> loadSchemaList(Document schemaLinks, String schemaType) throws Exception {
		NodeList nodes = schemaLinks.getElementsByTagNameNS("http://www.occamlab.com/te/parsers", schemaType);
		if (nodes.getLength() == 0) {
			return Collections.emptyList();
		}
		final ArrayList<Object> schemas = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element) nodes.item(i);
			Object schema = null;
			String type = e.getAttribute("type");
			// URL, File, or Resource
			if (type.equals("url")) {
				schema = new URL(e.getTextContent());
			}
			else if (type.equals("file")) {
				schema = new File(e.getTextContent());
			}
			else if (type.equals("resource")) {
				ClassLoader cl = getClass().getClassLoader();
				String resource = e.getTextContent();
				URL url = cl.getResource(resource);
				if (url == null) {
					String msg = "Can't find schema resource on classpath at " + resource;
					jlogger.warning(msg);
					throw new Exception(msg);
				}
				schema = url;
			}
			else {
				throw new Exception("Unknown schema resource type " + type);
			}
			jlogger.finer("Adding schema reference " + schema.toString());
			schemas.add(schema);
		}
		return schemas;
	}

	private void loadSchemaLists(Node schemaLinks, ArrayList<SchemaSupplier> schemas, ArrayList<Object> dtds)
			throws Exception {
		if (null == schemaLinks) {
			return;
		}
		jlogger.finer("Received schemaLinks\n" + DomUtils.serializeNode(schemaLinks));
		Document configDoc;
		if (schemaLinks instanceof Document) {
			configDoc = (Document) schemaLinks;
		}
		else {
			configDoc = schemaLinks.getOwnerDocument();
		}

		final ArrayList<SchemaSupplier> schemaSuppliers = new ArrayList<>();
		for (final Object schemaObj : loadSchemaList(configDoc, "schema")) {
			schemaSuppliers.add(SchemaSupplier.makeSupplier(schemaObj));
		}
		schemas.addAll(schemaSuppliers);
		dtds.addAll(loadSchemaList(configDoc, "dtd"));

		// If instruction body is an embedded xsd:schema, add it to the
		// ArrayList
		NodeList nodes = configDoc.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element) nodes.item(i);
			CharArrayWriter caw = new CharArrayWriter();
			Transformer t = TF.newTransformer();
			t.transform(new DOMSource(e), new StreamResult(caw));
			schemas.add(new InMemorySchemaSupplier(caw.toCharArray()));
		}
	}

	public XMLValidatingParser() {

		if (nonValidatingDBF == null) {
			String property_name = "javax.xml.parsers.DocumentBuilderFactory";
			String oldprop = System.getProperty(property_name);
			System.setProperty(property_name, "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			nonValidatingDBF = DocumentBuilderFactory.newInstance();
			// Fortify Mod: Disable entity expansion to foil External Entity Injections
			nonValidatingDBF.setExpandEntityReferences(false);
			nonValidatingDBF.setNamespaceAware(true);
			schemaValidatingDBF = DocumentBuilderFactory.newInstance();
			schemaValidatingDBF.setNamespaceAware(true);
			schemaValidatingDBF.setValidating(true);
			schemaValidatingDBF.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
			dtdValidatingDBF = DocumentBuilderFactory.newInstance();
			dtdValidatingDBF.setNamespaceAware(true);
			dtdValidatingDBF.setValidating(true);
			// Fortify Mod: Disable entity expansion to foil External Entity Injections
			dtdValidatingDBF.setExpandEntityReferences(false);
			if (oldprop == null) {
				System.clearProperty(property_name);
			}
			else {
				System.setProperty(property_name, oldprop);
			}
		}

		if (TF == null) {
			// Fortify Mod: prevent external entity injection
			// includes try block to capture exceptions to setFeature.
			TF = TransformerFactory.newInstance();
			try {
				TF.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			}
			catch (Exception e) {
				jlogger.warning("Failed to secure Transformer");
			}
		}
	}

	public XMLValidatingParser(Document schema_links) throws Exception {
		this();
		if (null != schema_links) {
			loadSchemaLists(schema_links, this.schemaList, this.dtdList);
		}
	}

	/**
	 * Attempts to parse a resource read using the given connection to a URL.
	 * @param uc A connection for reading from some URL.
	 * @param instruction An Element node (ctlp:XMLValidatingParser) containing
	 * instructions, usually schema references.
	 * @param logger A log writer.
	 * @return A Document, or null if the resource could not be parsed.
	 * @throws SSLProtocolException
	 */
	public Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws SSLProtocolException {
		if (null == uc) {
			throw new NullPointerException("Unable to parse resource: URLConnection is null.");
		}
		jlogger.fine("Received URLConnection object for " + uc.getURL());
		Document doc = null;
		try (InputStream inStream = URLConnectionUtils.getInputStream(uc)) {
			doc = parse(inStream, instruction, logger);
		}
		catch (SSLProtocolException sslep) {
			throw new SSLProtocolException(
					"[SSL ERROR] Failed to connect with the requested URL due to \"Invalid server_name\" found!! :"
							+ uc.getURL() + ":" + sslep.getClass() + " : " + sslep.getMessage());
		}
		catch (Exception e) {
			throw new RuntimeException(String.format("Failed to parse resource from %s", uc.getURL()), e);
		}
		return doc;
	}

	/**
	 * Parses and validates an XML resource using the given schema references.
	 * @param input The XML input to parse and validate. It must be either an InputStream
	 * or a Document object.
	 * @param parserConfig An Element
	 * ({http://www.occamlab.com/te/parsers}XMLValidatingParser) containing configuration
	 * info. If it is {@code null} or empty validation will be performed by using location
	 * hints in the input document.
	 * @param logger The PrintWriter to log all results to
	 * @return {@code null} If any non-ignorable errors or warnings occurred; otherwise
	 * the resulting Document.
	 *
	 */
	Document parse(Object input, Element parserConfig, PrintWriter logger) throws Exception {
		jlogger.finer("Received XML resource of type " + input.getClass().getName());
		Document resultDoc = null;
		ErrorHandlerImpl errHandler = new ErrorHandlerImpl("Parsing", logger);

		if (input instanceof InputStream) {
			DocumentBuilderFactory dbf = nonValidatingDBF;
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(errHandler);
			try (InputStream xmlInput = (InputStream) input) {
				resultDoc = db.parse(xmlInput);
			}
			catch (Exception e) {
				jlogger.log(Level.INFO, "Error parsing InputStream", e);
			}
		}
		else if (input instanceof Document) {
			resultDoc = (Document) input;
		}
		else {
			throw new IllegalArgumentException("XML input must be an InputStream or a Document object.");
		}
		if (null == resultDoc) {
			throw new RuntimeException("Failed to parse input: " + input.getClass().getName());
		}
		errHandler.setRole("Validation");
		validate(resultDoc, parserConfig, errHandler);
		int error_count = errHandler.getErrorCount();
		int warning_count = errHandler.getWarningCount();
		if (error_count > 0 || warning_count > 0) {
			String msg = "";
			if (error_count > 0) {
				msg += error_count + " validation error" + (error_count == 1 ? "" : "s");
				if (warning_count > 0)
					msg += " and ";
			}
			if (warning_count > 0) {
				msg += warning_count + " warning" + (warning_count == 1 ? "" : "s");
			}
			msg += " detected.";
			logger.println(msg);
		}

		if (error_count > 0) {
			String s = (null != parserConfig) ? parserConfig.getAttribute("ignoreErrors") : "false";
			if (s.length() == 0 || !Boolean.parseBoolean(s)) {
				resultDoc = null;
			}
		}

		if (warning_count > 0) {
			String s = (null != parserConfig) ? parserConfig.getAttribute("ignoreWarnings") : "true";
			if (s.length() > 0 && !Boolean.parseBoolean(s)) {
				resultDoc = null;
			}
		}
		return resultDoc;
	}

	/**
	 * A method to validate a pool of schemas outside of the request element.
	 * @param doc doc The file document to validate
	 * @param instruction instruction The xml encapsulated schema information (file
	 * locations)
	 * @return false if there were errors, true if none.
	 *
	 */
	public boolean checkXMLRules(Document doc, Document instruction) throws Exception {

		if (doc == null || doc.getDocumentElement() == null)
			return false;
		Element e = instruction.getDocumentElement();
		PrintWriter logger = new PrintWriter(System.out);
		Document parsedDoc = parse(doc, e, logger);
		return (parsedDoc != null);
	}

	/**
	 * Validates the given document against the schema references supplied in the
	 * accompanying instruction document.
	 * @param doc The document to be validated.
	 * @param instruction A document containing schema references; may be null, in which
	 * case embedded schema references will be used instead.
	 * @return A list of Element nodes ({@code <error>}) containing error messages.
	 * @throws Exception If any error occurs.
	 */
	public NodeList validate(Document doc, Document instruction) throws Exception {
		return schemaValidation(doc, instruction).toNodeList();
	}

	public Element validateSingleResult(Document doc, Document instruction) throws Exception {
		return schemaValidation(doc, instruction).toRootElement();
	}

	XmlErrorHandler schemaValidation(Document doc, Document instruction) throws Exception {
		if (doc == null || doc.getDocumentElement() == null) {
			throw new NullPointerException("Input document is null.");
		}
		XmlErrorHandler errHandler = new XmlErrorHandler();
		validate(doc, instruction, errHandler);
		return errHandler;
	}

	/**
	 * Validates the given XML {@link Document} per the given instructions, recording
	 * errors in the given error handler.
	 * @param doc must not be null
	 * @param instruction may be null to signify no special instructions
	 * @param errHandler errors will be recorded on this object
	 */
	private void validate(final Document doc, final Node instruction, final ErrorHandler errHandler) throws Exception {
		ArrayList<SchemaSupplier> schemas = new ArrayList<>(schemaList);
		ArrayList<Object> dtds = new ArrayList<>(dtdList);
		loadSchemaLists(instruction, schemas, dtds);
		if (null == doc.getDoctype() && dtds.isEmpty()) {
			validateAgainstXMLSchemaList(doc, schemas, errHandler);
		}
		else {
			validateAgainstDTDList(doc, dtds, errHandler);
		}
	}

	/**
	 * Validates an XML resource against a list of XML Schemas. Validation errors are
	 * reported to the given handler.
	 * @param doc The input Document node.
	 * @param xsdList A list of XML schema references. Must be non-null, but if empty,
	 * validation will be performed by using location hints found in the input document.
	 * @param errHandler An ErrorHandler that collects validation errors.
	 * @throws SAXException If a schema cannot be read for some reason.
	 * @throws IOException If an I/O error occurs.
	 */
	void validateAgainstXMLSchemaList(Document doc, List<SchemaSupplier> xsdList, ErrorHandler errHandler)
			throws SAXException, IOException {
		jlogger
			.fine("Validating XML resource from " + doc.getDocumentURI() + " with these specified schemas: " + xsdList);
		Schema schema;
		if (!xsdList.isEmpty()) {
			schema = schemaLoader.loadSchema(ImmutableList.copyOf(xsdList));
		}
		else {
			schema = schemaLoader.defaultSchema();
		}
		Validator validator = schema.newValidator();
		validator.setErrorHandler(errHandler);
		DOMSource source = new DOMSource(doc, doc.getBaseURI());
		validator.validate(source);
	}

	/**
	 * Validates an XML resource against a list of DTD schemas or as indicated by a
	 * DOCTYPE declaration. Validation errors are reported to the given handler. If no DTD
	 * references are provided the external schema reference in the DOCTYPE declaration is
	 * used (Note: an internal subset is ignored).
	 * @param doc The input Document.
	 * @param dtdList A list of DTD schema references. May be empty but not null.
	 * @param errHandler An ErrorHandler that collects validation errors.
	 * @throws Exception If any errors occur while attempting to validate the document.
	 */
	private void validateAgainstDTDList(Document doc, ArrayList<Object> dtdList, ErrorHandler errHandler)
			throws Exception {
		jlogger.finer("Validating XML resource from " + doc.getDocumentURI());
		DocumentBuilder db = dtdValidatingDBF.newDocumentBuilder();
		db.setErrorHandler(errHandler);
		// Fortify Mod: prevent external entity injection
		// includes try block to capture exceptions to setFeature.
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		}
		catch (Exception e) {
			jlogger.warning("Failed to secure Transformer");
		}
		// End Fortify Mod
		Transformer copier = tf.newTransformer();
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		Result copy = new StreamResult(content);
		if (dtdList.isEmpty()) {
			DocumentType doctype = doc.getDoctype();
			if (null == doctype) {
				return;
			}
			URI systemId = URI.create(doctype.getSystemId());
			if (!systemId.isAbsolute() && null != doc.getBaseURI()) {
				systemId = URI.create(doc.getBaseURI()).resolve(systemId);
			}
			copier.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId.toString());
			copier.transform(new DOMSource(doc), copy);
			db.parse(new ByteArrayInputStream(content.toByteArray()));
		}
		else {
			for (Object dtdRef : dtdList) {
				content.reset();
				copier.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdRef.toString());
				copier.transform(new DOMSource(doc), copy);
				db.parse(new ByteArrayInputStream(content.toByteArray()));
			}
		}
	}

}
