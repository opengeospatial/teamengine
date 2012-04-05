package com.occamlab.te.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import sun.net.www.protocol.http.HttpURLConnection;
/*
 * sun.net.www.protocol.http.HttpURLConnection is not accessible from the 
 * Java Run-time Environment (JRE) rt.jar.  
 * It and other sun.www.protocol.* classes have been extracted into sun-rt.jar
 * which is copied to TeamEngineWMTS/apps/engine/lib to support this class.
 */

/**
 * Class of utilities for URL Connections, Java 7 (JDK 1.7.0) and later.
 * 
 * @author Paul Daisey (Image Matters LLC)
 *
 */
public class URLConnectionUtils {

	static {
		// enable error stream buffering
    	try {
	    	String eb =  System.setProperty("sun.net.http.errorstream.enableBuffering", "true");
	    	String est = System.setProperty("sun.net.http.errorstream.timeout", "600");
	    	// String esb = System.getProperty("sun.net.http.errorstream.bufferSize");
    	} catch (Exception e) {
    		System.err.println("Error setting System properties " + e.getMessage());
    		e.fillInStackTrace();
    		e.printStackTrace(System.err);
    	}
	}
	/**
	 * Get an input stream from a URL connection.  
	 * In case of an IOException, get the ErrorStream instead, if it is available.
	 * This makes it possible to obtain an ows:ExceptionReport when the server 
	 * returns HTTP code 400 Bad Request.
	 * 
	 * This method depends upon features of class sun.net.www.protocol.http.HttpURLConnection
	 * introduced in Java version 7 (JDK 1.7.0).  From its Javadoc:
	 * 
     * System properties related to error stream handling:
     *
     * sun.net.http.errorstream.enableBuffering = <boolean>
     *
     * With the above system property set to true (default is false),
     * when the response code is >=400, the HTTP handler will try to
     * buffer the response body (up to a certain amount and within a
     * time limit). Thus freeing up the underlying socket connection
     * for reuse. The rationale behind this is that usually when the
     * server responds with a >=400 error (client error or server
     * error, such as 404 file not found), the server will send a
     * small response body to explain who to contact and what to do to
     * recover. With this property set to true, even if the
     * application doesn't call getErrorStream(), read the response
     * body, and then call close(), the underlying socket connection
     * can still be kept-alive and reused. The following two system
     * properties provide further control to the error stream
     * buffering behaviour.
     *
     * sun.net.http.errorstream.timeout = <int>
     *     the timeout (in millisec) waiting the error stream
     *     to be buffered; default is 300 ms
     *
     * sun.net.http.errorstream.bufferSize = <int>
     *     the size (in bytes) to use for the buffering the error stream;
     *     default is 4k
     * @return InputStream from URLConnection if available, or ErrorStream, or null;
	 */
	public static InputStream getInputStream(URLConnection uc) throws IOException {
		IOException savedException = null;
		InputStream is = null;
		try {
			is = uc.getInputStream();
		} catch (IOException ioe) {
			savedException = ioe;
			// System.err.println(ioe);
		} finally {
			if (savedException != null) {
				try {
					if (uc instanceof sun.net.www.protocol.http.HttpURLConnection) {
    					InputStream errorStream = ((sun.net.www.protocol.http.HttpURLConnection)uc).getErrorStream();
    					is = errorStream;
					}
					
				} catch (Exception e) {
					// do nothing
				} finally{
					if (is == null) {
						throw savedException;
					}
				}
			} 
			return is;
		}
	}
}
