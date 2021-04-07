/*

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s): No additional contributors to date
 */

/* This file is in the s9api package so it can access the protected XdmNode constructor */
package net.sf.saxon.s9api;

import net.sf.saxon.om.NodeInfo;

public class S9APIUtils {
    public static XdmNode makeNode(NodeInfo node) {
        return new XdmNode(node);
    }

    public static void setTransformerParam(XsltTransformer xt, String param,
            String value) throws SaxonApiException {
        setTransformerParam(xt, new QName(param), value);
    }

    public static void setTransformerParam(XsltTransformer xt, QName param,
            String value) throws SaxonApiException {
        xt.setParameter(param,
        		new XdmAtomicValue(value, ItemType.ANY_ATOMIC_VALUE));
    }
}
