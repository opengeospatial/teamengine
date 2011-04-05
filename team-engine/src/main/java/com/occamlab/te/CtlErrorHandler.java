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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Validation error handler that accumulates the total number of error and
 * warning notifications received.
 * 
 */
public class CtlErrorHandler implements ErrorHandler {
    int ErrorCount = 0;

    int WarningCount = 0;

    void error(String type, SAXParseException e) {
        System.err.println(type + " at line " + +e.getLineNumber()
                + ", column " + e.getColumnNumber() + " of " + e.getSystemId()
                + ":");
        System.err.println("  " + e.getMessage());
    }

    public int getErrorCount() {
        return ErrorCount;
    }

    public int getWarningCount() {
        return WarningCount;
    }

    public void error(SAXParseException exception) {
        error("Validation error", exception);
        ErrorCount++;
    }

    public void fatalError(SAXParseException exception) {
        error("Fatal validation error", exception);
        ErrorCount++;
    }

    public void warning(SAXParseException exception) {
        error("Validation warning", exception);
        WarningCount++;
    }
}
