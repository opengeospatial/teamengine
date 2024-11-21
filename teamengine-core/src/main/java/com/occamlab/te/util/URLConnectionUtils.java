package com.occamlab.te.util;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class of utilities for URL Connections, Java 7 (JDK 1.7.0) and later.
 *
 * @author Paul Daisey (Image Matters LLC)
 *
 */
public class URLConnectionUtils {

	private static final Logger LOGR = Logger.getLogger(URLConnectionUtils.class.getName());

	static {
		try {
			System.setProperty("sun.net.http.errorstream.enableBuffering", "true");
			System.setProperty("sun.net.http.errorstream.timeout", "600");
		}
		catch (Exception e) {
			LOGR.warning("Failed to enable buffering of error stream.\n" + e.getMessage());
			e.fillInStackTrace();
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Get an input stream from a URL connection. In case of an IOException, get the
	 * ErrorStream instead, if it is available. This makes it possible to obtain an
	 * ows:ExceptionReport when the server returns HTTP code 400 Bad Request.
	 *
	 * This method depends upon features of class
	 * sun.net.www.protocol.http.HttpURLConnection introduced in Java version 7 (JDK
	 * 1.7.0). From its Javadoc:
	 *
	 * System properties related to error stream handling:
	 *
	 * sun.net.http.errorstream.enableBuffering = <boolean>
	 *
	 * With the above system property set to true (default is false), when the response
	 * code is >=400, the HTTP handler will try to buffer the response body (up to a
	 * certain amount and within a time limit). Thus freeing up the underlying socket
	 * connection for reuse. The rationale behind this is that usually when the server
	 * responds with a >=400 error (client error or server error, such as 404 file not
	 * found), the server will send a small response body to explain who to contact and
	 * what to do to recover. With this property set to true, even if the application
	 * doesn't call getErrorStream(), read the response body, and then call close(), the
	 * underlying socket connection can still be kept-alive and reused. The following two
	 * system properties provide further control to the error stream buffering behaviour.
	 *
	 * sun.net.http.errorstream.timeout = <int> the timeout (in millisec) waiting the
	 * error stream to be buffered; default is 300 ms
	 *
	 * sun.net.http.errorstream.bufferSize = <int> the size (in bytes) to use for the
	 * buffering the error stream; default is 4k
	 * @return InputStream from URLConnection if available, or the error stream if the
	 * connection failed but the server sent useful data, or null.
	 */
	public static InputStream getInputStream(URLConnection uc) throws IOException {
		InputStream is = null;
		try {
			is = uc.getInputStream();
		}
		catch (IOException ioe) {
			if (LOGR.isLoggable(Level.FINE)) {
				String msg = String.format("Failed to resolve URL %s.\nTrying error stream...\n %s", uc.getURL(),
						ioe.getMessage());
				LOGR.fine(msg);
			}
			if (HttpURLConnection.class.isInstance(uc)) {
				HttpURLConnection huc = (HttpURLConnection) uc;
				is = huc.getErrorStream();
			}
		}
		return is;
	}

}
