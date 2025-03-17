/****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s):
     C. Heazel (WiSC) Fortify modifications

 ****************************************************************************/
package com.occamlab.te.parsers;

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

import java.io.InputStream;
import java.io.PrintWriter;

import java.net.URLConnection;

import org.w3c.dom.Element;

/**
 * Reads a response message and produces a String representation of its content.
 *
 */
public class CDataParser {

	public static String parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
		// Fortify Mod: manage the input stream as a local variable so that it can be
		// closed.
		// return parse(uc.getInputStream(), instruction, logger);
		InputStream is = uc.getInputStream();
		String s = parse(is, instruction, logger);
		is.close();
		return s;
	}

	private static String parse(InputStream is, Element instruction, PrintWriter logger) throws Exception {
		byte[] buf = new byte[1024];
		int numread = is.read(buf);
		String s = new String(buf, 0, numread);
		while (numread >= 0) {
			numread = is.read(buf);
			if (numread > 0) {
				s += new String(buf, 0, numread);
			}
		}
		return s;
	}

}
