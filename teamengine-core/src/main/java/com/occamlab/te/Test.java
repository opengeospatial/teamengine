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

 Contributor(s): 
 2009         F. Vitale     vitale@imaa.cnr.it
           
 */

package com.occamlab.te;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamSource;

import com.occamlab.te.index.Index;
import com.occamlab.te.util.DocumentationHelper;
import com.occamlab.te.util.LogUtils;
import com.occamlab.te.util.Misc;

/**
 * 
 * The main class for the TEAM Engine command line interface.
 * 
 */
public class Test {
    private static final Logger LOGR = Logger.getLogger(Test.class.getName());
    public static final int TEST_MODE = 0;
    public static final int RETEST_MODE = 1;
    public static final int RESUME_MODE = 2;
    public static final int REDO_FROM_CACHE_MODE = 3;

    public static final int DOC_MODE = 4;
    public static final int CHECK_MODE = 5;
    public static final int PRETTYLOG_MODE = 6;

    public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
    public static final String TE_NS = "http://www.occamlab.com/te";
    public static final String CTL_NS = "http://www.occamlab.com/ctl";
    public static final String CTLP_NS = "http://www.occamlab.com/te/parsers";

    SetupOptions setupOpts;
    RuntimeOptions runOpts;

    /**
     * Constructs a test executor with default options.
     */
    public Test() {
        this.setupOpts = new SetupOptions();
        this.runOpts = new RuntimeOptions();
    }

    void setSetupOptions(SetupOptions setupOpts) {
        this.setupOpts = setupOpts;
    }

    void setRuntimeOptions(RuntimeOptions runOpts) {
        this.runOpts = runOpts;
    }

    public void executeTest(String relativePathToMainCtl) throws Exception {
        // File file =Misc.getResourceAsFile(relativePathToMainCtl);
        String[] arguments = new String[1];
        arguments[0] = "-source=" + relativePathToMainCtl;
        execute(arguments);
    }

    public static void main(String[] args) throws Exception {
        Test test = new Test();
        test.execute(args);
    }

    /**
     * Executes a test suite.
     * 
     * @param args
     *            Command line arguments
     * @throws Exception
     */
    public void execute(String[] args) throws Exception {
        String cmd = "java com.occamlab.te.Test";
        File workDir = setupOpts.getWorkDir();
        runOpts.setWorkDir(workDir);
        File logDir = runOpts.getLogDir();
        String session = null;
        int mode = TEST_MODE;
        File sourceFile = null;

        // Parse arguments from command-line
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-cmd=")) {
                cmd = args[i].substring(5);
            } else if (args[i].startsWith("-source=")) {
                String sourcePath = args[i].substring(8);
                sourceFile = new File(sourcePath);
                if (!sourceFile.isAbsolute()) {
                    File scriptsDir = new File(
                            SetupOptions.getBaseConfigDirectory(), "scripts");
                    sourceFile = new File(scriptsDir, sourcePath);
                }
                if (sourceFile.exists()) {
                    setupOpts.addSource(sourceFile);
                } else {
                    System.out.println("Error: Cannot find CTL script(s) at "
                            + sourceFile.getAbsolutePath());
                    return;
                }
            } else if (args[i].startsWith("-session=")) {
                session = args[i].substring(9);
            } else if (args[i].startsWith("-base=")) {
                runOpts.setBaseURI(args[i].substring(6));
            } else if (args[i].startsWith("-test=")) {
                runOpts.setTestName(args[i].substring(6));
            } else if (args[i].startsWith("-suite=")) {
                runOpts.setSuiteName(args[i].substring(7));
            } else if (args[i].startsWith("-profile=")) {
                runOpts.addProfile(args[i].substring(9));
            } else if (args[i].startsWith("@")) {
                runOpts.addParam(args[i].substring(1));
            } else if (args[i].equals("-mode=test")) {
                mode = TEST_MODE;
            } else if (args[i].equals("-mode=retest")) {
                mode = RETEST_MODE;
            } else if (args[i].equals("-mode=resume")) {
                mode = RESUME_MODE;
            } else if (args[i].equals("-mode=doc")) {
                mode = DOC_MODE;
            } else if (args[i].equals("-mode=check")) {
                mode = CHECK_MODE;
            } else if (args[i].equals("-mode=pplogs")) {
                mode = PRETTYLOG_MODE;
            } else if (args[i].equals("-mode=cache")) {
                mode = REDO_FROM_CACHE_MODE;
            } else if (args[i].startsWith("-mode=")) {
                System.out.println("Error: Invalid mode.");
                return;
            } else if (args[i].equals("-validate=no")) {
                setupOpts.setValidate(false);
            } else if (!args[i].startsWith("-")) {
                if (mode == RETEST_MODE) {
                    runOpts.addTestPath(args[i]);
                } else {
                    System.out.println("Unrecognized parameter \"" + args[i]
                            + "\"");
                }
            } else {
                System.out
                        .println("Unrecognized parameter \"" + args[i] + "\"");
            }
        }

        // Set mode
        runOpts.setMode(mode);

        // Syntax checks
        if ((mode == RETEST_MODE && (logDir == null || session == null))
                || (mode == RESUME_MODE && (logDir == null || session == null))) {
            syntax(cmd);
            return;
        }
        if (mode == REDO_FROM_CACHE_MODE && (logDir == null || session == null)) {
            syntax(cmd);
            return;
        }
        if (runOpts.getProfiles().size() > 0 && logDir == null) {
            System.out
                    .println("Error: A -logdir parameter is required for testing profiles");
            return;
        }
        if (mode == PRETTYLOG_MODE && logDir == null) {
            System.out
                    .println("Error: A -logdir parameter is required to create report");
            return;
        }

        // Set session
        if (session == null) {
            session = System.getProperty("team.session");
        }
        if (session == null) {
            if (logDir == null) {
                session = "s0001";
            } else {
                session = LogUtils.generateSessionId(logDir);
            }
        }
        runOpts.setSessionId(session);
        Thread.currentThread().setName("TEAM Engine");
        Index masterIndex = null;
        File indexFile = null;
        if (logDir != null && session != null) {
            File dir = new File(logDir, runOpts.getSessionId());
            indexFile = new File(dir, "index.xml");
        }

        if (mode == DOC_MODE) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            DocumentationHelper docCode = new DocumentationHelper(
                    cl.getResource("com/occamlab/te/PseudoCTLDocumentation.xsl"));
            File html_output_documentation_file = new File(
                    workDir.getAbsolutePath() + File.separator
                            + "documentation.html");
            if (html_output_documentation_file.exists())
                throw new Exception(
                        "Error: Documentation file already exists, check the file "
                                + html_output_documentation_file
                                        .getAbsolutePath() + " ");
            docCode.generateDocumentation(setupOpts.getSources().get(0)
                    .getAbsolutePath(), new FileOutputStream(
                    html_output_documentation_file));
            System.out.println("Test documentation file \""
                    + html_output_documentation_file.getAbsolutePath()
                    + "\" created!");
            return;
        }

        if (mode == PRETTYLOG_MODE) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            DocumentationHelper docLogs = new DocumentationHelper(
                    cl.getResource("com/occamlab/te/test_report_html.xsl"));
            docLogs.prettyPrintsReport(logDir);
            return;
        }
        if (mode == TEST_MODE || mode == CHECK_MODE) {
            masterIndex = Generator.generateXsl(setupOpts);
            if (indexFile != null) {
                masterIndex.persist(indexFile);
            }
        } else if (mode == REDO_FROM_CACHE_MODE) {
            boolean regenerate = false;
            if (indexFile.canRead()) {
                masterIndex = new Index(indexFile);
                if (masterIndex.outOfDate()) {
                    System.out
                            .println("Warning: Scripts have changed since this session was first executed.");
                    regenerate = true;
                }
            } else {
                System.out.println("Error: Can't read index file.");
                regenerate = true;
            }
            if (regenerate) {
                System.out
                        .println("Regenerating masterIndex from source scripts");
                masterIndex = Generator.generateXsl(setupOpts);
                if (indexFile != null) {
                    masterIndex.persist(indexFile);
                }
            }
        } else {
            if (!indexFile.canRead()) {
                System.out.println("Error: Can't read index file.");
                return;
            }
            masterIndex = new Index(indexFile);
            if (masterIndex.outOfDate()) {
                System.out
                        .println("Warning: Scripts have changed since this session was first executed.");
            }
        }

        if (mode == REDO_FROM_CACHE_MODE) {
            File stylesheet = Misc
                    .getResourceAsFile("com/occamlab/te/web/viewlog.xsl");
            Templates ViewLogTemplates = ViewLog.transformerFactory
                    .newTemplates(new StreamSource(stylesheet));
            File userlog = logDir;
            StringWriter sw = new StringWriter();
            String testName=null;
            ViewLog.view_log(testName,userlog, session, new ArrayList<String>(),
                    ViewLogTemplates, sw);
            boolean hasCache = ViewLog.hasCache();
            if (!hasCache) {
                File dir = new File(logDir, runOpts.getSessionId());
                throw new Exception("Error: no cache for "
                        + dir.getAbsolutePath());
            }
        }

        masterIndex.setElements(null);
        TEClassLoader cl = new TEClassLoader(findResourcesDirectory(sourceFile));
        Engine engine = new Engine(masterIndex, setupOpts.getSourcesName(), cl);

        if (setupOpts.isPreload() || mode == CHECK_MODE) {
            engine.preload(masterIndex, setupOpts.getSourcesName());
        }

        if (LOGR.isLoggable(Level.FINE)) {
            LOGR.fine(runOpts.toString());
        }

        if (mode != CHECK_MODE) {
            TECore core = new TECore(engine, masterIndex, runOpts);
            core.execute();
        }

    }

    /**
     * Seeks a "resources" directory by searching the file system from a
     * starting location and continuing upwards into ancestor directories.
     * 
     * @param sourceFile
     *            A File denoting a file system location (file or directory).
     * @return A File representing a directory named "resources", or
     *         {@code null} if one cannot be found.
     */
    static File findResourcesDirectory(File sourceFile) {
        File parent = sourceFile.getParentFile();
        if (null == parent) {
            return null;
        }
        File[] resourceDirs = parent.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.equalsIgnoreCase("resources") && new File(dir,
                        name).isDirectory());
            }
        });
        if (resourceDirs.length > 0) {
            return resourceDirs[0];
        }
        return findResourcesDirectory(parent);
    }

    /**
     * Displays startup command syntax.
     * 
     * @param cmd
     *            Name of the startup command (i.e. test.bat or test.sh)
     */
    static void syntax(String cmd) {
        System.out.println();
        System.out.println("Test mode:");
        System.out.println("  Use to start a test session.\n");
        System.out.println("  " + cmd
                + " [-mode=test] [-source=ctlfile|dir]...");
        System.out.println("  [-session=session] [-base=baseURI]");
        System.out
                .println("    [-suite=qname|-test=qname [@param-name=value] ...] [-profile=qname|*] ...\n");
        System.out.println("    qname=[namespace_uri,|prefix:]local_name]\n");
        System.out.println("Resume mode:");
        System.out
                .println("  Use to resume a test session that was interrupted before completion.\n");
        System.out.println("  " + cmd
                + " -mode=resume -logdir=dir -session=session\n");
        System.out.println("Retest mode:");
        System.out.println("  Use to reexecute individual tests.\n");
        System.out
                .println("  "
                        + cmd
                        + " -mode=retest -logdir=dir -session=session testpath1 [testpath2] ...\n");
        System.out.println("Doc mode:");
        System.out.println("  Use to generate documentation of tests.\n");
        System.out
                .println("  "
                        + cmd
                        + " -mode=doc -source=<main ctl file> [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
        System.out.println("PPLogs mode:");
        System.out
                .println("  Pretty Print Logs mode is used to generate a readable HTML report of execution.\n");
        System.out.println("  " + cmd
                + " -mode=pplogs -logdir=<dir of a session log>  \n");
        System.out.println("  " + cmd
                + "-mode=cache -logdir=dir -session=session\n");
    }

    /**
     * Returns name of mode.
     * 
     * @param mode
     */
    public static String getModeName(int mode) {
        switch (mode) {
        case 0:
            return "Test Mode";
        case 1:
            return "Retest Mode";
        case 2:
            return "Resume Mode";
        case 3:
            return "Redo From Cache Mode";
        case 4:
            return "Doc Mode";
        case 5:
            return "Check Mode";
        case 6:
            return "Pretty Log Mode";
        default:
            return "Invalid Mode";
        }
    }
}
