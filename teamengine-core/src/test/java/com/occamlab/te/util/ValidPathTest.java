package com.occamlab.te.util;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This junit test exercises the ValidPath class ValidPath addresses security vulnerabilities
 * due to manipulation of the file system path. 
 * 
 * @version July 30 2016
 * @author Charles Heazel
 */

public class ValidPathTest {
	// Determine if we are using Windows or Unix path names
	// Select the appropriate set of test paths
	private static String test1 = null;
	private static String test2 = null;
	private static String test3 = null;
	private static String test4 = null;

	@BeforeClass
	public static void initialize() {
		String separator = System.getProperty("file.separator");
		if(separator.equals("/")) {
			test1 = "/TE_BASE";
			test2 = "/etc/init.d";      // Path root is restricted
			test3 = "/TE_BASE/../etc";  // Navigation up the tree is not allowed
			test4 = "/usr/tmp/foo-bar"; // Valid path name
		}
		if(separator.equals("\\")) {
			test1 = "c:\\TE_BASE";
			test2 = "c:\\Windows";              // Path root is restricted
			test3 = "c\\TE_BASE\\..\\Windows";  // Navigation up the tree is not allowed
			test4 = "c:\\TE_BASE\\foo-bar";        // Valid path name
		}
	}

	@Test
	public void testAddElement() {
		ValidPath vpath = new ValidPath();
		vpath.addElement(test1);
		vpath.addElement("foo-bar");
		boolean valid = vpath.isValid();
		assertTrue("Expected ValidPath should be " + test4 + ".", valid);
	}

	@Test
	public void testGetPath() {
		ValidPath vpath = new ValidPath();
		vpath.addElement(test2);
		assertNull("Expected null for ValidPath.getPath() for " + test2, vpath.getPath());
	}

	@Test
	public void testIsValid() {
		ValidPath vpath1 = new ValidPath();
		vpath1.addElement(test2);
		assertFalse("Expected false for ValidPath.isValid() for " + test2, vpath1.isValid());
		ValidPath vpath2 = new ValidPath();
		vpath2.addElement(test3);
		assertFalse("Expected false for ValidPath.isValid() for " + test3, vpath2.isValid());
	}
}

