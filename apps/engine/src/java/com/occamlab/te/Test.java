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
            System.out.println("  " + cmd + " -mode=retest -logdir=dir testpath1 [testpath2] ...\n");
            System.out.println("Doc mode:");
            System.out.println("  Use to generate a list of assertions.\n");
            System.out.println("  " + cmd + " -mode=doc -source={ctlfile|dir} [-source={ctlfile|dir}] ...");
            System.out.println("    [-suite=[{namespace_uri,|prefix:}]suite_name]\n");
            return;
        }

        Thread.currentThread().setName("TEAM Engine");
        
        Generator.generateXsl(setupOpts);
        
        Execute.prepareSaxon();

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
            Execute.preload();
        }
        
        if (mode != CHECK_MODE) {
            Execute.execute(runOpts, System.out, false);
        }
    }
}
