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

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLConnection;

import org.w3c.dom.Element;

public class CDataParser {

  public static String parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
    InputStream in = uc.getInputStream();
    byte[] buf = new byte[1024];
    int numread = in.read(buf);
    String s = new String(buf, 0, numread); 
    while (numread >= 0) {
      numread = in.read(buf);
      if (numread > 0) {
        s += new String(buf, 0, numread);
      }
    }
    return s;
  }
}
