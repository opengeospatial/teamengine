/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.config;

/**
 * Exception thrown when TE_BASE is not found.
 *
 * @author lbermudez
 *
 */
public class TEBaseNotFoundException extends TEConfigException {

	/**
	 * The <code>path</code> does not exist.
	 * @param path
	 */
	public TEBaseNotFoundException (String path){
		super (path);
	}

}
