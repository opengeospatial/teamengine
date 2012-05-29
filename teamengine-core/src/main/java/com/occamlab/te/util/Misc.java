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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.occamlab.te.TEClassLoader;

public class Misc {

    // Deletes a directory and its contents
    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            deleteDirContents(dir);
            dir.delete();
        }
    }

    // Deletes the contents of a directory
    public static void deleteDirContents(File dir) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            File f = new File(dir, children[i]);
            if (f.isDirectory()) {
                deleteDirContents(f);
            }
            f.delete();
        }
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
    
    public static Method getMethod(String className, String methodName, TEClassLoader cl, int minArgs, int maxArgs) throws Exception {
//        cl.registerClass(className);
        Class c = Class.forName(className, true, cl);
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            int count = m.getParameterTypes().length;
            if (m.getName().equals(methodName) && count >= minArgs && count <= maxArgs) {
                return m;
            }
        }
        String argsDesc = Integer.toString(minArgs);
        if (maxArgs > minArgs) {
            argsDesc += " to " + Integer.toString(maxArgs) + " argument";
        }
        if (minArgs > 1 || maxArgs > 1) {
            argsDesc += "s";
        }
        throw new Exception("Error: Method " + methodName + " with " + argsDesc + " was not found in class " + className);
    }

    public static Method getMethod(String className, String methodName, TEClassLoader cl, int argCount) throws Exception {
        return getMethod(className, methodName, cl, argCount, argCount);
    }
    
//    public static Method getMethod(String className, String methodName, int argCount) throws Exception {
//        Class c = Class.forName(className);
//        Method[] methods = c.getMethods();
//        for (int i = 0; i < methods.length; i++) {
//            Method m = methods[i];
//            int count = m.getParameterTypes().length;
//            if (m.getName().equals(methodName) && count == argCount) {
//                return m;
//            }
//        }
//        throw new Exception("Error: Method " + methodName + " with " + Integer.toString(argCount) + " arguments was not found in class " + className);
//    }
    
    public static Object makeInstance(String className, List<Node> classParams, TEClassLoader cl) throws Exception {
//        cl.registerClass(className);
        Class c = Class.forName(className, true, cl);
        Constructor[] constructors = c.getConstructors();
        int paramCount = 0;
        if (classParams != null) {
            paramCount = classParams.size();
        }
        Object[] classParamObjects = new Object[paramCount];
        for (int i = 0; i < constructors.length; i++) {
            Class<?>[] types = constructors[i].getParameterTypes();
            if (types.length == paramCount) {
                boolean constructorCorrect = true;
                for (int j = 0; j < types.length; j++) {
                    Node n = classParams.get(j);
                    if (Document.class.isAssignableFrom(types[j])) {
                    	if (n instanceof Document) {
                    		classParamObjects[j] = (Document)n;
                    	} else {
                            classParamObjects[j] = DomUtils.createDocument(n);
                    	}
                    } else if (Node.class.isAssignableFrom(types[j])) {
                        classParamObjects[j] = n;
                    } else if (types[j].equals(String.class)) {
                        classParamObjects[j] = n.getTextContent();
                    } else if (types[j].toString().equals("char")) {
                        classParamObjects[j] = n.getTextContent().charAt(0);
                    } else if (types[j].toString().equals("boolean")) {
                        classParamObjects[j] = Boolean.parseBoolean(n.getTextContent());
                    } else if (types[j].toString().equals("byte")) {
                        classParamObjects[j] = Byte.parseByte(n.getTextContent());
                    } else if (types[j].toString().equals("short")) {
                        classParamObjects[j] = Short.parseShort(n.getTextContent());
                    } else if (types[j].toString().equals("int")) {
                        classParamObjects[j] = Integer.parseInt(n.getTextContent());
                    } else if (types[j].toString().equals("float")) {
                        classParamObjects[j] = Float.parseFloat(n.getTextContent());
                    } else if (types[j].toString().equals("double")) {
                        classParamObjects[j] = Double.parseDouble(n.getTextContent());
                    } else {
                        constructorCorrect = false;
                        break;
                    }
                }
                if (constructorCorrect) {
                    return constructors[i].newInstance(classParamObjects);
                }
            }
        }
        throw new Exception("No compatible constructors found in class " + className);
    }
}
