/****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.parsers;

import java.net.URLConnection;
import java.io.PrintWriter;

import org.w3c.dom.Element;

/**
 * Ignores the request information and returns <code>null</code>. This request parser may
 * be used to skip a request for some reason.
 *
 */
public class NullParser {

	public static String parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
		return null;
	}

}
