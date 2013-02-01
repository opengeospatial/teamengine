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

        // Parse source command-line argument
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-source=")) {
                File scriptsDir = new File(
                        SetupOptions.getBaseConfigDirectory(), "scripts");
                File f = new File(scriptsDir, args[i].substring(8));
                if (f.exists()) {
                    setupOpts.addSource(f);
                } else {
                    System.out.println("Error: Can't find CTL script(s) at "
                            + f.getAbsolutePath());
                    return;
                }
            }
        }

        Index index = Generator.generateXsl(setupOpts);
        for (String suiteId : index.getSuiteKeys()) {
            SuiteEntry suite = index.getSuite(suiteId);
            System.out.print("Suite " + suite.getPrefix() + ":"
                    + suite.getLocalName());
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
