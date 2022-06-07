package com.occamlab.te.util;

import static org.junit.Assert.*;

import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This junit test exercises the TEPath class. TEPath addresses security vulnerabilities
 * due to manipulation of the file system path.
 * 
 *  The specific rules applied by TEPath are:
 *   1) The path is restricted to one of the following root directories
 *        a) TE_BASE - path defined by the TE_BASE environment variable 
 *        b) TE_INSTALL - path defined by the TE_INSTALL environment variable 
 *        c) Java temp directory
 *        d) TE_BUILD - An environment variable which contains the path to this source tree  	
 *   2) No navigating up the directory tree
 *   3) These paths are never allowed
 *        a) c:\\Windows 
 *        b) /etc 
 *        c) /bin 
 *        d) /usr/bin  
 * 
 * @version Januiary 23 2018
 * @author Charles Heazel
 */

public class TEPathTest {

	// Define a set of valid and invalid test paths
	private static String validpath1 = null;
	private static String validpath2 = null;
	private static String validpath3 = null;
	private static String validpath4 = null;
	private static String validpath5 = null;
			
	private static String invalidpath1 = null;
	private static String invalidpath2 = null;
	private static String invalidpath3 = null;
	private static String invalidpath4 = null;
	

	@BeforeClass
	public static void initialize() {

        //  Use the same system properties and environment variables as TEPath.java
 
        String te_base = System.getProperty("TE_BASE");
        if(te_base == null) te_base = System.getProperty("java.io.tmpdir");
        String tmpdir = System.getProperty("java.io.tmpdir");
        if(tmpdir == null) tmpdir = te_base;
        String te_install = System.getProperty("TE_INSTALL");
        if(te_install == null) te_install = System.getenv("TE_INSTALL");
        if(te_install == null) te_install = te_base;
        String te_build = System.getenv("TE_BUILD");
        if(te_build == null) te_build = te_base;
        String user_home = System.getProperty("user.home");
        if(user_home == null) user_home = te_base;
        
        // Valid paths - not Operating System dependent
	validpath1 = te_base;
	validpath2 = tmpdir;
	validpath3 = te_install;
	validpath4 = user_home;

        // Invalid paths - not Operating System dependent 
        // (none currently defined)

        // Operating System dependent valid and invalid paths
       
	String separator = System.getProperty("file.separator");
	if(separator.equals("/")) {
            validpath5 = validpath1.concat("/foo-bar");  // Valid path name 
            invalidpath1 = validpath1.concat("/../etc"); // Navigation up the tree is not allowed
	    invalidpath2 = "/etc/foo-bar";               // Forbidden path
            invalidpath3 = "/bin/foo-bar";               // Forbidden path
            invalidpath4 = "/usr/bin/foo-bar";           // Forbidden path
            }
        if(separator.equals("\\")) {
            validpath5 = validpath1.concat("\\foo-bar");        // Valid path name 
            invalidpath1 = validpath4.concat("\\..\\Windows");  // Navigation up the tree is not allowed
            invalidpath2 = "c:\\Windows\\Boot";                 // Forbidden path
            invalidpath3 = "C:\\Windows\\foo-bar";              // Forbidden path
            invalidpath4 = "c:\\Windows\\foo-bar";              // Forbidden path
            }
	}

	@Test
	public void testToString() {
		String retpath = null;
		TEPath vpath = new TEPath(validpath1);
		retpath = vpath.toString();
		assertFalse("Expected non-empty TEPath.toString() return for " + validpath1, retpath.isEmpty());
		TEPath iv_vpath = new TEPath(invalidpath2);
		retpath = iv_vpath.toString();
		assertTrue("Expected empty for TEPath.toString() for " + invalidpath2, retpath.isEmpty());
	}

	@Test
	public void testIsValid() {
		TEPath vpath1 = new TEPath(validpath1);
		assertTrue("Expected true for TEPath.isValid() for " + validpath1, vpath1.isValid());

		TEPath vpath2 = new TEPath(validpath2);
		assertTrue("Expected true for TEPath.isValid() for " + validpath2, vpath2.isValid());

		TEPath vpath3 = new TEPath(validpath3);
		assertTrue("Expected true for TEPath.isValid() for " + validpath3, vpath3.isValid());

		TEPath vpath4 = new TEPath(validpath4);
		assertTrue("Expected true for TEPath.isValid() for " + validpath4, vpath4.isValid());

		TEPath vpath5 = new TEPath(validpath5);
		assertTrue("Expected true for TEPath.isValid() for " + validpath5, vpath5.isValid());

                TEPath iv_vpath1 = new TEPath(invalidpath1);
		assertFalse("Expected false for TEPath.isValid() for " + invalidpath1, iv_vpath1.isValid());

		TEPath iv_vpath2 = new TEPath(invalidpath2);
		assertFalse("Expected false for TEPath.isValid() for " + invalidpath2, iv_vpath2.isValid());

		TEPath iv_vpath3 = new TEPath(invalidpath3);
		assertFalse("Expected false for TEPath.isValid() for " + invalidpath3, iv_vpath3.isValid());

		TEPath iv_vpath4 = new TEPath(invalidpath4);
		assertFalse("Expected false for TEPath.isValid() for " + invalidpath4, iv_vpath4.isValid());
	}
}

