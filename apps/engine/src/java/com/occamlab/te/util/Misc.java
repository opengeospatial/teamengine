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

package com.occamlab.te.util;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class Misc {

    // Deletes a directory and its contents
    public static void deleteDir(File dir) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            File f = new File(dir, children[i]);
            if (f.isDirectory()) {
                deleteDir(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }

    // Deletes just the sub directories for a certain directory
    public static void deleteSubDirs(File dir) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            File f = new File(dir, children[i]);
            if (f.isDirectory()) {
                deleteDir(f);
            }
        }
    }

    // Loads a file into memory from the classpath
    public static File getResourceAsFile(String resource) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            return new File(URLDecoder.decode(cl.getResource(resource).getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }

    // Loads a DOM Document from the classpath
    Document getResourceAsDoc(String resource) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(resource);
        if (is != null) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            Document doc = db.newDocument();
            t.transform(new StreamSource(is), new DOMResult(doc));
            return doc;
        } else {
            return null;
        }
    }

    // Returns the URL for file on the classpath as a String
    public static String getResourceURL(String resource) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(resource).toString();
    }
}
