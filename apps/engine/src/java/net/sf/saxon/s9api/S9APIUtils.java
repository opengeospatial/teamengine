/*
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
 */

/* This file is in the s9api package so it can access the protected XdmNode constructor */ 
package net.sf.saxon.s9api;

import net.sf.saxon.om.NodeInfo;

public class S9APIUtils {
  public static XdmNode makeNode(NodeInfo node) {
    return new XdmNode(node);
  }
  
  public static void setTransformerParam(XsltTransformer xt, String param, String value) throws SaxonApiException {
      setTransformerParam(xt, new QName(param), value);
  }

  public static void setTransformerParam(XsltTransformer xt, QName param, String value) throws SaxonApiException {
      xt.setParameter(param, XdmItem.newAtomicValue(value, ItemType.ANY_ATOMIC_VALUE));
  }
}
