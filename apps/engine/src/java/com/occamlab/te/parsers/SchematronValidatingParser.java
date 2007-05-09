package com.occamlab.te.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.PrintWriter;

import java.lang.ClassLoader;
import java.lang.Thread;

import java.net.URL;
import java.net.URLConnection;

import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.SchemaReaderLoader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.schematron.SchematronProperty;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import com.occamlab.te.ErrorHandlerImpl;

/**
 * Validates the given XML file (document) with the desred schematron file.
 * Used in conjuction with standard XML schema validation to give a better validation coverage.
 *
 * @author jparrpearson
 * @version 1.0
 */
public class SchematronValidatingParser {
    	
    	/** Configuration info for Schematron validation. */
    	private PropertyMapBuilder propMapBuilder = null;
    
        	/** Schema reader for Schematron validation. */
    	private SchemaReader schematronReader = null;
	
	/** Namespace URI for the Schematron assertion language (v 1.5). */
    	public static final String SCHEMATRON_NS_URI = "http://www.ascc.net/xml/schematron";
        
        	/**
        	 * Creates and initializes the schematron reader used in the validation process.
        	 */
	private void initSchematronReader() {
		this.propMapBuilder = new PropertyMapBuilder();
		// set general properties for schematron validation
		SchematronProperty.DIAGNOSE.add(this.propMapBuilder);
		SchemaReaderLoader loader = new SchemaReaderLoader();
		this.schematronReader = loader.createSchemaReader(SCHEMATRON_NS_URI);
		assert null != this.schematronReader : "Unable to create a SchemaReader for this schema language: " + SCHEMATRON_NS_URI;
	}
    
    	/**
    	 * Converts an org.w3c.dom.Document element to an java.io.InputStream.
    	 * 
    	 * @param edoc
    	 *	The org.w3c.dom.Document to be converted
    	 * @return 
    	 *	The InputStream value of the passed doument
    	 */
	public InputStream DocumentToInputStream(org.w3c.dom.Document edoc) throws IOException {
		
		// Create the input and output for use in the transformation
		final org.w3c.dom.Document doc = edoc;
		final PipedOutputStream pos = new PipedOutputStream();
		PipedInputStream pis = new PipedInputStream();
		pis.connect(pos);
	
		(new Thread(new Runnable() {
	
			public void run() {
				// Use the Transformer.transform() method to save the Document to a StreamResult
				try {
					TransformerFactory tFactory = TransformerFactory.newInstance();
				            	Transformer transformer = tFactory.newTransformer();
				            	transformer.setOutputProperty("encoding", "ISO-8859-1");
				            	transformer.setOutputProperty("indent", "yes");
				            	transformer.transform(new DOMSource(doc), new StreamResult(pos));
				}
				catch (Exception _ex) {
					throw new RuntimeException("Failed to tranform org.w3c.dom.Document to PipedOutputStream", _ex);
				}
				finally {
					try {
						pos.close();
					}
					catch (IOException e) {
	
					}
				}
			}
		}, "MyClassName.convert(org.w3c.dom.Document edoc)")).start();
	
		return pis;
	}
    
        	/**
    	 * Checks the given schematron phase for the XML file and returns the validation status.
    	 * 
    	 * @param doc
    	 *	The XML file to validate (Document)
    	 * @param schemaFile
    	 *	The string path to the schematron file to use
    	 * @param phase
    	 *	The string phase name (contained in schematron file)
    	 * @return 
    	 *	Whether there were validation errors or not (boolean)
    	 */
	public boolean checkSchematronRules(Document doc, String schemaFile, String phase) {
		
		// The validation state (true = no validation errors)
		boolean isValid = false;
		
		// Convert doc to InputSource
		InputSource xmlInputSource = null;
		try {
			InputStream inputStream = DocumentToInputStream(doc);
			xmlInputSource = new InputSource(inputStream);
		} catch (IOException e) { e.printStackTrace(); }

		// Load schematron file
		File f  = null;
		InputSource schema = null;
		FileInputStream fileInputStream = null;
		try {
			// Use ClassLoader to load schematron off classpath
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
   			URL url = loader.getResource(schemaFile);
			f = new File(url.getFile());
			//f = new File(schemaFile);
			fileInputStream = new FileInputStream(f);
			schema = new InputSource(fileInputStream);
		} catch (FileNotFoundException e) {
			assert false : "Entity body not found. " + e.toString();
		}
		ValidationDriver driver = createSchematronDriver(phase);
		assert null != driver : "Unable to create Schematron ValidationDriver";
		try {
			// Validate using the thaiopensource validation method
			if (driver.loadSchema(schema)) {
				isValid = driver.validate(xmlInputSource);
			} else {
				assert false : ("Failed to load Schematron schema: " + schemaFile + "\nIs the schema valid? Is the phase defined?");
			}
		} catch (SAXException e) {
			assert false : e.toString();
		} catch (IOException e) {
			assert false : e.toString();
		} finally {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return isValid;
	}

        	/**
    	 * Checks the given schematron phase for the XML file and returns the validation status (takes schematron file, not string location).
    	 * New and ADVANCED! (team engine can't work with overloaded methods :P)
    	 *
    	 * @param doc
    	 *	The XML file to validate (Document)
    	 * @param schemaFile
    	 *	The file object of the schematron file to validate with
    	 * @param phase
    	 *	The string phase name (contained in schematron file)
    	 * @return 
    	 *	Whether there were validation errors or not (boolean)
    	 */
	public boolean checkSchematronRulesAdv(InputSource inputDoc, File schemaFile, String phase) {
		
		// The validation state (true = no validation errors)
		boolean isValid = false;

		// Load schematron file
		ValidationDriver driver = createSchematronDriver(phase);
		assert null != driver : "Unable to create Schematron ValidationDriver";
		InputSource is = null;
		try {
			FileInputStream fis = new FileInputStream(schemaFile);
		 	is = new InputSource(fis);
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
		
		try {
			// Validate using the thaiopensource validation method
			if (driver.loadSchema(is)) {
				isValid = driver.validate(inputDoc);
			} else {
				assert false : ("Failed to load Schematron schema: " + schemaFile + "\nIs the schema valid? Is the phase defined?");
			}
		} catch (SAXException e) {
			assert false : e.toString();
		} catch (IOException e) {
			assert false : e.toString();
		}

		return isValid;
	}

    	/**
    	 * Sets up the schematron reader with all the necessary parameters.
    	 * Calls initSchematronReader() to do further setup of the validation driver.
    	 *
    	 * @param phase
    	 *	The string phase name (contained in schematron file)
    	 * @return 
    	 *	The ValidationDriver to use in validating the XML document
    	 */
	private ValidationDriver createSchematronDriver(String phase) {
		initSchematronReader();
		
		PropertyMapBuilder schematronConfig = this.propMapBuilder;
		// validate using default phase
		SchematronProperty.PHASE.put(schematronConfig, "Default");
		if (null != phase && phase.length() > 0) {
			SchematronProperty.PHASE.put(schematronConfig, phase);
		}
		SchemaReader reader = this.schematronReader;
		ValidationDriver validator = new ValidationDriver(schematronConfig.toPropertyMap(), reader);
		return validator;
	}
	
	
	
	
	
	private static String schemaLocation = null;
	private static String phase = null;
	private static String type = null;

	/** Default constructor required for init */
	public SchematronValidatingParser () throws Exception {
	}

	/** Overloaded constructor required for init */
	public SchematronValidatingParser (Document schema_link) throws Exception {
		String localType = getFileType(schema_link.getDocumentElement());
	}
	
	/**
	 * Parses the parser element to get the schematron file location and type of resource (from ctl file).
	 *
	 * @param schema_links
	 *	Gets the location of the scehma (and type of resource) and saves to global parameter
	 * @ return 
	 *	The type of resource (URL, File, Resource)
	 */
	public String getFileType(Element schema_links) throws Exception {
		Document d = schema_links.getOwnerDocument();
		NodeList nodes = d.getElementsByTagNameNS("http://www.occamlab.com/te/parsers", "schema");
		String localType = null;
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element)nodes.item(i);
			localType = e.getAttribute("type");
			this.type = e.getAttribute("type");
			this.phase = e.getAttribute("phase");
			this.schemaLocation = e.getTextContent();
		}
		return localType;
	}

	/**
	 * Parses the document by first retieving it, then validating, before returning the parsed document (schematron in this case)
	 *
	 * @param uc
    	 *	The URL information (host, port,request, etc) for the response document
    	 * @param instruction
    	 *	A copy of the parser Element
    	 * @param logger
    	 *	The PrintWriter to log any errors to
    	 * @return 
    	 *	The Document response from the URL connection made
	 */
	public Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {

		// Get schematron schema information from ctl file
		String localType = getFileType(instruction);
		
		URL schemaURL = null;
		File schemaFile = null;	
		if (type.equals("url")) {
			schemaURL = new URL(schemaLocation);
		} else if (type.equals("file")) {
			schemaFile = new File(schemaLocation);
		} else if (type.equals("resource")) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			schemaFile = new File(cl.getResource(schemaLocation).getFile());
		} 
			
		// Creates and sets document builder, to recieve response document from request
		String property_name = "javax.xml.parsers.DocumentBuilderFactory";
		String oldprop = System.getProperty(property_name);
		System.setProperty(property_name, "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		if (oldprop == null) {
			System.clearProperty(property_name);
		} else {
			System.setProperty(property_name, oldprop);
		}
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		ErrorHandlerImpl eh = new ErrorHandlerImpl("Parsing", logger);
		db.setErrorHandler(eh);

		// Gets the response document
		Document doc = null;
		try {
			doc = db.parse(uc.getInputStream());
		} catch (Exception e) {
			logger.println(e.getMessage());
		}

		boolean isValid = false;
		if (doc != null) {
			InputSource xmlInputSource = null;
			try {
				InputStream inputStream = DocumentToInputStream(doc);
				xmlInputSource = new InputSource(inputStream);
			} catch (IOException e) { e.printStackTrace(); }
			isValid = checkSchematronRulesAdv(xmlInputSource, schemaFile, this.phase);
		}

		// Displays any validation errors that were caught
		int error_count = eh.getErrorCount();
		int warning_count = eh.getWarningCount();
		if (error_count > 0 || warning_count > 0) {
			String msg = "";
			if (error_count > 0) {
				msg += error_count + " validation error" + (error_count == 1 ? "" : "s");
				if (warning_count > 0) msg += " and ";
			}
			if (warning_count > 0) {
				msg += warning_count + " warning" + (warning_count == 1 ? "" : "s");
			}
			msg += " detected.";
			logger.println(msg);
		}

		boolean b_ignore_errors = false;
		String s_ignore_errors = instruction.getAttribute("ignoreErrors");
		if (s_ignore_errors.length() > 0) b_ignore_errors = Boolean.parseBoolean(s_ignore_errors);
		if (error_count > 0 && !b_ignore_errors) return null;

		boolean b_ignore_warnings = true;
		String s_ignore_warnings = instruction.getAttribute("ignoreWarnings");
		if (s_ignore_warnings.length() > 0) b_ignore_warnings = Boolean.parseBoolean(s_ignore_warnings);
		if (warning_count > 0 && !b_ignore_warnings) return null;

		// Return null if the schematron found an error, else return the valid document
		if (!isValid) {
			return null;
		}
		else {
			return doc;
		}
	}

}