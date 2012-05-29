package com.occamlab.te;

import java.io.File;

import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;

/**
 * Provides utility methods for managing a collection of CTL test suites.
 * 
 */
public class ListSuites {
    
    public static void main(String[] args) throws Exception {
        SetupOptions setupOpts = new SetupOptions();

        boolean sourcesSupplied = false;
        String cmd = "java com.occamlab.te.ListSuites";
        File workDir = null;

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
                setupOpts.setWorkDir(workDir);
            }
        }
        
        if (workDir == null) {
            workDir = new File(System.getProperty("java.io.tmpdir"), "te_work");
            workDir.mkdirs();
            System.out.println("No working directory supplied.  Using " + workDir.toString());
        } else {
            if (!workDir.isDirectory()) {
                System.out.println("Error: Working directory " + workDir.toString() + " does not exist.");
                return;
            }
        }

        if (!sourcesSupplied) {
            System.out.println(cmd + " [-workdir=dir] -source=ctlfile|dir [-source=ctlfile|dir] ...");
            return;
        }

        Index index = Generator.generateXsl(setupOpts);
        
        for (String suiteId : index.getSuiteKeys()) {
            SuiteEntry suite = index.getSuite(suiteId);
            System.out.print("Suite " + suite.getPrefix() + ":" + suite.getLocalName());
            System.out.println(" (" + suiteId + ")");
            System.out.println(suite.getTitle());
            String desc = suite.getDescription();
            if (desc != null) {
                System.out.println(desc);
            }
            String link = suite.getLink();
            if (link != null) {
                System.out.println("See " + link);
            }
            System.out.println();
        }
    }
}
