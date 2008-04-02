/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date
 */

package com.occamlab.te;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.*;
import java.lang.ClassLoader;
import java.net.URLDecoder;

import net.sf.saxon.FeatureKeys;
import org.w3c.dom.*;
import java.util.*;
import javax.xml.validation.*;
import javax.xml.XMLConstants;

/**
 * The main test driver for a given executable test suite.
 * 
 */
public class Test {
    public static final int TEST_MODE = 0;

    public static final int RETEST_MODE = 1;

    public static final int RESUME_MODE = 2;

    public static final int RECOVER_MODE = 3;

    public static final int DOC_MODE = 4;

    public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";

    public static final String TE_NS = "java:com.occamlab.te.TECore";

    public static final String CTL_NS = "http://www.occamlab.com/ctl";

    ClassLoader CL;

    DocumentBuilderFactory DBF;

    DocumentBuilder DB;

    TransformerFactory TF;

    Templates executableTestSuite;

    private Logger appLogger;

    private TestDriverConfig driverConfig;

    /**
     * Initializes the main test driver.
     * 
     * @param driverConfig
     *            test driver configuration.
     */
    public Test(TestDriverConfig driverConfig) throws Exception {
        this.driverConfig = driverConfig;
        initAppLogger(driverConfig.getSessionDir());
        appLogger.log(Level.INFO, "Initializing main test driver");
        appLogger.entering(this.getClass().getName(), "ctor",
                new Object[] { driverConfig });

        File logDir = driverConfig.getLogDir();
        if (logDir != null) {
            appLogger.log(Level.INFO, "Using logdir " + logDir.getAbsolutePath());
        }

        // configure parser to resolve XIncludes
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");
        DBF = DocumentBuilderFactory.newInstance();
        DBF.setNamespaceAware(true);
        DBF.setFeature(
                "http://apache.org/xml/features/xinclude/fixup-base-uris",
                false);
        DB = DBF.newDocumentBuilder();

        // create CTL validator
        SchemaFactory sf = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema ctl_schema = sf
                .newSchema(getResourceAsFile("com/occamlab/te/schemas/ctl.xsd"));
        Validator ctl_validator = ctl_schema.newValidator();
        CtlErrorHandler validation_eh = new CtlErrorHandler();
        ctl_validator.setErrorHandler(validation_eh);

        // create transformer to generate executable script from CTL sources
        TF = TransformerFactory.newInstance();
        TF.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
        TF.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
        CL = Thread.currentThread().getContextClassLoader();
        Transformer identityTransformer = TF.newTransformer();
        identityTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        File generatorStylesheet;
        String extension;
        // Transform the test file into a document (dxsl) or for actually
        // running (txsl)
        if (driverConfig.getMode() == DOC_MODE) {
            generatorStylesheet = getResourceAsFile("com/occamlab/te/generate_dxsl.xsl");
            extension = "dxsl";
        } else {
            generatorStylesheet = getResourceAsFile("com/occamlab/te/compile.xsl");
            extension = "txsl";
        }
        // Prepare the XSLT transformers
        Transformer generator = TF.newTransformer(new StreamSource(
                generatorStylesheet));
        InputStream main_is = CL
                .getResourceAsStream("com/occamlab/te/main.xsl");
        Transformer composer = TF.newTransformer(new StreamSource(main_is));

        char[] script_chars = null; // container for final stylesheet module
        Document scriptDoc = DB.newDocument();
        Element scriptElem = scriptDoc.createElement("script");
        scriptDoc.appendChild(scriptElem);

        // Goes through each test source file and compiles it together to run
        // later (txsl file)
        Iterator it = driverConfig.getSources().iterator();
        while (it.hasNext()) {
            File sourcefile = (File) it.next();
            this.appLogger.log(Level.INFO, "Processing source(s) at: "
                    + sourcefile.getAbsolutePath());
            Document generatedDoc = DB.newDocument();
            DocumentBuilder inputDB = DBF.newDocumentBuilder();
            Document inputCtl = null;
            generator.clearParameters();
            composer.clearParameters();
            // Get all test source files in a directory
            if (sourcefile.isDirectory()) {
                Element transformElem = generatedDoc.createElementNS(XSL_NS,
                        "xsl:transform");
                transformElem.setAttribute("version", "2.0");
                generatedDoc.appendChild(transformElem);
                String[] children = sourcefile.list();
                for (int i = 0; i < children.length; i++) {
                    // Finds all .ctl and .xml files in the directory to use
                    if (children[i].toLowerCase().endsWith(".ctl")
                            || children[i].toLowerCase().endsWith(".xml")) {
                        File ctl_file = new File(sourcefile, children[i]);
                        if (ctl_file.isFile()) {
                            File txsl_file = new File(sourcefile, children[i]
                                    .substring(0, children[i].length() - 3)
                                    + extension);
                            boolean needs_compiling;
                            if (txsl_file.exists()) {
                                // regenerate if existing output file is
                                // obsolete
                                needs_compiling = txsl_file.lastModified() < ctl_file
                                        .lastModified()
                                        || txsl_file.lastModified() < generatorStylesheet
                                                .lastModified();
                            } else {
                                needs_compiling = true;
                            }
                            if (needs_compiling) {
                                try {
                                    int old_count = validation_eh
                                            .getErrorCount();
                                    if (driverConfig.hasValidationFlag())
                                        ctl_validator
                                                .validate(new StreamSource(
                                                        ctl_file));
                                    if (validation_eh.getErrorCount() == old_count) {
                                        generator.setParameter("filename",
                                                ctl_file.getAbsolutePath());
                                        generator.setParameter("txsl_filename",
                                                txsl_file.toURL().toString());
                                        inputCtl = inputDB.parse(ctl_file);
                                        this.appLogger
                                                .fine("Stage 1 (generator) transformation parameters:"
                                                        + "\n filename (source): "
                                                        + ctl_file
                                                                .getAbsolutePath()
                                                        + "\n txsl_filename (result): "
                                                        + txsl_file
                                                                .getAbsolutePath());
                                        generator.transform(new DOMSource(
                                                inputCtl), new StreamResult(
                                                txsl_file));
                                    }
                                } catch (org.xml.sax.SAXException e) {
                                    appLogger.severe(e.getMessage());
                                    throw e;
                                } catch (TransformerException e) {
                                    appLogger.severe(e.getMessageAndLocation());
                                    throw e;
                                } finally {
                                    generator.reset();
                                }
                            }
                            Element include = generatedDoc.createElementNS(
                                    XSL_NS, "xsl:include");
                            include.setAttribute("href", txsl_file.toURL()
                                    .toString());
                            transformElem.appendChild(include);
                        }
                    }
                }
            }

            else { // process CTL file
                try {
                    int old_count = validation_eh.getErrorCount();
                    if (driverConfig.hasValidationFlag())
                        ctl_validator.validate(new StreamSource(sourcefile));
                    if (validation_eh.getErrorCount() == old_count) {
                        generator.setParameter("filename", sourcefile
                                .getAbsolutePath());
                        generator.setParameter("txsl_filename", sourcefile
                                .toURL().toString());
                        inputCtl = inputDB.parse(sourcefile);
                        generator.transform(new DOMSource(inputCtl),
                                new DOMResult(generatedDoc));
                    }
                } catch (org.xml.sax.SAXException e) {
                    appLogger.severe(e.getMessage());
                    throw e;
                } catch (TransformerException e) {
                    appLogger.severe(e.getMessageAndLocation());
                    throw e;
                } finally {
                    generator.reset();
                }
            }

            if (script_chars != null) { // when processing second and subsequent
                // sources
                CharArrayReader car = new CharArrayReader(script_chars);
                identityTransformer.transform(new StreamSource(car),
                        new DOMResult(scriptElem));
                if (this.appLogger.isLoggable(Level.FINE)) {
                    this.appLogger
                            .fine("Saving previously generated output (script_chars) to scriptDoc/script");
                    writeNodeToLog(this.appLogger, scriptElem);
                }
                composer.setParameter("prev", scriptElem);
            }
            CharArrayWriter caw = new CharArrayWriter();
            composer.transform(new DOMSource(generatedDoc), new StreamResult(
                    caw));
            script_chars = caw.toCharArray();
            if (this.appLogger.isLoggable(Level.FINE)) {
                this.appLogger.fine("Content of script_chars variable:\n"
                        + new String(script_chars));
            }

        }

        int error_count = validation_eh.getErrorCount();
        if (error_count > 0) {
            String msg = error_count + " validation error"
                    + (error_count == 1 ? "" : "s");
            int warning_count = validation_eh.getWarningCount();
            if (warning_count > 0) {
                msg += " and " + warning_count + " warning"
                        + (warning_count == 1 ? "" : "s");
            }
            msg += " detected.";
            appLogger.severe(msg);
            throw new Exception(msg);
        }

        if (this.appLogger.isLoggable(Level.INFO)) {
            writeFinalStylesheetToFile(script_chars, driverConfig
                    .getSessionDir());
        }

        // Create resusable templates
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
            tf.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
            tf.setErrorListener(new TeErrorListener(script_chars));
            executableTestSuite = tf.newTemplates(new StreamSource(
                    new CharArrayReader(script_chars)));
        } catch (TransformerException e) {
            appLogger.severe(e.getMessageAndLocation());
            throw new Exception("Unable to create final templates.");
        }
    }

    public TestDriverConfig getDriverConfig() {
        return driverConfig;
    }

    // Deletes a directory and its contents
    public static void deleteDir(File dir) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            File f = new File(dir, children[i]);
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }

    // Deletes just the sub directories for a certain directory
    public static void deleteSubDirs(File dir) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            File f = new File(dir, children[i]);
            if (f.isDirectory()) {
                deleteDir(f);
            }
        }
    }

    // Loads a file into memory from the classpath
    public static File getResourceAsFile(String resource) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return new File(URLDecoder.decode(cl.getResource(resource).getFile(),
                "UTF-8"));
    }

    // Loads a DOM Document from the classpath
    Document getResourceAsDoc(String resource) throws Exception {
        InputStream is = CL.getResourceAsStream(resource);
        if (is != null) {
            Transformer t = TF.newTransformer();
            Document doc = DB.newDocument();
            t.transform(new StreamSource(is), new DOMResult(doc));
            return doc;
        } else {
            return null;
        }
    }

    // Get information on a specific test and return the information in a DOM
    // Document
    Document getTemplateFromLog(File logdir, String callpath) throws Exception {
        Document logdoc;
        logdoc = TECore.read_log(logdir.getAbsolutePath(), callpath);
        Element starttest = (Element) logdoc.getElementsByTagName("starttest")
                .item(0);
        String prefix = starttest.getAttribute("prefix");
        String namespace = starttest.getAttribute("namespace-uri");
        String local_name = starttest.getAttribute("local-name");
        Document doc = DB.newDocument();
        Element e = doc.createElementNS(namespace, prefix + ":" + local_name);
        doc.appendChild(e);
        return doc;
    }

    // Main test method
    public void test(List tests, TECore core) throws Exception {
        String sessionId = this.driverConfig.getSessionId();
        File logDir = this.driverConfig.getLogDir();
        File sessionDir = this.driverConfig.getSessionDir();

        core.setSessionId(sessionId);
        core.setSessionDir(sessionDir.getAbsolutePath());

        if (driverConfig.getMode() == TEST_MODE) {
            File f = new File(sessionDir, "log.xml");
            if (f.exists()) {
                f.delete();
            }
            deleteSubDirs(sessionDir);
        }

        // Prepare suite
        Map<String, Document> templates = new HashMap<String, Document>();
        if (tests.isEmpty()) {
            if (driverConfig.getMode() == RETEST_MODE) {
                // ToDo: Find failed tests
            } else if (driverConfig.getMode() == TEST_MODE) {
                Document doc = DB.newDocument();
                String namespace = null;
                String simple_name = "suite";
                String suiteName = driverConfig.getSuiteName();
                if (suiteName != null) {
                    int i = suiteName.lastIndexOf(",");
                    if (i > 0) {
                        namespace = suiteName.substring(0, i);
                        simple_name = suiteName.substring(i + 1);
                    } else {
                        simple_name = suiteName.replaceFirst(":", "-");
                    }
                }
                Element e;
                if (namespace == null) {
                    e = doc.createElement(simple_name);
                } else {
                    e = doc.createElementNS(namespace, simple_name);
                }
                doc.appendChild(e);
                templates.put(sessionId, doc);

            } else if (driverConfig.getMode() == RESUME_MODE) {
                File testLog = new File(sessionDir, "log.xml");
                if (testLog.exists()) {
                    templates.put(sessionId, getTemplateFromLog(logDir,
                            sessionId));
                } else {
                    this.appLogger.warning("Unable to find test log "
                            + testLog.getAbsolutePath());
                    return;
                }
            }
        } else {
            Iterator it = tests.iterator();
            while (it.hasNext()) {
                String path = it.next().toString();
                templates.put(path, getTemplateFromLog(logDir, path));
            }
        }

        // Run each test and log the results
        Transformer t = executableTestSuite.newTransformer();
        Iterator it = templates.keySet().iterator();
        while (it.hasNext()) {
            String path = (String) it.next();
            if (driverConfig.getMode() == RETEST_MODE) {
                File f = new File(logDir, path);
                deleteSubDirs(f);
            }
            Document doc = (Document) templates.get(path);
            t.clearParameters();
            if (logDir != null) {
                t.setParameter("{" + TE_NS + "}logdir", logDir
                        .getCanonicalPath());
            }
            t.setParameter("{" + TE_NS + "}mode", Integer.toString(driverConfig
                    .getMode()));
            t.setParameter("{" + TE_NS + "}starting-test-path", path);
            t.setParameter("{" + TE_NS + "}core", core);
            boolean done = false;
            while (!done) {
                try {
                    t.transform(new DOMSource(doc), new StreamResult(
                            new ByteArrayOutputStream(16)));
                    done = true;
                } catch (TransformerException e) {
                    PrintWriter logger = core.getLogger();
                    boolean root = true;
                    if (logger != null) {
                        logger.println("<exception><![CDATA["
                                + e.getMessageAndLocation() + "]]></exception>");
                        logger.println("<endtest result=\"3\"/>");
                        core.close_log();
                        while (core.getLogger() != null) {
                            root = false;
                            core.close_log();
                        }
                    }
                    this.appLogger.warning(e.getMessageAndLocation());
                    if (root) {
                        done = true;
                    } else {
                        System.out.println("Recovering...");
                        t.setParameter("{" + TE_NS + "}mode", Integer.toString(RECOVER_MODE));
                    }
                }
            }
        }
    }

    private void writeNodeToLog(Logger logger, Node node) {
        StringWriter strWriter = new StringWriter();
        Transformer identity = null;
        try {
            identity = TF.newTransformer();
            identity
                    .transform(new DOMSource(node), new StreamResult(strWriter));
        } catch (TransformerConfigurationException ex) {
            logger.fine(ex.toString());
        } catch (TransformerException ex) {
            logger.fine(ex.toString());
        }
        logger.fine(strWriter.toString());
        return;
    }

    /**
     * Writes the final executable stylesheet module to a file (ets.xsl) in the
     * given test log directory.
     * 
     * @param chars
     *            a char array containing an XSLT stylesheet module.
     * @param logDir
     *            a File representing the location of the log directory.
     */
    private void writeFinalStylesheetToFile(char[] chars, File logDir) {
        File etsFile = new File(logDir, "ets.xsl");
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(etsFile), "UTF-8"));
            out.write(chars);
        } catch (IOException ex) {
            this.appLogger.log(Level.INFO, ex.getMessage(), ex);
        } finally {
            try {
                if (null != out)
                    out.close();
            } catch (IOException ex) {
                this.appLogger.log(Level.INFO, ex.getMessage(), ex);
            }
        }
        return;
    }

    /**
     * Initializes the main application logger. A log file (te.log) is created
     * in the test session directory.
     * 
     * @param sessionDir
     *            a File representing the location of test session directory.
     */
    private void initAppLogger(File sessionDir) {
        this.appLogger = Logger.getLogger(this.getClass().getName());
        File logFile = new File(sessionDir, "teamengine.log");
        StreamHandler streamHandler = null;
        try {
            streamHandler = new StreamHandler(new FileOutputStream(logFile),
                    new SimpleFormatter());
            streamHandler.setEncoding("UTF-8");
        } catch (FileNotFoundException ex) {
            this.appLogger.log(Level.WARNING, ex.getMessage(), ex);
        } catch (UnsupportedEncodingException ex) {
            this.appLogger.log(Level.WARNING, ex.getMessage(), ex);
        } catch (SecurityException ex) {
            this.appLogger.log(Level.WARNING, ex.getMessage(), ex);
        }

        if (null != streamHandler) {
            this.appLogger.addHandler(streamHandler);
        }
        return;
    }

    public static void main(String[] args) throws Exception {
        int mode = TEST_MODE;
        boolean validate = true;
        File logDir = null;
        String sessionId = null;
        String suiteName = null;
        ArrayList<File> sources = new ArrayList<File>();
        ArrayList<String> tests = new ArrayList<String>();
        String cmd = "java com.occamlab.te.Test";

        File f = getResourceAsFile("com/occamlab/te/compile.xsl");
        sources.add(new File(f.getParentFile(), "scripts"));

        // Parse arguments from command-line
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-cmd=")) {
                cmd = args[i].substring(5);
            } else if (args[i].startsWith("-source=")) {
                boolean exists = new File(args[i].substring(8)).exists();
                File sourceFile = exists ? new File(args[i].substring(8))
                        : getResourceAsFile(args[i].substring(8));
                sources.add(sourceFile);
            } else if (args[i].startsWith("-package=")) {
                boolean exists = new File(args[i].substring(9)).exists();
                File packagefile = exists ? new File(args[i].substring(9))
                        : getResourceAsFile(args[i].substring(9));
                sources.add(packagefile);
            } else if (args[i].startsWith("-sourcedir=")) {
                boolean exists = new File(args[i].substring(11)).exists();
                File sourcedir = exists ? new File(args[i].substring(11))
                        : getResourceAsFile(args[i].substring(11));
                sources.add(sourcedir);
            } else if (args[i].startsWith("-logdir=")) {
                logDir = new File(args[i].substring(8));
            } else if (args[i].startsWith("-session=")) {
                sessionId = args[i].substring(9);
            } else if (args[i].startsWith("-suite=")) {
                suiteName = args[i].substring(7);
            } else if (args[i].equals("-mode=test")) {
                mode = TEST_MODE;
            } else if (args[i].equals("-mode=retest")) {
                mode = RETEST_MODE;
            } else if (args[i].equals("-mode=resume")) {
                mode = RESUME_MODE;
            } else if (args[i].equals("-mode=doc")) {
                mode = DOC_MODE;
            } else if (args[i].startsWith("-mode=")) {
                System.out.println("Error: Invalid mode.");
                return;
            } else if (args[i].equals("-validate=no")) {
                validate = false;
            } else if (!args[i].startsWith("-")) {
                if (mode == TEST_MODE) {
                    suiteName = args[i];
                } else if (mode == RETEST_MODE) {
                    tests.add(args[i]);
                }
            }
        }

        if (sources.size() == 1) {
            System.out.println();
            System.out.println("Test mode:");
            System.out.println("  Use to start a test session.\n");
            System.out
                    .println("  "
                            + cmd
                            + " [-mode=test] -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out
                    .println("    [-suite=[{namespace_uri,|prefix:}]suite_name] [-logdir=dir] [-session=session]\n");
            System.out.println("Resume mode:");
            System.out
                    .println("  Use to resume a test session that was interrupted before completion.\n");
            System.out
                    .println("  "
                            + cmd
                            + " -mode=resume -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out.println("    -logdir=dir -session=session\n");
            System.out.println("Retest mode:");
            System.out.println("  Use to reexecute individual tests.\n");
            System.out
                    .println("  "
                            + cmd
                            + " -mode=retest -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out.println("    -logdir=dir test1 [test2] ...\n");
            System.out.println("Doc mode:");
            System.out.println("  Use to generate a list of assertions.\n");
            System.out
                    .println("  "
                            + cmd
                            + " -mode=doc -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out
                    .println("    [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
            return;
        }

        TestDriverConfig driverConfig = new TestDriverConfig(suiteName,
                sessionId, sources, logDir, validate, mode);
        Thread.currentThread().setName("CTL Test Engine");
        Test t = new Test(driverConfig);
        
        // Hack: must reset DOC_MODE to TEST_MODE after creating Test t, but before running it with t.test 
        if (mode == DOC_MODE) {
            driverConfig.setMode(TEST_MODE);
        }
        
        TECore core = new TECore(System.out, false);
        t.test(tests, core);
    }
}
