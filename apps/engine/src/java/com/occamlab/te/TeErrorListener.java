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
package com.occamlab.te;

import javax.xml.transform.*;
import java.io.*;
import java.net.URI;

public class TeErrorListener implements ErrorListener {
	private char[] ScriptChars;
	private int ErrorCount = 0;
	private int WarningCount = 0;
	
	
	public TeErrorListener(char[] script_chars) {
		ScriptChars = script_chars;
	}

	public int getErrorCount() {
		return ErrorCount;
	}

	public int getWarningCount() {
		return WarningCount;
	}

	private void error(String type, TransformerException exception) {
		try {
			String systemId = exception.getLocator().getSystemId();
			BufferedReader in;
			if (systemId.length() == 0) {
				in = new BufferedReader(new CharArrayReader(ScriptChars));
			} else {
   		  File txsl_file = new File(new URI(systemId));
    		in = new BufferedReader(new FileReader(txsl_file));
			}
	  	int txsl_linenum = exception.getLocator().getLineNumber();
  		String line;
  		String location = "unknown location";
  		int current_line = 1;
  		boolean closed = true;
  		while (current_line <= txsl_linenum || !closed) {
  			line = in.readLine();
        if (line == null) {
          location = "unknown location";
          break;
        }
  			int pos = line.indexOf("te:loc=\"");
  			if (pos >= 0) {
  				int comma = line.indexOf(",", pos);
  				int end_quote = line.indexOf("\"", comma);
  				location = "line " + line.substring(pos + 8, comma) + " of " + line.substring(comma + 1, end_quote);
  			}
        if (current_line >= txsl_linenum) {
          closed = (line.indexOf(">") > 0);
        }
  			current_line++;
  		}
      System.err.println(type + " at " + location + ":");
      System.err.println("  " + exception.getMessage() + " in intermediate stylesheet" + exception.getLocationAsString());
		} catch (Exception e) {
      System.err.println(type + ": " + exception.getMessageAndLocation());
		}
  }

	public void error(TransformerException exception) {
//    System.out.println("TE Error: " + exception.getMessageAndLocation() + "\n.\n");
		error("Error", exception);
		ErrorCount++;
  }

  public void fatalError(TransformerException exception) {
//    System.out.println("TE Fatal Error: " + exception.getMessageAndLocation() + "\n.\n");
		error("Fatal Error", exception);
		ErrorCount++;
  }

  public void warning(TransformerException exception) {
//    System.out.println("TE Warning: " + exception.getMessageAndLocation() + "\n.\n");
		error("Warning Error", exception);
		WarningCount++;
  }
}
