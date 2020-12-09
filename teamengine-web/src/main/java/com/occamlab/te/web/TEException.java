/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.web;

public class TEException extends RuntimeException
{
    /**
     * Super class of all exceptions in TE web
     */
    public TEException()                                  { super(); }
	public TEException(String message)                    { super(message); }
	public TEException(Throwable cause)                   { super(cause) ; }
    public TEException(String message, Throwable cause)   { super(message, cause) ; }
}
