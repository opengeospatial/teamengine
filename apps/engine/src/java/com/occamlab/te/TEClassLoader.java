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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

public class TEClassLoader extends ClassLoader {
    File resourcesDir;
    
    public TEClassLoader(File resourcesDir) {
        this.resourcesDir = resourcesDir; 
    }

    public URL getResource(String name) {
        URL u = super.getResource(name);
        if (resourcesDir != null && u == null) {
            File f = new File(resourcesDir, name);
            try {
                u = f.toURI().toURL();
            } catch (MalformedURLException e) {
            }
        }
        return u;
    }

    public InputStream getResourceAsStream(String name) {
        InputStream in = super.getResourceAsStream(name);
        if (resourcesDir != null && in == null) {
            try {
                return getResource(name).openStream();
            } catch (IOException e) {
            }
        }
        return in;
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> resources = super.getResources(name);
        URL u = getResource(name);
        if (resourcesDir != null && u != null) {
            Vector<URL> v = new Vector<URL>();
            while (resources.hasMoreElements()) {
                v.add(resources.nextElement());
            }
            v.add(u);
            resources = v.elements();
        }
        return resources;
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass(name, resolve, true);
    }
    
    public Class<?> loadClass(String name, boolean resolve, boolean defer) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null && defer) {
            c = findSystemClass(name);
        }
        if (c == null) {
            String filename = name.replace('.', '/') + ".class";
            try {
                InputStream in = getResourceAsStream(filename);
                ByteArrayOutputStream baos = new ByteArrayOutputStream(1024); 
                int i = in.read();
                while (i >= 0) {
                    baos.write(i);
                    i = in.read();
                }
                in.close();
//                System.out.println(name + " " + baos.size());
                c = defineClass(name, baos.toByteArray(), 0, baos.size());
            } catch (IOException ie) {
            }
        }
        if (c == null) {
            throw new ClassNotFoundException(name);
        } else {
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

}
