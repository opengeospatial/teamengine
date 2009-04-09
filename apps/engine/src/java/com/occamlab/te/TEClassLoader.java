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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

public class TEClassLoader extends ClassLoader {
    File resourcesDir;
    ClassLoader cl;
    HashSet<String> registeredClasses; 
    
    public TEClassLoader(File resourcesDir) {
        this.resourcesDir = resourcesDir;
        cl = Thread.currentThread().getContextClassLoader();
        registeredClasses = new HashSet<String>();
    }

    public URL getResource(String name) {
        if (resourcesDir != null) {
            File f = new File(resourcesDir, name);
            try {
                return f.toURI().toURL();
            } catch (MalformedURLException e) {
            }
        }
        return cl.getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        if (resourcesDir != null) {
            URL u = getResource(name);
            if (u != null) {
                try {
                    return u.openStream();
                } catch (IOException e) {
                }
            }
        }
        return cl.getResourceAsStream(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> resources = cl.getResources(name);
        URL u = getResource(name);
        if (resourcesDir != null && u != null) {
            Vector<URL> v = new Vector<URL>();
            v.add(u);
            while (resources.hasMoreElements()) {
                v.add(resources.nextElement());
            }
            return v.elements();
        }
        return resources;
    }
    
    public void registerClass(String name) {
        if (!registeredClasses.contains(name)) {
            registeredClasses.add(name);
        }
    }

    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            for (String registeredClass : registeredClasses) {
                if (name.startsWith(registeredClass)) {
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
                        c = defineClass(name, baos.toByteArray(), 0, baos.size());
                        break;
                    } catch (Exception e) {
                    }
                }
            }
        }
        if (c == null) {
            c = cl.loadClass(name);
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
