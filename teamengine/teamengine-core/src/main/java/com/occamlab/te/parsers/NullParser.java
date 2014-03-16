/****************************************************************************

 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

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
 * Ignores the request information and returns <code>null</code>. This request
 * parser may be used to skip a request for some reason.
 * 
 */
public class NullParser {
    /*
     * public static String parse(HttpResponse resp, Element instruction,
     * PrintWriter logger) throws Exception { return null; }
     */
    public static String parse(URLConnection uc, Element instruction,
            PrintWriter logger) throws Exception {
        return null;
    }
}
