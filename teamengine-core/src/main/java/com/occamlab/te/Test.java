/*

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): 
 2009         F. Vitale     vitale@imaa.cnr.it
 2018         C. Heazel     cheazel@wiscenterprisesl.com

 Modifications: 
 2/14/18
    - Addressed path manipulation vulnerabilities and general cleanup.
    - Identified Issues which need further discussion.
           
 */

package com.occamlab.te;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.occamlab.te.index.Index;
import com.occamlab.te.util.LogUtils;

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

    /**
     * Replace the SetupOptions with a new copy
     *
     * @param	setupOpts
     *              The new copy of the setup options
     *
     * ISSUE: SetupOptions are established when the Teamengin is initialized.  they should be the same for all tests.  Why do we allow them to be modified?
     * ISSUE: when and how are the setup options validated?
     * ANSWER: Delete this method since it is never called.
     */
    // void setSetupOptions(SetupOptions setupOpts) {
    //    this.setupOpts = setupOpts;
    // }

    /**
     * Replace the RuntimeOptions with a new copy
     *
     * @param	runOpts
     *              The new copy of the runtime options
     *
     * ISSUE: when and how are the runtime options validated?
     * ANSWER: make RuntimeOptions self-validating.
     */
    void setRuntimeOptions(RuntimeOptions runOpts) {
        this.runOpts = runOpts;
    }

    // This method does not appear to be used.  Consider deleting it.
    // Changed from public to private until we validate that is can be removed.
    
    private void executeTest(String relativePathToMainCtl) throws Exception {
        // File file =Misc.getResourceAsFile(relativePathToMainCtl);
        String[] arguments = new String[1];
        arguments[0] = "-source=" + relativePathToMainCtl;
        execute(arguments);
    }

    /**
    * Entry point from the command line.  Creates a new test object then executes it.
    * 
    * @param args
    *             An array of command line arguments
    * @throws Exception
    */
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
        @SuppressWarnings("unused") boolean rslt;

        // Copy work directory from setup options to runtime
        File workDir = setupOpts.getWorkDir();
        rslt = runOpts.setWorkDir(workDir);

        // Initialize the rest of the test context
        String cmd = "java com.occamlab.te.Test";
        File logDir = runOpts.getLogDir();
        String session = null;
        boolean hasSessionArg = false;
        int mode = TEST_MODE;

        // Parse arguments from command-line
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-cmd=")) {
                cmd = arg.substring(5);
	        } else if (arg.equals("-h") || arg.equals("-help") || arg.equals("-?")) {
	            syntax(cmd);
	            return;
            // The path to the test script.  If the file name is not an absolute
            // path then it must be under the Scripts directory.
            // Issue: should we restrict the range of valid paths for source files? 
	        } else if (arg.startsWith("-source=")) {
                String sourcePath = arg.substring(8);
                File sourceFile = new File(sourcePath);
                if (!sourceFile.isAbsolute()) {
                    File scriptsDir = new File(
                            SetupOptions.getBaseConfigDirectory(), "scripts");
                    sourceFile = new File(scriptsDir, sourcePath);
                }
                // Fortify Mod: Validate the sourceFile.  It must both exist
                // and pass validation by setupOptions
                if (! sourceFile.exists() || ! setupOpts.addSourceWithValidation(sourceFile)){
                    System.out.println("Error: Cannot find CTL script(s) at "
                            + sourceFile.getAbsolutePath());
                    return;
                }
            } else if (args[i].startsWith("-logdir=")) {
            	String path = args[i].substring(8);
                File file = new File(path);
                if (file.isAbsolute()) {
                	logDir = file;
                } else {
                	logDir = new File(SetupOptions.getBaseConfigDirectory(), path);
                }
                if (! logDir.exists() || !runOpts.setLogDir(logDir)) {
                    System.out.println("Error: Cannot use logdir " + logDir.getAbsolutePath());
                    return;
                }
            } else if (arg.startsWith("-session=")) {
                // Issue: session is not validated but it is used as part of a file path.
            	hasSessionArg = true;
                session = arg.substring(9);
            } else if (arg.startsWith("-base=")) {
                rslt = runOpts.setBaseURI(arg.substring(6));
            } else if (arg.startsWith("-test=")) {
                rslt = runOpts.setTestName(arg.substring(6));
            } else if (arg.startsWith("-suite=")) {
                rslt = runOpts.setSuiteName(arg.substring(7));
            } else if (arg.startsWith("-profile=")) {
                rslt = runOpts.addProfile(arg.substring(9));
            } else if (arg.startsWith("@")) {
                rslt = runOpts.addParam(arg.substring(1));
            } else if (arg.equals("-mode=test")) {
                mode = TEST_MODE;
            } else if (arg.equals("-mode=retest")) {
                mode = RETEST_MODE;
            } else if (arg.equals("-mode=resume")) {
                mode = RESUME_MODE;
            } else if (arg.equals("-mode=doc")) {
                mode = DOC_MODE;
            } else if (arg.equals("-mode=check")) {
                mode = CHECK_MODE;
            } else if (arg.equals("-mode=cache")) {
                mode = REDO_FROM_CACHE_MODE;
            } else if (arg.startsWith("-mode=")) {
                System.out.println("Error: Invalid mode.");
                return;
            } else if (arg.equals("-validate=no")) {
                setupOpts.setValidate(false);
            } else if ((arg.startsWith("-form="))) {
              rslt = runOpts.addRecordedForm(arg.substring(6));
            } else if (!arg.startsWith("-")) {
                if (mode == RETEST_MODE || mode == REDO_FROM_CACHE_MODE) {
                	if (hasSessionArg) {
                		if (arg.startsWith(session)) {
                			rslt = runOpts.addTestPath(arg);
                		} else {
                			rslt = runOpts.addTestPath(session + "/" + arg);
                		}
                	} else {
                		int slash = (arg).indexOf("/");
                		if (slash >= 0) {
                			if (session == null) {
                				session = arg.substring(0, slash);
                			} else if (!arg.substring(0, slash).equals(session)) {
                                System.out.println("Each test path must be within the same session.");
                                return;
                			}
               				rslt = runOpts.addTestPath(arg);
                		}
                	}
                } else {
                    System.out.println("Unrecognized parameter \"" + arg
                            + "\"");
                }
            } else {
                System.out
                        .println("Unrecognized parameter \"" + arg + "\"");
            }
        }
        
        // Set mode
        rslt = runOpts.setMode(mode);
        rslt = runOpts.setSessionId(session);
        
        if (mode == RETEST_MODE || mode == RESUME_MODE || mode == REDO_FROM_CACHE_MODE) {
        	if (logDir == null || session == null) {
	            syntax(cmd);
	            return;
        	}
        	if (setupOpts.getSources().isEmpty()) {
        		loadSources();
        	}
        }
    
        // Syntax checks
        if (setupOpts.getSources().isEmpty()) {
            System.out
            .println("Error: At least one -source parameter is required");
            return;
        }
        if (runOpts.getProfiles().size() > 0 && logDir == null) {
            System.out.println("Error: A -logdir parameter is required for testing profiles");
            return;
        }

        // Set session
        if (session == null) {
            session = System.getProperty("team.session");
        }
        // CMH - changed session format to UUID.
        // BPR - changed back to previous format
        if (session == null) {           
        	if (logDir == null) {
                session = "s0001";
            } else {
                session = LogUtils.generateSessionId(logDir);
            }
            rslt = runOpts.setSessionId(session);
        }

        Thread.currentThread().setName("TEAM Engine");
        Index masterIndex = new Index();
        File indexFile = null;
        if (logDir != null && session != null) {
            File dir = new File(logDir, runOpts.getSessionId());
            indexFile = new File(dir, "index.xml");
        }

        boolean regenerate = false;
        
        if (mode == TEST_MODE || mode == CHECK_MODE) {
            regenerate = true;
        } else if (mode == DOC_MODE) {
        	masterIndex = Generator.generateDocXsl(setupOpts);
        } else {
            if (indexFile == null || !indexFile.canRead()) {
                System.out.println("Error: Can't read index file.");
                regenerate = true;
            } else {
            	masterIndex = new Index(indexFile);
	            if (masterIndex.outOfDate()) {
	                System.out.println("Warning: Scripts have changed since this session was first executed.");
	                if (mode == REDO_FROM_CACHE_MODE) {
	                    System.out.println("Regenerating masterIndex from source scripts");
	                	regenerate = true;
	                }
	            }
            }
        }

        if (regenerate) {
            masterIndex = Generator.generateXsl(setupOpts);
            if (indexFile != null) {
                masterIndex.persist(indexFile);
            }
        }
        
        if (mode == REDO_FROM_CACHE_MODE) {
            boolean hasCache = ViewLog.checkCache(logDir, session);
            if (!hasCache) {
                File dir = new File(logDir, runOpts.getSessionId());
                throw new Exception("Error: no cache for "
                        + dir.getAbsolutePath());
            }
        }

        // Fortify Mod: Make sure masterIndex is not null
        // masterIndex.setElements(null);
        if( masterIndex != null) {
        	masterIndex.setElements(null);
        } else {
        	masterIndex = new Index();
        }
        List<File> resourcesDirs = new ArrayList<File>();
        for (File sourceFile : setupOpts.getSources()) {
        	File resourcesDir = findResourcesDirectory(sourceFile);
        	if (!resourcesDirs.contains(resourcesDir)) {
        		resourcesDirs.add(resourcesDir);
        	}
        }
        TEClassLoader cl = new TEClassLoader(resourcesDirs);
        Engine engine = new Engine(masterIndex, setupOpts.getSourcesName(), cl);

        if (setupOpts.isPreload() || mode == CHECK_MODE) {
            engine.preload(masterIndex, setupOpts.getSourcesName());
        }

        if (LOGR.isLoggable(Level.FINE)) {
            LOGR.fine(runOpts.toString());
        }

        if (mode == TEST_MODE) {
        	saveSources();
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
        System.out.println("  " + cmd + " [-mode=test] -source=ctlfile|dir ...");
        System.out.println("    [-logdir=dir] [-session=session] [-base=baseURI]");
        System.out.println("    -suite=qname [-profile=qname|*] ... | -test=qname [@param-name=value] ...\n");
        System.out.println("    qname=[namespace_uri,|prefix:]local_name\n");
        System.out.println("Resume mode:");
        System.out.println("  Use to resume a test session that was interrupted before completion.\n");
        System.out.println("  " + cmd + " -mode=resume [-logdir=dir] -session=session\n");
        System.out.println("Retest mode:");
        System.out.println("  Use to reexecute individual tests.\n");
        System.out.println("  " + cmd + " -mode=retest [-logdir=dir] testpath1 [testpath2] ...\n");
        System.out.println("Redo From Cache Mode:");
        System.out.println("  Use to rerun tests with cached server responses when the test has changed.\n");
        System.out.println("  " + cmd + "-mode=cache [-logdir=dir] [-session=session] [testpath1 [testpath2]]\n");
        System.out.println("  Warning: Test changes may make cached server responses invalid.\n");
        System.out.println("Check mode:");
        System.out.println("  Use to validate source files.\n");
        System.out.println("  " + cmd + " -mode=check -source=ctlfile|dir ...\n");
        System.out.println("Doc mode:");
        System.out.println("  Use to visit all subtests and generate log files without executing them.\n");
        System.out.println("  " + cmd + " -mode=doc -source=ctlfile|dir ... [-suite=qname]\n");
        
        /* 
         * Doc mode once again behaves as it originally did in https://sourceforge.net/p/teamengine/code/1
         * The entry point for the PseudoCTLDocumentation generator (which was called with -mode=doc starting with
         * https://sourceforge.net/p/teamengine/code/459) is now the main method of DocumentationHelper.
         */
        
        /* 
         * The entry point for the PrettyPrintLogs functionality which was called with -mode=pplogs
         * is now in the main method of ViewLog and is activated with its -html parameter.
         */
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
        default:
            return "Invalid Mode";
        }
    }
    
    private void saveSources() {
    	if (runOpts.getLogDir() != null && runOpts.getSessionId() != null) {
	    	File sessionDir = new File(runOpts.getLogDir(), runOpts.getSessionId());
	    	File sourcesFile = new File(sessionDir, "sources.xml");
	    	try (PrintWriter writer = new PrintWriter(sourcesFile)) {
		    	writer.println("<sources>");
		    	for (File file : setupOpts.getSources()) {
		    		writer.println("<source>" + file.getPath() + "</source>");
		    	}
		    	writer.println("</sources>");
	    	} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	}
    }
    
    private void loadSources() {
    	if (runOpts.getLogDir() != null && runOpts.getSessionId() != null) {
	    	File sessionDir = new File(runOpts.getLogDir(), runOpts.getSessionId());
	    	File sourcesFile = new File(sessionDir, "sources.xml");
	        if (sourcesFile.exists()) {
				try {
		            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		            dbf.setExpandEntityReferences(false);
					DocumentBuilder db = dbf.newDocumentBuilder();
		            Document doc = db.parse(sourcesFile);
		            NodeList nl = doc.getElementsByTagName("source");
		            for (int i = 0; i < nl.getLength(); i++) {
		                String sourcePath = nl.item(i).getTextContent();
		                File sourceFile = new File(sourcePath);
		                setupOpts.addSourceWithValidation(sourceFile);
		            }
				} catch (Exception e) {
					e.printStackTrace();
				}
	        }
    	}
    }
}
