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

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Handles errors arising in the course of generating an executable test suite
 * from CTL source files.
 * 
 */
public class TeErrorListener implements ErrorListener {
    private char[] scriptChars = null;
    private int ErrorCount = 0;
    private int WarningCount = 0;
    private boolean active = true;
    private static Logger logger = Logger.getLogger("com.occamlab.te.TeErrorListener");

    public TeErrorListener() {
    }

    public TeErrorListener(char[] script_chars) {
        scriptChars = script_chars;
    }

    public int getErrorCount() {
        return ErrorCount;
    }

    public int getWarningCount() {
        return WarningCount;
    }

    private void error(String type, TransformerException exception) {
        if (scriptChars == null) {
            if (active) {
                System.err.println(type + ": " + exception.getMessageAndLocation());
            }
            return;
        }
        
        try {
            String systemId = exception.getLocator().getSystemId();
            BufferedReader in;
            if (systemId.length() == 0) {
                in = new BufferedReader(new CharArrayReader(scriptChars));
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
                    location = "line " + line.substring(pos + 8, comma)
                            + " of " + line.substring(comma + 1, end_quote);
                }
                if (current_line >= txsl_linenum) {
                    closed = (line.indexOf(">") > 0);
                }
                current_line++;
            }
            System.err.println(type + " at " + location + ":");
            System.err.println("  " + exception.getMessage()
                    + " in intermediate stylesheet"
                    + exception.getLocationAsString());
        } catch (Exception e) {
            logger.log(Level.SEVERE,"",e);

            System.err.println(type + ": " + exception.getMessageAndLocation());
        }
    }

    public void error(TransformerException exception) {
        error("Error", exception);
        ErrorCount++;
    }

    public void fatalError(TransformerException exception) {
        error("Fatal Error", exception);
        ErrorCount++;
    }

    public void warning(TransformerException exception) {
        error("Warning Error", exception);
        WarningCount++;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
