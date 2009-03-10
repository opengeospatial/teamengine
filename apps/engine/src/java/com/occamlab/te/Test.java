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

import com.occamlab.te.index.Index;
import com.occamlab.te.util.LogUtils;

public class Test {
    public static final int TEST_MODE = 0;
    public static final int RETEST_MODE = 1;
    public static final int RESUME_MODE = 2;
    public static final int DOC_MODE = 4;
    public static final int CHECK_MODE = 5;

    public static final String XSL_NS = "http://www.w3.org/1999/XSL/Transform";
    public static final String TE_NS = "http://www.occamlab.com/te";
    public static final String CTL_NS = "http://www.occamlab.com/ctl";
    
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
//        System.out.println("Doc mode:");
//        System.out.println("  Use to generate a list of assertions.\n");
//        System.out.println("  " + cmd + " -mode=doc -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
//        System.out.println("    [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
    }

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
//                setupOpts.setWorkDir(workDir);
//                runOpts.setWorkDir(workDir);
            } else if (args[i].startsWith("-logdir=")) {
                logDir = new File(args[i].substring(8));
//                runOpts.setLogDir(logDir);
            } else if (args[i].startsWith("-session=")) {
                session = args[i].substring(9); 
//                runOpts.setSessionId(session);
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
//        setupOpts.setMode(mode);
        runOpts.setMode(mode);
        
        // Syntax checks
        if ((mode == TEST_MODE && !sourcesSupplied) ||
            (mode == RETEST_MODE && (logDir == null || session == null)) ||
            (mode == RESUME_MODE && (logDir == null || session == null))
            ) {
            syntax(cmd);
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
        if (session == null) {
            if (logDir == null) {
                session = "s0001";
            } else {
                session = LogUtils.generateSessionId(logDir);
            }
        }
        runOpts.setSessionId(session);

        Thread.currentThread().setName("TEAM Engine");
        
        Index masterIndex;
        File indexFile = null;
        if (logDir != null && session != null) {
            File dir = new File(logDir, runOpts.getSessionId());
            indexFile = new File(dir, "index.xml");
        }

        if (mode == TEST_MODE || mode == CHECK_MODE) {
            masterIndex = Generator.generateXsl(setupOpts);
            if (indexFile != null) {
                masterIndex.persist(indexFile);
            }
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
            
        masterIndex.setElements(null);
        
        Engine engine = new Engine(masterIndex);
        
        if (setupOpts.isPreload() || mode == CHECK_MODE) {
            engine.preload(masterIndex, setupOpts.getSourcesName());
        }
        
        if (mode != CHECK_MODE) {
            TECore core = new TECore(engine, masterIndex, runOpts);
            core.execute();
//            engine.execute(runOpts, masterIndex, System.out, false);
        }
    }
}
