/****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s):
 Andreas Schmitz
 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 ****************************************************************************/
package com.occamlab.te.util.protocols.data;

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

import static java.net.URLDecoder.decode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.xerces.impl.dv.util.Base64;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * @version $Revision$, $Date: 2010-08-09 00:08:36 -0700 (Mon, 09 Aug 2010) $
 */
public class Handler extends URLStreamHandler {

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		String[] ss = u.getPath().split(",", 2);
		final String data = ss[1];
		ss = cleanup(ss[0].split(";"));

		String mime = "text/plain";
		String charset = "US-ASCII";
		String encoding = null;

		if (ss.length == 3) {
			mime = ss[0];
			charset = ss[1].split("=")[1];
			encoding = ss[2];
		}
		else if (ss.length == 2) {
			if (ss[0].startsWith("charset")) {
				charset = ss[0].split("=")[1];
				encoding = ss[1];
			}
			else {
				mime = ss[0];
				if (ss[1].startsWith("charset")) {
					charset = ss[1].split("=")[1];
				}
				else {
					encoding = ss[1];
				}
			}
		}
		else if (ss.length == 1) {
			if (ss[0].startsWith("charset")) {
				charset = ss[0].split("=")[1];
			}
			else {
				encoding = ss[0];
			}
		}
		else if (ss.length > 0) {
			System.out.println("too many fields");
		}

		if (encoding != null && !encoding.equalsIgnoreCase("base64")) {
			throw new IOException("The '" + encoding + "' encoding is not supported by the data URL. "
					+ "Only base64 and the default of url-encoded is allowed.");
		}

		final String contentType = mime.length() == 0 ? "text/plain" : mime;
		final String characterEncoding = charset;

		if (encoding == null) {
			return new URLConnection(u) {
				InputStream in;

				{
					connect();
				}

				@Override
				public void connect() throws IOException {
					in = new ByteArrayInputStream(decode(data, characterEncoding).getBytes(StandardCharsets.UTF_8));
				}

				@Override
				public InputStream getInputStream() {
					return in;
				}

				@Override
				public String getContentEncoding() {
					return "UTF-8";
				}

				@Override
				public String getContentType() {
					return contentType;
				}
			};
		}

		return new URLConnection(u) {
			InputStream in;

			{
				connect();
			}

			@Override
			public void connect() throws IOException {
				in = new ByteArrayInputStream(Base64.decode(data));
			}

			@Override
			public InputStream getInputStream() {
				return in;
			}

			@Override
			public String getContentEncoding() {
				return characterEncoding;
			}

			@Override
			public String getContentType() {
				return contentType;
			}
		};
	}

	private static String[] cleanup(String[] ss) {
		ArrayList<String> list = new ArrayList<>();
		for (String s : ss) {
			if (s.length() > 0) {
				list.add(s);
			}
		}
		return list.toArray(new String[list.size()]);
	}

}
