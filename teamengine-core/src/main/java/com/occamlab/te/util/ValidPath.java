package com.occamlab.te.util;

import java.util.logging.Logger;

/**
 * This utility was developed to address security vulnerabilities due to manipulation
 * of the file system path.  It restricts file system paths to those which are known 
 * to be safe.  The specific rules applied are described in-line below.  
 * 
 * @version August 01 2016
 * @author Charles Heazel
 */
public class ValidPath {

    private String path = null;
    private String tmpdir = null;
    private String te_install = null;
    private String te_base = null;
    private String te_build = null;
    private String user_home = null;
    private String separator = null;
    private static Logger jlogger = Logger.getLogger("com.occamlab.te.util.ValidPath");

    public ValidPath() {
    	// Make every effort to populate these strings with something 
    	// even if it is made up.
        path = new String();
        separator = System.getProperty("file.separator");
        if(separator == null) separator = "/";
        te_base = System.getProperty("TE_BASE");
        if(te_base == null) te_base = System.getenv("TE_BASE");
        if(te_base == null) te_base = "/TE_BASE";
        tmpdir = System.getProperty("java.io.tmpdir");
        if(tmpdir == null) tmpdir = te_base;
        te_install = System.getProperty("TE_INSTALL");
        if(te_install == null) te_install = System.getenv("TE_INSTALL");
        if(te_install == null) te_install = te_base;
        te_build = System.getenv("TE_BUILD");
        if(te_build == null) te_build = te_base;
        user_home = System.getenv("HOME");
        if(user_home == null) user_home = te_base;
        
        // Force windows first character to lower case
        if(tmpdir.startsWith("C:")) tmpdir = tmpdir.replaceFirst("^C:", "c:");
        if(te_install.startsWith("C:")) te_install = te_install.replaceFirst("^C:", "c:");
        if(te_base.startsWith("C:")) te_base = te_base.replaceFirst("^C:", "c:");
        if(te_build.startsWith("C:")) te_build = te_build.replaceFirst("^C:", "c:");
        if(user_home.startsWith("C:")) user_home = user_home.replaceFirst("^C:", "c:");
    }

    public boolean addElement( String arg1 ) {
        // NULL case - return true since nothing was done.
        if( arg1 == null) return(true);

        // convert the separators
        if(separator.equals("/")) arg1 = arg1.replace("\\", separator);
        if(separator.equals("\\")) arg1 = arg1.replace("/", separator);
        
        // Force windows first character to lower case
        if(arg1.startsWith("C:")) {
        	arg1 = arg1.replaceFirst("^C:", "c:");
        }
    	
        // IF a relative path, strip off the relative part
        while(arg1.startsWith(".")) arg1 = arg1.substring(1);

    	// IF the path is not empty and does not end with a separator, add one.
    	if(!path.isEmpty() && !path.endsWith(separator)){
    		path = path.concat(separator);
    	}

    	// Append the element.  
        path = path.concat( arg1 );
        
        // This comes in handy when a change to the code requires a change to the ValidPath rules.
        // jlogger.info("VALIDPATH Add Element = " + path);

    	  // Validate it.
        if(this.validate(path)) return(true);
        return(false);
    }

    public String getPath() {
    	// Only return a path if it is valid
    	if(this.validate(path)) return(path);
    	// If it is an invalid path, get rid of it
    	// Unfortunately Java does not allow us to destroy objects.  So the best we can do is dereference it.
    	path = new String();
        return(null);
    }
    
    public boolean isValid() {
    	if(this.validate(path)) return(true);
    	return(false);
    }
    
    private boolean validate( String arg1 ){

    	// Validate checks the path supplied in the argument against a set of rules
    	// for a valid path.  Changes to the source over time may require adjustments
    	// to these rules.

        // a null path is still valid
        if(arg1 == null) return(true);
        
    	// Restrict the path to one of the valid root directories
    	boolean valid = false;
    	if(arg1.startsWith(te_base)) valid = true; 
    	if(arg1.startsWith(te_install)) valid = true; 
    	if(arg1.startsWith(tmpdir)) valid = true;
    	if(arg1.startsWith(te_build)) valid = true;
    	
    	// No navigating up the directory tree
    	if(arg1.contains("..")) valid = false;
    	
    	// These are never allowed
    	if(arg1.startsWith("c:\\Windows")) valid = false; 
    	if(arg1.startsWith("/etc")) valid = false; 
    	if(arg1.startsWith("/bin")) valid = false; 
    	if(arg1.startsWith("/usr/bin")) valid = false; 
    	
    	if(valid == false){
           jlogger.warning("VALIDPATH Invalid Path: " + arg1);
    	}
    	return(valid);
    }
}

