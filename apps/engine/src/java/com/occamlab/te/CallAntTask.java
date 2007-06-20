package com.occamlab.te;

import java.lang.ClassLoader;
import java.net.URL;
import java.lang.Thread;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.helper.ProjectHelper2;

/**
 * Creates an Ant project and invokes the given target in the supplied build
 * file.
 */
public class CallAntTask {

    /**
     * Calls an ant task using the ant java library.
     * 
     * @param filename
     *            the resource location (on classpath) of the build file to run
     * @param target
     *            the name of the target to run
     */
    public void callAnt(String filename, String target) {
        // Create a new project object, ProjectHelper2 is the newer
        // implementation
        Project ant = new Project();
        // ProjectHelper helper = new ProjectHelperImpl();
        ProjectHelper helper = new ProjectHelper2();

        // Add a logger to display messages
        DefaultLogger log = new DefaultLogger();
        log.setErrorPrintStream(System.err);
        log.setOutputPrintStream(System.out);
        log.setMessageOutputLevel(Project.MSG_INFO);
        ant.addBuildListener(log);

        ant.init();

        // Load the build file
        File f = null;
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource(filename);
            f = new File(url.getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Read in the build file and execute
        helper.parse(ant, f);
        ant.executeTarget(target);
    }

}
