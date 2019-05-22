package com.occamlab.te.parsers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.occamlab.te.parsers.xml.UrlSchemaSupplier;

public class XMLValidatingParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private static DocumentBuilder docBuilder;

	@BeforeClass
	public static void initFixture() throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void createParserWithCswSchemaAsResource() throws Exception {
		Document schemaRefs = parseResourceDocument("/conf/schema-refs.xml");
		final URL expectedResource = getClass().getResource("/xsd/ogc/csw/2.0.2/csw-2.0.2.2.xsd");
		final UrlSchemaSupplier expectedSupplier = new UrlSchemaSupplier(expectedResource);
		XMLValidatingParser iut = new XMLValidatingParser(schemaRefs);
		assertNotNull(iut);
		assertEquals(Arrays.asList(expectedSupplier), iut.schemaList);
	}

	@Test
	public void parseWithSchemaListAndNoLocation() throws Exception {
		Document schemaRefs = parseResourceDocument("/conf/schema-refs-ipo.xml");
		URL url = getClass().getResource("/ipo.xml");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(url.openConnection(),
				schemaRefs.getDocumentElement(), logger);
		assertNotNull(result);
		assertTrue("Unexpected validation error(s) reported.", strWriter
				.toString().isEmpty());
		assertEquals("Document element has unexpected local name", result
				.getDocumentElement().getLocalName(), "purchaseOrder");
	}

	@Test
	public void parseWithSchemaListAndLocation() throws Exception {
		final Document schemaRefs = parseResourceDocument("/conf/multiple-schema-refs.xml");
		final Document doc = parseResourceDocument("/ipo-multipleSchemaRefs.xml");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(doc, schemaRefs.getDocumentElement(), logger);
		assertNotNull(result);
		assertEquals("Unexpected validation error(s) were reported.",
				"", strWriter.toString());
	}

	/**
	 * Tests that an XML document with a schema location is still declared invalid
	 * if {@link XMLValidatingParser} is configured with a different, non-matching
	 * schema.
	 */
	@Test
	public void parseWithSchemaListAndDifferentLocation() throws Exception {
		// This document also tests schema loading from a URL (as opposed to a classpath
		// resource).
		final Document schemaRefs = parseResourceDocument("/conf/schema-ref-log4j.xml");
		final Document doc = parseResourceDocument("/ipo-schemaLoc.xml");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(doc, schemaRefs.getDocumentElement(), logger);
		assertNull(result);
		assertNotEquals("Validation error(s) should be reported.",
				"", strWriter.toString());
	}

	@Test
	public void parsePurchaseOrder_invalid() throws Exception {
		Document schemaRefs = parseResourceDocument("/conf/schema-refs-ipo.xml");
		URL url = getClass().getResource("/ipo-invalid.xml");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(url.openConnection(),
				schemaRefs.getDocumentElement(), logger);
		assertNull(result);
		assertFalse("Expected validation error(s) but none reported.",
				strWriter.toString().isEmpty());
	}

	@Test
	public void parseWithInlineSchemaAndNoSchemaLocation() throws Exception {
		Document schemaRefs = parseResourceDocument("/conf/schema-inline.xml");
		URL url = getClass().getResource("/comment.xml");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(url.openConnection(),
				schemaRefs.getDocumentElement(), logger);
		assertEquals("Unexpected validation error(s) were reported.",
				"", strWriter.toString());
		assertNotNull(result);
	}

	@Test
	public void validateWithNullSchemaListAndNoSchemaLocation()
			throws SAXException, IOException {
		Document doc = parseResourceDocument("/ipo.xml");
		XmlErrorHandler errHandler = new XmlErrorHandler();
		XMLValidatingParser iut = new XMLValidatingParser();
		iut.validateAgainstXMLSchemaList(doc, Collections.emptyList(), errHandler);
		assertFalse("Expected one or more validation errors.",
				errHandler.isEmpty());
	}

	@Test
	public void validateWithNullSchemaListAndSchemaLocation()
			throws SAXException, IOException, URISyntaxException {
		final Document doc = parseResourceDocument("/ipo-schemaLoc.xml");
		XmlErrorHandler errHandler = new XmlErrorHandler();
		XMLValidatingParser iut = new XMLValidatingParser();
		iut.validateAgainstXMLSchemaList(doc, Collections.emptyList(), errHandler);
		List<String> errList = errHandler.toList();
		assertEquals("Unexpected number of validation errors reported.", 0,
				errList.size());
	}

	@Test
	public void parseWithNullInstructionAndMultipleSchemaLocations()
			throws Exception {
		final Document doc = parseResourceDocument("/ipo-multipleSchemaRefs.xml");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(doc, null, logger);
		assertNotNull(result);
		assertTrue("Unexpected validation error(s) were reported.", strWriter
				.toString().isEmpty());
	}

	@Test
	public void parseWithEmptySchemaListAndMultipleSchemaLocations()
			throws Exception {
		final Document schemaRefs = parseResourceDocument("/conf/no-schema-refs.xml");
		final Document doc = parseResourceDocument("/ipo-multipleSchemaRefs.xml");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(doc, schemaRefs.getDocumentElement(),
				logger);
		assertNotNull(result);
		assertTrue("Unexpected validation error(s) were reported.", strWriter
				.toString().isEmpty());
	}

	@Test
	public void validateWithNullDTDList_valid() throws Exception {
		final Document doc = parseResourceDocument("/testng.xml");
		XMLValidatingParser iut = new XMLValidatingParser();
		NodeList errList = iut.validate(doc, null);
		assertEquals("Unexpected number of validation errors reported.", 0,
				errList.getLength());
	}

	/**
	 * Tests validating a DTD-specified document against a different DTD. Should
	 * fail validation.
	 */
	@Test
	public void validateWithMismatchedDTDList() throws Exception {
		final Document doc = parseResourceDocument("/testng.xml");
		final Document dtdInstructions = parseResourceDocument("/conf/dtd-ref-wms.xml");
		XMLValidatingParser iut = new XMLValidatingParser();
		NodeList errList = iut.validate(doc, dtdInstructions);
		assertTrue("should report validation errors", errList.getLength() > 0);
	}

	@Test
	public void validateDocumentWithDoctype_invalid() throws Exception {
		final Document doc = parseResourceDocument("/testng-invalid.xml");
		XMLValidatingParser iut = new XMLValidatingParser();
		NodeList errList = iut.validate(doc, null);
		assertEquals("Unexpected number of validation errors reported.", 2,
				errList.getLength());
	}

	@Test
	public void parseNonXMLResource() throws IOException {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Failed to parse resource");
		URL url = getClass().getResource("/jabberwocky.txt");
		StringWriter strWriter = new StringWriter();
		PrintWriter logger = new PrintWriter(strWriter);
		XMLValidatingParser iut = new XMLValidatingParser();
		Document result = iut.parse(url.openConnection(), null, logger);
		assertNull(result);
	}

	/**
	 * Parses an XML Document from a classpath resource.
	 * 
	 * @param resourcePath relative to the current class
	 * @return never null
	 */
	private Document parseResourceDocument(final String resourcePath)
			throws SAXException, IOException {
		final URL url = getClass().getResource(resourcePath);
		final Document doc = docBuilder.parse(url.toString());
		doc.setDocumentURI(url.toString());
		return doc;
	}
}
