/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.config;

public class TEConfigException extends RuntimeException
{
    /**
     * Super class of all configuration exceptions
     */
    public TEConfigException()                                  { super(); }
	public TEConfigException(String message)                    { super(message); }
	public TEConfigException(Throwable cause)                   { super(cause) ; }
    public TEConfigException(String message, Throwable cause)   { super(message, cause) ; }
}
