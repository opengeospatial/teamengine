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
 2011         Paul Daisey   pauld@imagemattersllc.com  
           
 */

package com.occamlab.te;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.occamlab.te.index.Index;
import com.occamlab.te.util.DocumentationHelper;
import com.occamlab.te.util.LogUtils; 

import java.io.*;                    // 2011-06-27 PwD
import java.util.*;                  // 2011-06-27 PwD
import javax.xml.transform.*;        // 2011-06-27 PwD
import javax.xml.transform.stream.*; // 2011-06-27 PwD
import com.occamlab.te.util.Misc;    // 2011-06-27 PwD


/**
 * 
 * The main class for the TEAM Engine command line interface. 
 *
 */
public class Test {
    public static final int TEST_MODE = 0;
    public static final int RETEST_MODE = 1;
    public static final int RESUME_MODE = 2;
    public static final int REDO_FROM_CACHE_MODE = 3; // 2011-06-09 PwD    
    public static final int DOC_MODE = 4;
    public static final int CHECK_MODE = 5;
    public static final int PRETTYLOG_MODE = 6;

    public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
    public static final String TE_NS = "http://www.occamlab.com/te";
    public static final String CTL_NS = "http://www.occamlab.com/ctl";
    public static final String CTLP_NS = "http://www.occamlab.com/te/parsers";

    /**
     * Returns name of mode
     * @param mode
     */
    public static String getModeName(int mode) {
    	switch (mode){
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
    /**
     * Displays startup command syntax
     * 
     * @param cmd Name of the startup command (i.e. test.bat or test.sh) 
     */
    static void syntax(String cmd) {
        System.out.println();
        System.out.println("Test mode:");
        System.out.println("  Use to start a test session.\n");
        System.out.println("  " + cmd + " [-mode=test] -source=ctlfile|dir [-source=ctlfile|dir] ...");
        System.out.println("    [-workdir=dir] [-logdir=dir] [-session=session] [-base=baseURI]");
        System.out.println("    [-suite=qname|-test=qname [@param-name=value] ...] [-profile=qname|*] ...\n");
        System.out.println("    qname=[namespace_uri,|prefix:]local_name]\n");
        System.out.println("Resume mode:");
        System.out.println("  Use to resume a test session that was interrupted before completion.\n");
        System.out.println("  " + cmd + " -mode=resume -logdir=dir -session=session\n");
        System.out.println("Retest mode:");
        System.out.println("  Use to reexecute individual tests.\n");
        System.out.println("  " + cmd + " -mode=retest -logdir=dir -session=session testpath1 [testpath2] ...\n");
        System.out.println("Doc mode:");
        System.out.println("  Use to generate documentation of tests.\n");
        System.out.println("  " + cmd + " -mode=doc -source=<main ctl file> [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
        System.out.println("PPLogs mode:");
        System.out.println("  Pretty Print Logs mode is used to generate a readable HTML report of execution.\n");
        System.out.println("  " + cmd + " -mode=pplogs -logdir=<dir of a session log>  \n");
        // Start PwD 2011-06-09 addition
        System.out.println("  " + cmd + "-mode=cache -logdir=dir -session=session\n");
        // End PwD 2011-06-09 addition
    }

    /**
     * The main TEAM Engine command line interface.
     * 
     * 
     * @param args Command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SetupOptions setupOpts = new SetupOptions();
        RuntimeOptions runOpts = new RuntimeOptions();

        boolean sourcesSupplied = false;
        String cmd = "java com.occamlab.te.Test";
        File workDir = null;
        File logDir = null;
        String session = null;
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
                logDir = new File(args[i].substring(8));
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
            } else if (args[i].equals("-mode=check")) {
                mode = CHECK_MODE;
            } else if (args[i].equals("-mode=pplogs")){
            	mode = PRETTYLOG_MODE;
            // Start PwD 2011-06-09 addition
            } else if (args[i].equals("-mode=cache")) {
            	mode = REDO_FROM_CACHE_MODE;
            // End PwD 2011-06-09 addition
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
        
        // Set work dir
        if (workDir == null) {
            String prop = System.getProperty("team.workdir");
            if (prop == null) {
                workDir = new File(System.getProperty("java.io.tmpdir"), "te_work");
                workDir.mkdirs();
                System.out.println("No working directory supplied.  Using " + workDir.toString());
            } else {
                workDir = new File(prop);
            }
        }
        if (!workDir.isDirectory()) {
            System.out.println("Error: Working directory " + workDir.toString() + " does not exist.");
            return;
        }
        setupOpts.setWorkDir(workDir);
        runOpts.setWorkDir(workDir);

        // Set log dir
        if (logDir == null) {
            String prop = System.getProperty("team.logdir");
            if (prop != null) {
                logDir = new File(prop);
            }
        }
        if (logDir != null) {
            if (!logDir.isDirectory()) {
                System.out.println("Error: Log directory " + logDir.toString() + " does not exist.");
                return;
            }
        }
        runOpts.setLogDir(logDir);

        // Set mode
        runOpts.setMode(mode);
        
        // Syntax checks
        if ((mode == TEST_MODE && !sourcesSupplied) ||
            (mode == RETEST_MODE && (logDir == null || session == null)) ||
            // Start PwD 2011-06-09 addition
            //(mode == RESUME_MODE && (logDir == null || session == null))
            (mode == RESUME_MODE && (logDir == null || session == null)) ||
            (mode == REDO_FROM_CACHE_MODE && (logDir == null || session == null))
            
            // End PwD 2011-06-09 addition
            ) {
            syntax(cmd);
            return;
        }
        if (runOpts.getProfiles().size() > 0 && logDir == null) {
            System.out.println("Error: A -logdir parameter is required for testing profiles");
            return;
        }
        if (mode == PRETTYLOG_MODE && logDir == null) {
            System.out.println("Error: A -logdir parameter is required to create report");
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
        
        Index masterIndex = null;  // 2011-06-30 PwD
        File indexFile = null;
        if (logDir != null && session != null) {
            File dir = new File(logDir, runOpts.getSessionId());
            indexFile = new File(dir, "index.xml");
        }

        if (mode == DOC_MODE) {
        	ClassLoader cl = Thread.currentThread().getContextClassLoader();           
        	DocumentationHelper docCode= new DocumentationHelper(cl.getResource("com/occamlab/te/PseudoCTLDocumentation.xsl"));
        	File html_output_documentation_file=new File(workDir.getAbsolutePath()+File.separator+"documentation.html");
    		if (html_output_documentation_file.exists())
    			throw new Exception("Error: Documentation file already exists, check the file " + html_output_documentation_file.getAbsolutePath() + " ");
        	
        	//docCode.generateDocumentation(setupOpts.getSources().get(0).getAbsolutePath(),runOpts.getSuiteName(),System.out);
        	docCode.generateDocumentation(setupOpts.getSources().get(0).getAbsolutePath(),new FileOutputStream(html_output_documentation_file));
        	System.out.println("Test documentation file \""+html_output_documentation_file.getAbsolutePath()+"\" created!");
        	return;
        }
        

        if (mode == PRETTYLOG_MODE) {
        	ClassLoader cl = Thread.currentThread().getContextClassLoader();           
        	DocumentationHelper docLogs= new DocumentationHelper(cl.getResource("com/occamlab/te/test_report_html.xsl"));
        	docLogs.prettyPrintsReport(logDir);        	
        	return;
        }
        if (mode == TEST_MODE || mode == CHECK_MODE) {
            masterIndex = Generator.generateXsl(setupOpts);
            if (indexFile != null) {
                masterIndex.persist(indexFile);
            }
        // begin 2011-06-30 PwD 
        } else if (mode == REDO_FROM_CACHE_MODE) {
        	boolean regenerate = false;
        	if (indexFile.canRead()) {
        		masterIndex = new Index(indexFile);
        		if (masterIndex.outOfDate()) {
        			System.out.println("Warning: Scripts have changed since this session was first executed.");
        			regenerate = true;
        		}
        	} else {
        		System.out.println("Error: Can't read index file.");
        		regenerate = true;
        	}
        	if (regenerate) {
        		System.out.println("Regenerating masterIndex from source scripts");
                masterIndex = Generator.generateXsl(setupOpts);
                if (indexFile != null) {
                    masterIndex.persist(indexFile);
                }		
        	}
        // end 2011-06-30 PwD
        } else {
            if (!indexFile.canRead()) {
              System.out.println("Error: Can't read index file.");
              return;
            }
            masterIndex = new Index(indexFile);
            if (masterIndex.outOfDate()) {
                System.out.println("Warning: Scripts have changed since this session was first executed.");
            }
        }

        // begin 2011-06-27 PwD
        if (mode == REDO_FROM_CACHE_MODE) {
    		File stylesheet = Misc.getResourceAsFile("com/occamlab/te/web/viewlog.xsl");
    		Templates ViewLogTemplates = ViewLog.transformerFactory.newTemplates(new StreamSource(stylesheet));
    	    ArrayList tests = new ArrayList();
    	    File userlog = logDir;
    	    StringWriter sw= new StringWriter();
    	    boolean complete = ViewLog.view_log(userlog, session, tests, ViewLogTemplates, sw);     
    	    boolean hasCache = ViewLog.hasCache();
    	    if (!hasCache) {
                File dir = new File(logDir, runOpts.getSessionId());
    	    	throw new Exception("Error: no cache for " + dir.getAbsolutePath());
    	    }
        }
        // end 2011-06-27 PwD
        
        masterIndex.setElements(null);
        
        TEClassLoader cl = new TEClassLoader(null); 
        Engine engine = new Engine(masterIndex, setupOpts.getSourcesName(), cl);
        
        if (setupOpts.isPreload() || mode == CHECK_MODE) {
            engine.preload(masterIndex, setupOpts.getSourcesName());
        }
        
        if (mode != CHECK_MODE) {
        	TECore core = new TECore(engine, masterIndex, runOpts);
            core.execute();
        }
       
    }
}
