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

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.Configuration;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.index.TestEntry;
import com.occamlab.te.saxon.TEFunctionLibrary;
import com.occamlab.te.util.Misc;

import com.occamlab.te.Globals;

public class Test {
    public static final int TEST_MODE = 0;
    public static final int RETEST_MODE = 1;
    public static final int RESUME_MODE = 2;
    public static final int DOC_MODE = 4;
    public static final int CHECK_MODE = 5;

    public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
    public static final String TE_NS = "http://www.occamlab.com/te";
    public static final String CTL_NS = "http://www.occamlab.com/ctl";

//    ClassLoader CL;
//    DocumentBuilderFactory DBF;
//    DocumentBuilder DB;
//    TransformerFactory TF;
//    Templates executableTestSuite;
//    static Logger appLogger;

    public static void generateXsl(SetupOptions opts) throws Exception {
        // create CTL validator
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema ctl_schema = sf.newSchema(Misc.getResourceAsFile("com/occamlab/te/schemas/ctl.xsd"));
        Validator ctl_validator = ctl_schema.newValidator();
        CtlErrorHandler validation_eh = new CtlErrorHandler();
        ctl_validator.setErrorHandler(validation_eh);
        
        // create transformer to generate executable script from CTL sources
        Processor processor = new Processor(false);
        XsltCompiler generatorCompiler = processor.newXsltCompiler();

        File generatorStylesheet;
        if (opts.getMode() == DOC_MODE) {
            generatorStylesheet = Misc.getResourceAsFile("com/occamlab/te/generate_dxsl.xsl");
        } else {
            generatorStylesheet = Misc.getResourceAsFile("com/occamlab/te/generate_xsl.xsl");
        }

        // Prepare the XSLT transformer
        XsltExecutable generatorXsltExecutable = generatorCompiler.compile(new StreamSource(generatorStylesheet));
        XsltTransformer generatorTransformer = generatorXsltExecutable.load();

        ArrayList<File> sources = new ArrayList<File>();
        File f = Misc.getResourceAsFile("com/occamlab/te/scripts/parsers.ctl");
        sources.add(f.getParentFile());
        sources.addAll(opts.getSources());

        // Create a list of source CTL files only (no dirs),
        // and a corresponding list containing a working dir for each file
        ArrayList<File> sourceFiles = new ArrayList<File>(); 
        ArrayList<File> workDirs = new ArrayList<File>();
        Iterator<File> it = sources.iterator();
        while (it.hasNext()) {
            File source = it.next();
//          appLogger.log(Level.INFO, "Processing source(s) at: " + source.getAbsolutePath());

            String encodedName = URLEncoder.encode(source.getAbsolutePath(), "UTF-8");
            File workingDir = new File(opts.getWorkDir(), encodedName);
            workingDir.mkdir();
            
            if (source.isDirectory()) {
                String[] children = source.list();
                for (int i = 0; i < children.length; i++) {
                    // Finds all .ctl and .xml files in the directory to use
                    String lowerName = children[i].toLowerCase();
                    if (lowerName.endsWith(".ctl") || lowerName.endsWith(".xml")) {
                        File file = new File(source, children[i]);
                        if (file.isFile()) {
                            sourceFiles.add(file);
                            String basename = children[i].substring(0, children[i].length() - 4);
                            File subdir = new File(workingDir, basename);
                            subdir.mkdir();
                            workDirs.add(subdir);
                        }
                    }
                }
            } else {
                sourceFiles.add(source);
                workDirs.add(workingDir);
            }
        }
        
        for (int i = 0; i < sourceFiles.size(); i++) {
            File sourceFile = sourceFiles.get(i);
            File workingDir = workDirs.get(i);

            File indexFile = new File(workingDir, "index.xml");
            Index index = null;
            boolean outOfDate = true;
            if (indexFile.isFile()) {
                try {
                    index = new Index(indexFile);
                    outOfDate = index.outOfDate();
                } catch (Exception e) {
                    // If there was an exception reading the index file, it is likely corrupt.  Regenerate it
                    outOfDate = true;
                }
            }
            
            boolean validationErrors = false;
            if (opts.isValidate() && outOfDate) {
                int old_count = validation_eh.getErrorCount();
                ctl_validator.validate(new StreamSource(sourceFile));
                validationErrors = (validation_eh.getErrorCount() > old_count);
            }
            
            if (!validationErrors && outOfDate) {
                generatorTransformer.setSource(new StreamSource(sourceFile));
                Serializer generatorSerializer = new Serializer();
                generatorSerializer.setOutputFile(indexFile);
                generatorTransformer.setDestination(generatorSerializer);
                XdmAtomicValue av = new XdmAtomicValue(workingDir.getAbsolutePath());
                generatorTransformer.setParameter(new QName("outdir"), av);
                generatorTransformer.transform();
                index = new Index(indexFile);
                Globals.masterIndex.add(index);
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
//            appLogger.severe(msg);
            throw new Exception(msg);
        }
    }
    
    public static void initSaxonGlobals() {
        Globals.processor = new Processor(false);
        Configuration config = Globals.processor.getUnderlyingConfiguration();
        TEFunctionLibrary telib = new TEFunctionLibrary(config, Globals.masterIndex);
        FunctionLibraryList liblist = new FunctionLibraryList();
        liblist.addFunctionLibrary(telib);
        liblist.addFunctionLibrary(config.getExtensionBinder("java"));
        config.setExtensionBinder("java", liblist);
        config.setVersionWarning(false);
        Globals.errorListener = new TeErrorListener();
//        config.setErrorListener(Globals.errorListener);

        Globals.compiler = Globals.processor.newXsltCompiler();
        Globals.builder = Globals.processor.newDocumentBuilder();
    }

    public static void preload() {
    }

    static String getTestIdFromLog(Document log) throws Exception {
        Element starttest = (Element) log.getElementsByTagName("starttest").item(0);
        String namespace = starttest.getAttribute("namespace-uri");
        String localName = starttest.getAttribute("local-name");
        return "{" + namespace + "}" + localName;
    }

    static XdmNode getParamsFromLog(Document log) throws Exception {
        Element starttest = (Element) log.getElementsByTagName("starttest").item(0);
        NodeList nl = starttest.getElementsByTagName("params");
        if (nl == null) {
            return null;
        } else {
            return Globals.builder.build(new DOMSource(nl.item(0)));
        }
    }

    // Main test method
    public static void execute(RuntimeOptions opts, PrintStream out, boolean web) throws Exception {
        String sessionId = opts.getSessionId();
        File logDir = opts.getLogDir();
        int mode = opts.getMode();

        if (mode == TEST_MODE && logDir != null) {
            File sessionDir = new File(logDir, sessionId);
            if (sessionDir.isDirectory()) {
                File f = new File(sessionDir, "log.xml");
                if (f.exists()) {
                    f.delete();
                }
                Misc.deleteSubDirs(sessionDir);
            } else {
                sessionDir.mkdir();
            }
        }
        
        List<TestEntry> tests = new ArrayList<TestEntry>();
        List<String> testPaths = new ArrayList<String>();
        List<XdmNode> params = new ArrayList<XdmNode>();

        if (opts.getTestName() != null) {
            tests.add(Globals.masterIndex.getTest(opts.getTestName()));
            testPaths.add(sessionId);
            params.add(opts.getParamsNode());
        } else if (opts.getSuiteName() != null) {
            SuiteEntry suite = Globals.masterIndex.getSuite(opts.getSuiteName());
            tests.add(Globals.masterIndex.getTest(suite.getStartingTest()));
            testPaths.add(sessionId);
            params.add(opts.getParamsNode());
        } else if (mode == RESUME_MODE) {
            Document log = TECore.readLog(logDir, sessionId);
            String testName = getTestIdFromLog(log);
            tests.add(Globals.masterIndex.getTest(testName));
            testPaths.add(sessionId);
            params.add(getParamsFromLog(log));
        } else if (mode == RETEST_MODE) {
            Iterator<String> it = opts.getTestPaths().iterator();
            while (it.hasNext()) {
                String testPath = it.next();
                Document log = TECore.readLog(logDir, testPath);
                String testName = getTestIdFromLog(log);
                tests.add(Globals.masterIndex.getTest(testName));
                testPaths.add(testPath);
                params.add(getParamsFromLog(log));
            }
        } else {
            Iterator<String> it = Globals.masterIndex.getSuiteKeys().iterator();
            if (!it.hasNext()) {
                throw new Exception("Error: No suites in sources.  Must sepecify -test option.");
            }
            SuiteEntry suite = Globals.masterIndex.getSuite(it.next());
            if (suite == null || it.hasNext()) {
                throw new Exception("Error: More than one suite in sources.  Must sepecify -suite or -test option.");
            }
            tests.add(Globals.masterIndex.getTest(suite.getStartingTest()));
            testPaths.add(sessionId);
            params.add(opts.getParamsNode());
        }
        
        TECore core = new TECore(sessionId);
        core.setMode(mode);
        core.setLogDir(logDir);
        for (int i = 0; i < tests.size(); i++) {
            if (mode == RETEST_MODE) {
                File f = new File(logDir, testPaths.get(i));
                Misc.deleteSubDirs(f);
            }
            core.setTestPath(testPaths.get(i));
            core.executeTest(tests.get(i), params.get(i));
//
//            boolean done = false;
//            while (!done) {
//                try {
//                    core.executeTest(tests.get(i), params.get(i));
//                    done = true;
//                } catch (SaxonApiException e) {
//                    PrintWriter logger = core.getLogger();
//                    boolean root = true;
//                    if (logger != null) {
//                        logger.println("<exception><![CDATA[" + e.getMessage() + "]]></exception>");
//                        logger.println("<endtest result=\"" + TECore.FAIL + "\"/>");
//                        core.closeLog();
//                        while (core.getLogger() != null) {
//                            root = false;
//                            core.closeLog();
//                        }
//                    }
//                    out.println(e.getMessage());
//                    if (root) {
//                        done = true;
//                    } else {
//                        System.out.println("Recovering...");
//                        core.setMode(RECOVER_MODE);
//                    }
//                }
//            }
        }
    }

//    private void writeNodeToLog(Logger logger, Node node) {
//        StringWriter strWriter = new StringWriter();
//        Transformer identity = null;
//        try {
//            identity = TF.newTransformer();
//            identity
//                    .transform(new DOMSource(node), new StreamResult(strWriter));
//        } catch (TransformerConfigurationException ex) {
//            logger.fine(ex.toString());
//        } catch (TransformerException ex) {
//            logger.fine(ex.toString());
//        }
//        logger.fine(strWriter.toString());
//        return;
//    }
//
//    /**
//     * Initializes the main application logger. A log file (te.log) is created
//     * in the test session directory.
//     * 
//     * @param sessionDir
//     *            a File representing the location of test session directory.
//     */
//    private void initAppLogger(File sessionDir) {
//        this.appLogger = Logger.getLogger(this.getClass().getName());
//        File logFile = new File(sessionDir, "teamengine.log");
//        StreamHandler streamHandler = null;
//        try {
//            streamHandler = new StreamHandler(new FileOutputStream(logFile),
//                    new SimpleFormatter());
//            streamHandler.setEncoding("UTF-8");
//        } catch (FileNotFoundException ex) {
//            this.appLogger.log(Level.WARNING, ex.getMessage(), ex);
//        } catch (UnsupportedEncodingException ex) {
//            this.appLogger.log(Level.WARNING, ex.getMessage(), ex);
//        } catch (SecurityException ex) {
//            this.appLogger.log(Level.WARNING, ex.getMessage(), ex);
//        }
//
//        if (null != streamHandler) {
//            this.appLogger.addHandler(streamHandler);
//        }
//        return;
//    }
//    /**
//     * Builds Xml of parameters
//     * @param params
//     * 			parameters passed from command line
//     * @return String
//     * 			parameters converted to XmlString
//     */
//    protected String buildParamsXML(ArrayList<String> params){
//		String paramsXML = "<params>";
//        for(int i = 0; i < params.size(); i++){
//        	if(params.get(i).indexOf('=')!= 0){
//            	paramsXML = paramsXML + "<param name=\""+ params.get(i).substring(0, params.get(i).indexOf('='))+ "\">" +
//            				params.get(i).substring(params.get(i).indexOf('=')+1) + "</param>";        		
//        	}
//        }
//        paramsXML = paramsXML + "</params>";
////System.out.println("paramsXML: "+paramsXML);
//
////        net.sf.saxon.s9api.DocumentBuilder documentBuilder = null;
////        documentBuilder = processor.newDocumentBuilder();
////        XdmNode paramsNode = documentBuilder.build(new StreamSource(new StringReader(buildParamsXML(params))));
//
//        return paramsXML;
//    }

    public static void main(String[] args) throws Exception {
        SetupOptions setupOpts = new SetupOptions();
        RuntimeOptions runOpts = new RuntimeOptions();

        boolean sourcesSupplied = false;
        String cmd = "java com.occamlab.te.Test";
        
        setupOpts.setWorkDir(new File("c:\\team_work"));

        // Parse arguments from command-line
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-cmd=")) {
                cmd = args[i].substring(5);
            } else if (args[i].startsWith("-source=")) {
                File f = new File(args[i].substring(8));
                if (f.exists()) {
                    setupOpts.addSource(f);
                    sourcesSupplied = true;
                } else {
                    System.out.println("Error: Can't find source \"" + args[i].substring(8) + "\".");
                    return;
                }
            } else if (args[i].startsWith("-logdir=")) {
                runOpts.setLogDir(new File(args[i].substring(8)));
            } else if (args[i].startsWith("-session=")) {
                runOpts.setSessionId(args[i].substring(9));
            } else if (args[i].startsWith("-test=")) {
                runOpts.setTestName(args[i].substring(6));
            } else if (args[i].startsWith("-suite=")) {
                runOpts.setSuiteName(args[i].substring(7));
            } else if(args[i].startsWith("@")){
                runOpts.addParam(args[i].substring(1));
            } else if (args[i].equals("-mode=test")) {
                setupOpts.setMode(TEST_MODE);
            } else if (args[i].equals("-mode=retest")) {
                setupOpts.setMode(RETEST_MODE);
            } else if (args[i].equals("-mode=resume")) {
                setupOpts.setMode(RESUME_MODE);
            } else if (args[i].equals("-mode=doc")) {
                setupOpts.setMode(DOC_MODE);
            } else if (args[i].startsWith("-mode=")) {
                System.out.println("Error: Invalid mode.");
                return;
            } else if (args[i].equals("-validate=no")) {
                setupOpts.setValidate(false);
            } else if (!args[i].startsWith("-")) {
                if (setupOpts.getMode() == RETEST_MODE) {
                    runOpts.addTestPath(args[i]);
                } else {
                    System.out.println("Unrecognized parameter \"" + args[i] + "\"");
                }
            } else {
                System.out.println("Unrecognized parameter \"" + args[i] + "\"");
            }
        }
        
        runOpts.setMode(setupOpts.getMode());
        runOpts.setWorkDir(setupOpts.getWorkDir());

        if (!sourcesSupplied) {
            System.out.println();
            System.out.println("Test mode:");
            System.out.println("  Use to start a test session.\n");
            System.out.println("  " + cmd + " [-mode=test] -source=ctlfile|dir [-source=ctlfile|dir] ...");
            System.out.println("    [-suite=qname|-test=qname [@param-name=value] ...] [-logdir=dir] [-session=session] \n");
            System.out.println("    qname=[namespace_uri,|prefix:]local_name]\n");
            System.out.println("Resume mode:");
            System.out.println("  Use to resume a test session that was interrupted before completion.\n");
            System.out.println("  " + cmd + " -mode=resume -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out.println("    -logdir=dir -session=session\n");
            System.out.println("Retest mode:");
            System.out.println("  Use to reexecute individual tests.\n");
            System.out.println("  " + cmd + " -mode=retest -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out.println("    -logdir=dir [@param-name=value] test1 [test2] ...\n");
            System.out.println("Doc mode:");
            System.out.println("  Use to generate a list of assertions.\n");
            System.out.println("  " + cmd + " -mode=doc -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out.println("    [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
            return;
        }

        Thread.currentThread().setName("TEAM Engine");
        
        generateXsl(setupOpts);
        
        Globals.masterIndex.initClasses();
        
        initSaxonGlobals();
        
        if (setupOpts.isPreload() || setupOpts.getMode() == CHECK_MODE) {
            preload();
        }
        
        if (setupOpts.getMode() != CHECK_MODE) {
            execute(runOpts, System.out, false);
        }
    }
}
