/****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * Ignores all errors arising during a transformation process.
 * 
 */
public class NullErrorListener implements ErrorListener {

    public void error(TransformerException exception) {
    }

    public void fatalError(TransformerException exception) {
    }

    public void warning(TransformerException exception) {
    }
}
