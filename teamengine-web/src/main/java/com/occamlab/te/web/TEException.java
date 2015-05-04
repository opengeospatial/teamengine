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