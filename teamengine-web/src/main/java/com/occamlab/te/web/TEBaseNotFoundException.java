package com.occamlab.te.web;

import java.nio.file.Path;

/**
 * Exception thrown when TE_BASE is not found.
 * 
 * @author lbermudez
 *
 */
public class TEBaseNotFoundException extends TEException {
	
	/**
	 * The <code>path</code> does not exist.
	 * @param name
	 */
	public TEBaseNotFoundException (String path){
		super (path);
	}

}
