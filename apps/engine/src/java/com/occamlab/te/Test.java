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
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.instruct.Executable;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.w3c.dom.Document;

import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;
import com.occamlab.te.index.TestEntry;
import com.occamlab.te.saxon.TEFunctionLibrary;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;

public class Test {
    public static final int TEST_MODE = 0;
    public static final int RETEST_MODE = 1;
    public static final int RESUME_MODE = 2;
    public static final int DOC_MODE = 4;
    public static final int CHECK_MODE = 5;

    public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
    public static final String TE_NS = "http://www.occamlab.com/te";
    public static final String CTL_NS = "http://www.occamlab.com/ctl";

    // Generates XSL template files from CTL sources and a master index
    // of metadata about the CTL objects
    public static void generateXsl(SetupOptions opts) throws Exception {
        // Create CTL validator
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema ctl_schema = sf.newSchema(Misc.getResourceAsFile("com/occamlab/te/schemas/ctl.xsd"));
        Validator ctl_validator = ctl_schema.newValidator();
        CtlErrorHandler validation_eh = new CtlErrorHandler();
        ctl_validator.setErrorHandler(validation_eh);
        
        // Create a transformer to generate executable scripts from CTL sources
        Processor processor = new Processor(false);
        XsltCompiler generatorCompiler = processor.newXsltCompiler();
        File generatorStylesheet;
        if (opts.getMode() == DOC_MODE) {
            generatorStylesheet = Misc.getResourceAsFile("com/occamlab/te/generate_dxsl.xsl");
        } else {
            generatorStylesheet = Misc.getResourceAsFile("com/occamlab/te/generate_xsl.xsl");
        }
        XsltExecutable generatorXsltExecutable = generatorCompiler.compile(new StreamSource(generatorStylesheet));
        XsltTransformer generatorTransformer = generatorXsltExecutable.load();

        // Create a list of CTL sources (may be files or dirs)
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
//System.out.println("Processing source(s) at: " + source.getAbsolutePath());
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

        // Process each CTL source file
        for (int i = 0; i < sourceFiles.size(); i++) {
            File sourceFile = sourceFiles.get(i);
            File workingDir = workDirs.get(i);

            // Read previous index for this file (if any), and determine whether the
            // index and xsl need to be regenerated
            File indexFile = new File(workingDir, "index.xml");
            Index index = null;
            boolean regenerate = true;
            if (indexFile.isFile()) {
                try {
                    if (indexFile.lastModified() > generatorStylesheet.lastModified()) {
                        index = new Index(indexFile);
                        regenerate = index.outOfDate();
                    }
                } catch (Exception e) {
                    // If there was an exception reading the index file, it is likely corrupt.  Regenerate it.
                    regenerate = true;
                }
            }
            
            if (regenerate) {
                // Validate the source CTL file 
                boolean validationErrors = false;
                if (opts.isValidate()) {
                    int old_count = validation_eh.getErrorCount();
                    ctl_validator.validate(new StreamSource(sourceFile));
                    validationErrors = (validation_eh.getErrorCount() > old_count);
                }
                
                if (!validationErrors) {
                    // Clean up the working directory
                    Misc.deleteDirContents(workingDir);
                    
                    // Run the generator transformation.  Output is an index file and is saved to disk.
                    // The generator also creates XSL template files in the working dir.
                    generatorTransformer.setSource(new StreamSource(sourceFile));
                    Serializer generatorSerializer = new Serializer();
                    generatorSerializer.setOutputFile(indexFile);
                    generatorTransformer.setDestination(generatorSerializer);
                    XdmAtomicValue av = new XdmAtomicValue(workingDir.getAbsolutePath());
                    generatorTransformer.setParameter(new QName("outdir"), av);
                    generatorTransformer.transform();
                    
                    // Read the generated index
                    index = new Index(indexFile);
                }
            }

            // Add new index entries to the master index
            Globals.masterIndex.add(index);
        }
            
        // If there were any validation errors, display them and throw an exception
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

    // Configures a saxon processor to be used globally and sets up
    // other dependant saxon objects.
    public static void prepareSaxon() throws Exception {
        // Create processor
        Globals.processor = new Processor(false);

        // Modify its configuration settings
        Configuration config = Globals.processor.getUnderlyingConfiguration();
        config.setVersionWarning(false);

        // Change the function library to a new library list that includes
        // our custom java function library
        FunctionLibraryList liblist = new FunctionLibraryList();
        TEFunctionLibrary telib = new TEFunctionLibrary(config, Globals.masterIndex);
        liblist.addFunctionLibrary(telib);
        liblist.addFunctionLibrary(config.getExtensionBinder("java"));
        config.setExtensionBinder("java", liblist);

        // Use our custom error listener which reports line numbers in the CTL source file
        Globals.errorListener = new TeErrorListener();
        config.setErrorListener(Globals.errorListener);

        // Create a compiler and document builder
        Globals.compiler = Globals.processor.newXsltCompiler();
        Globals.builder = Globals.processor.newDocumentBuilder();

        // Load an executable for the TECore.form method
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream("com/occamlab/te/formfn.xsl");
        Globals.formExecutable = Globals.compiler.compile(new StreamSource(is));
    }

    public static void preload() {
        //TODO: implement
    }

    // Execute tests
    public static void execute(RuntimeOptions opts, PrintStream out, boolean web) throws Exception {
        String sessionId = opts.getSessionId();
        File logDir = opts.getLogDir();
        int mode = opts.getMode();

        // Create an array containing test(s) to execute, and corresponding
        // arrays containing the test path and parameters for the tests
        List<TestEntry> tests = new ArrayList<TestEntry>();
        List<String> testPaths = new ArrayList<String>();
        List<XdmNode> params = new ArrayList<XdmNode>();
        List<XdmNode> contexts = new ArrayList<XdmNode>();

        // Fill the arrays
        if (mode == RESUME_MODE) {
            Document log = LogUtils.readLog(logDir, sessionId);
            String testName = LogUtils.getTestIdFromLog(log);
            tests.add(Globals.masterIndex.getTest(testName));
            testPaths.add(sessionId);
            params.add(LogUtils.getParamsFromLog(log));
            contexts.add(LogUtils.getContextFromLog(log));
        } else if (mode == RETEST_MODE) {
            Iterator<String> it = opts.getTestPaths().iterator();
            while (it.hasNext()) {
                String testPath = it.next();
                Document log = LogUtils.readLog(logDir, testPath);
                String testName = LogUtils.getTestIdFromLog(log);
                tests.add(Globals.masterIndex.getTest(testName));
                testPaths.add(testPath);
                params.add(LogUtils.getParamsFromLog(log));
                contexts.add(LogUtils.getContextFromLog(log));
            }
        } else if (opts.getTestName() != null) {
            tests.add(Globals.masterIndex.getTest(opts.getTestName()));
            testPaths.add(sessionId);
            params.add(opts.getParamsNode());
            contexts.add(opts.getContextNode());
        } else if (opts.getSuiteName() != null) {
            SuiteEntry suite = Globals.masterIndex.getSuite(opts.getSuiteName());
            tests.add(Globals.masterIndex.getTest(suite.getStartingTest()));
            testPaths.add(sessionId);
            params.add(opts.getParamsNode());
            contexts.add(opts.getContextNode());
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
            contexts.add(opts.getContextNode());
        }
        
        // Instantiate a core object
        TECore core = new TECore(sessionId);
        core.setMode(mode);
        core.setLogDir(logDir);

        // Process each test
        for (int i = 0; i < tests.size(); i++) {
            if ((mode == TEST_MODE || mode == RETEST_MODE) && logDir != null) {
                // Create log directory or clean up old log files 
                File dir = new File(logDir, testPaths.get(i));
                if (dir.isDirectory()) {
                    File f = new File(dir, "log.xml");
                    if (f.exists()) {
                        f.delete();
                    }
                    Misc.deleteSubDirs(dir);
                } else {
                    dir.mkdir();
                }
            }
            
            // Set the test path
            core.setTestPath(testPaths.get(i));

            // Execute the test
            TestEntry test = tests.get(i);
            XPathContext context = null; 
            if (test.usesContext()) {
//              Executable ex = new Executable(Globals.processor.getUnderlyingConfiguration());
                XsltExecutable xe = test.loadExecutable();
                Executable ex = xe.getUnderlyingCompiledStylesheet().getExecutable();
                context = new XPathContextMajor(contexts.get(i).getUnderlyingNode(), ex);
            }
            core.executeTest(tests.get(i), params.get(i), context);
        }
    }

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

    public static void main(String[] args) throws Exception {
        SetupOptions setupOpts = new SetupOptions();
        RuntimeOptions runOpts = new RuntimeOptions();

        boolean sourcesSupplied = false;
        String cmd = "java com.occamlab.te.Test";
        File workDir = null;
        int mode = TEST_MODE;

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
            } else if (args[i].startsWith("-workdir=")) {
                workDir = new File(args[i].substring(9));
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
                setupOpts.setValidate(false);
            } else if (!args[i].startsWith("-")) {
                if (mode == RETEST_MODE) {
                    runOpts.addTestPath(args[i]);
                } else {
                    System.out.println("Unrecognized parameter \"" + args[i] + "\"");
                }
            } else {
                System.out.println("Unrecognized parameter \"" + args[i] + "\"");
            }
        }
        
        if (workDir == null) {
            workDir = new File(System.getProperty("java.io.tmpdir"), "te_work");
            workDir.mkdirs();
        } else {
            if (!workDir.isDirectory()) {
                System.out.println("Error: Working directory " + workDir + " does not exist.");
                return;
            }
        }

        setupOpts.setWorkDir(workDir);
        runOpts.setWorkDir(workDir);

        setupOpts.setMode(mode);
        runOpts.setMode(mode);

        if (!sourcesSupplied) {
            System.out.println();
            System.out.println("Test mode:");
            System.out.println("  Use to start a test session.\n");
            System.out.println("  " + cmd + " [-mode=test] -source=ctlfile|dir [-source=ctlfile|dir] ...");
            System.out.println("    [-suite=qname|-test=qname [@param-name=value] ...] [-logdir=dir] [-session=session] \n");
            System.out.println("    qname=[namespace_uri,|prefix:]local_name]\n");
            System.out.println("Resume mode:");
            System.out.println("  Use to resume a test session that was interrupted before completion.\n");
            System.out.println("  " + cmd + " -mode=resume -logdir=dir session\n");
            System.out.println("Retest mode:");
            System.out.println("  Use to reexecute individual tests.\n");
            System.out.println("  " + cmd + " -mode=retest -logdir=dir testapth1 [testpath2] ...\n");
            System.out.println("Doc mode:");
            System.out.println("  Use to generate a list of assertions.\n");
            System.out.println("  " + cmd + " -mode=doc -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out.println("    [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
            return;
        }

        Thread.currentThread().setName("TEAM Engine");
        
        generateXsl(setupOpts);
        
        prepareSaxon();

        // Set memory theshhold
        if (Globals.memThreshhold == 0) {
            long maxMemory = Runtime.getRuntime().maxMemory();
            if (maxMemory >= 32768*1024) {
                // Set threshhold at 16K if there is 32K or more available
                Globals.memThreshhold = maxMemory - 16384*1024;
            } else {
                // Otherwise, set it at half the memory available
                Globals.memThreshhold = maxMemory / 2;
            }
        }

        if (setupOpts.isPreload() || mode == CHECK_MODE) {
            preload();
        }
        
        if (mode != CHECK_MODE) {
            execute(runOpts, System.out, false);
        }
    }
}
