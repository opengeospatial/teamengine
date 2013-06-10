package com.occamlab.te.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.occamlab.te.ErrorHandlerImpl;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.SchemaReaderLoader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.prop.schematron.SchematronProperty;

/**
 * Validates the given XML resource against the rules specified in a Schematron
 * (v1.5) file. Used in conjunction with standard XML Schema validator to
 * provide more thorough validation coverage.
 * 
 * Diagnostic messages will be included if any are defined.
 * 
 */
public class SchematronValidatingParser {

    private static final Logger LOGR = Logger
            .getLogger(SchematronValidatingParser.class.getName());
    private PropertyMapBuilder configPropBuilder = null;
    private String schemaLocation = null;
    private File schemaFile = null;
    private String phase = null;
    private String type = null;
    private PrintWriter outputLogger = null;

    /** Namespace URI for the Schematron assertion language (v 1.5). */
    public static final String SCHEMATRON_NS_URI = "http://www.ascc.net/xml/schematron";

    /** Default constructor required for init */
    public SchematronValidatingParser() {
    }

    /** Overloaded constructor required for init */
    public SchematronValidatingParser(Document schema_link) throws Exception {
        getFileType(schema_link.getDocumentElement());
    }

    /**
     * Parses the parser element to get the schematron file location and type of
     * resource (from ctl file).
     * 
     * @param schema_links
     *            Gets the location of the schema (and type of resource) and
     *            saves to global parameter
     * @return The type of resource (URL, File, Resource)
     */
    public String getFileType(Element schema_links) throws Exception {
        Document d = schema_links.getOwnerDocument();
        NodeList nodes = d.getElementsByTagNameNS(
                "http://www.occamlab.com/te/parsers", "schema");
        String localType = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            localType = e.getAttribute("type");
            this.type = e.getAttribute("type");
            this.phase = e.getAttribute("phase");
            this.schemaLocation = e.getTextContent().trim();
        }
        return localType;
    }

    /**
     * Converts an org.w3c.dom.Document element to an java.io.InputStream.
     * 
     * @param edoc
     *            The org.w3c.dom.Document to be converted
     * @return The InputStream value of the passed doument
     */
    public InputStream DocumentToInputStream(org.w3c.dom.Document edoc)
            throws IOException {

        final org.w3c.dom.Document doc = edoc;
        final PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream();
        pis.connect(pos);

        (new Thread(new Runnable() {

            public void run() {
                // Use the Transformer.transform() method to save the Document
                // to a StreamResult
                try {
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer();
                    transformer.setOutputProperty("encoding", "ISO-8859-1");
                    transformer.setOutputProperty("indent", "yes");
                    transformer.transform(new DOMSource(doc), new StreamResult(
                            pos));
                } catch (Exception _ex) {
                    throw new RuntimeException(
                            "Failed to tranform org.w3c.dom.Document to PipedOutputStream",
                            _ex);
                } finally {
                    try {
                        pos.close();
                    } catch (IOException e) {

                    }
                }
            }
        }, "MyClassName.convert(org.w3c.dom.Document edoc)")).start();

        return pis;
    }

    /**
     * Checks the given schematron phase for the XML file and returns the
     * validation status.
     * 
     * @param doc
     *            The XML file to validate (Document)
     * @param schemaFile
     *            The string path to the schematron file to use
     * @param phase
     *            The string phase name (contained in schematron file)
     * @return Whether there were validation errors or not (boolean)
     */
    public boolean checkSchematronRules(Document doc, String schemaFile,
            String phase) throws Exception {

        boolean isValid = false;

        if (doc == null || doc.getDocumentElement() == null)
            return isValid;
        try {
            ClassLoader loader = this.getClass().getClassLoader();
            URL url = loader.getResource(schemaFile);
            this.schemaFile = new File(
                    URLDecoder.decode(url.getFile(), "UTF-8"));
        } catch (Exception e) {
            assert false : "Entity body not found. " + e.toString();
        }
        this.phase = phase;
        Document returnDoc = parse(doc, null, null);
        if (returnDoc != null) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Checks the given schematron phase for the XML file and returns the
     * validation status (takes schematron file, not string location). New and
     * ADVANCED! (team engine can't work with overloaded methods :P)
     * 
     * @param doc
     *            The XML file to validate (Document)
     * @param schemaFile
     *            The file object of the schematron file to validate with
     * @param phase
     *            The string phase name (contained in schematron file)
     * @return Whether there were validation errors or not (boolean)
     */
    public boolean checkSchematronRulesAdv(InputSource inputDoc,
            File schemaFile, String phase) throws Exception {

        boolean isValid = false;
        if (inputDoc == null)
            return isValid;
        this.schemaFile = schemaFile;
        this.phase = phase;

        Document returnDoc = parse(inputDoc, null, null);
        if (returnDoc != null) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Runs the schematron file against the input source.
     */
    public boolean executeSchematronDriver(InputSource inputDoc,
            File schemaFile, String phase) {

        boolean isValid = false;
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
            if (driver.loadSchema(is)) {
                isValid = driver.validate(inputDoc);
            } else {
                assert false : ("Failed to load Schematron schema: "
                        + schemaFile + "\nIs the schema valid? Is the phase defined?");
            }
        } catch (SAXException e) {
            assert false : e.toString();
        } catch (IOException e) {
            assert false : e.toString();
        }
        return isValid;
    }

    /**
     * Sets up the schematron reader with all the necessary parameters. Calls
     * initSchematronReader() to do further setup of the validation driver.
     * 
     * @param phase
     *            The string phase name (contained in schematron file)
     * @return The ValidationDriver to use in validating the XML document
     */
    ValidationDriver createSchematronDriver(String phase) {
        SchemaReaderLoader loader = new SchemaReaderLoader();
        SchemaReader schReader = loader.createSchemaReader(SCHEMATRON_NS_URI);
        this.configPropBuilder = new PropertyMapBuilder();
        SchematronProperty.DIAGNOSE.add(this.configPropBuilder);

        if (this.outputLogger == null) {
            this.outputLogger = new PrintWriter(System.out);
        }
        if (null != phase && !phase.isEmpty()) {
            this.configPropBuilder.put(SchematronProperty.PHASE, phase);
        }
        ErrorHandler eh = new ErrorHandlerImpl("Schematron", outputLogger);
        this.configPropBuilder.put(ValidateProperty.ERROR_HANDLER, eh);
        ValidationDriver validator = new ValidationDriver(
                this.configPropBuilder.toPropertyMap(), schReader);
        return validator;
    }

    /**
     * Parses and validates a resource obtained by dereferencing a URI.
     * 
     * @param uc
     *            A URLConnection to access an XML resource.
     * @param instruction
     *            An element containing parser instructions.
     * @param logger
     *            The PrintWriter used for logging errors.
     * @return A Document node if parsing succeeds, or {@code null} if it fails.
     * @throws Exception
     */
    public Document parse(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {
        return parse(uc.getInputStream(), instruction, logger);
    }

    Document parse(InputStream is, Element instruction, PrintWriter logger)
            throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = null;
        try {
            doc = db.parse(is);
        } catch (Exception e) {
            logger.println(e.getMessage());
        }
        return parse(doc, instruction, logger);
    }

    Document parse(InputSource is, Element instruction, PrintWriter logger)
            throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = null;
        try {
            doc = db.parse(is);
        } catch (Exception e) {
            logger.println(e.getMessage());
        }
        return parse(doc, instruction, logger);
    }

    /**
     * Checks the given Document against a Schematron schema. A schema reference
     * is conveyed by a DOM Element node as indicated below.
     * 
     * <pre>
     * {@code
     * <tep:schemas xmlns:tep="http://www.occamlab.com/te/parsers">
     *   <tep:schema type="resource" phase="#ALL">/class/path/schema1.sch</tep:schema>
     * </tep:schemas>
     * }
     * </pre>
     * 
     * @param doc
     *            The document to be validated.
     * @param instruction
     *            An Element containing schema information.
     * @param logger
     *            A Writer used for logging error messages.
     * @return The valid document, or {@code null} if any errors were detected.
     * @throws Exception
     */
    Document parse(Document doc, Element instruction, PrintWriter logger)
            throws Exception {
        this.outputLogger = logger;
        if (instruction != null) {
            getFileType(instruction);
            if (type.equals("url")) {
                URL schemaURL = new URL(this.schemaLocation);
                this.schemaFile = new File(schemaURL.toURI());
            } else if (type.equals("file")) {
                this.schemaFile = new File(this.schemaLocation);
            } else if (type.equals("resource")) {
                URL url = this.getClass().getResource(this.schemaLocation);
                this.schemaFile = new File(URLDecoder.decode(url.getFile(),
                        "UTF-8"));
            }
        }
        boolean isValid = false;
        if (doc != null) {
            InputSource xmlInputSource = null;
            try {
                InputStream inputStream = DocumentToInputStream(doc);
                xmlInputSource = new InputSource(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            isValid = executeSchematronDriver(xmlInputSource, this.schemaFile,
                    this.phase);
        }
        if (!isValid) {
            return null;
        } else {
            return doc;
        }
    }

    /**
     * Checks the content of an XML entity against the applicable rules defined
     * in a Schematron schema. The designated phase identifies the active
     * patterns (rule sets); if not specified, the default phase is executed.
     * 
     * @param xmlEntity
     *            A DOM Document representing the XML entity to validate.
     * @param schemaRef
     *            A (classpath) reference to a Schematron 1.5 schema.
     * @param phase
     *            The phase to execute.
     * @return A NodeList containing validation errors (it may be empty).
     */
    public NodeList validate(Document xmlEntity, String schemaRef, String phase) {

        if (xmlEntity == null || xmlEntity.getDocumentElement() == null)
            throw new IllegalArgumentException("No XML entity supplied (null).");
        InputSource xmlInputSource = null;
        try {
            InputStream inputStream = DocumentToInputStream(xmlEntity);
            xmlInputSource = new InputSource(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PropertyMapBuilder builder = new PropertyMapBuilder();
        SchematronProperty.DIAGNOSE.add(builder);
        if (null != phase && !phase.isEmpty()) {
            builder.put(SchematronProperty.PHASE, phase);
        }
        XmlErrorHandler errHandler = new XmlErrorHandler();
        builder.put(ValidateProperty.ERROR_HANDLER, errHandler);
        ValidationDriver driver = createDriver(builder.toPropertyMap());
        InputStream schStream = this.getClass().getResourceAsStream(schemaRef);
        try {
            InputSource input = new InputSource(schStream);
            try {
                boolean loaded = driver.loadSchema(input);
                if (!loaded) {
                    throw new Exception("Failed to load schema at " + schemaRef
                            + "\nIs the schema valid? Is the phase defined?");
                }
            } finally {
                schStream.close();
            }
            driver.validate(xmlInputSource);
        } catch (Exception e) {
            throw new RuntimeException("Schematron validation failed.", e);
        }
        NodeList errList = errHandler.toNodeList();
        if (LOGR.isLoggable(Level.FINER)) {
            LOGR.finer(String.format(
                    "Found %d Schematron rule violation(s):\n %s",
                    errList.getLength(), errHandler.toString()));
        }
        return errList;
    }

    /**
     * Creates and initializes a ValidationDriver to perform Schematron
     * validation. A schema must be loaded before an instance can be validated.
     * 
     * @param configProps
     *            A PropertyMap containing properties to configure schema
     *            construction and validation behavior; it typically includes
     *            {@code SchematronProperty} and {@code ValidationProperty}
     *            items.
     * @return A ValidationDriver that is ready to load a Schematron schema.
     */
    ValidationDriver createDriver(PropertyMap configProps) {
        SchemaReaderLoader loader = new SchemaReaderLoader();
        SchemaReader schReader = loader.createSchemaReader(SCHEMATRON_NS_URI);
        ValidationDriver validator = new ValidationDriver(configProps,
                schReader);
        return validator;
    }

}
