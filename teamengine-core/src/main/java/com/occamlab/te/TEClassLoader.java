/****************************************************************************

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
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TEClassLoader extends ClassLoader {
	List<File> resourcesDirs;
    ClassLoader cl;
    HashSet<String> registeredClasses;
    private static Logger logger = Logger
            .getLogger("com.occamlab.te.TEClassLoader");
    
    private static List<File> resourcesDirsList(File resourcesDir) {
    	List<File> resourcesDirs = new ArrayList<>();
    	if (resourcesDir != null) {
    		resourcesDirs.add(resourcesDir);
    	}
    	return resourcesDirs;
    }
    
    public TEClassLoader() {
    	this(resourcesDirsList(null));
    }
    
    public TEClassLoader(File resourcesDir) {
    	this(resourcesDirsList(resourcesDir));
    }
    
    public TEClassLoader(List<File> resourcesDirs) {
        this.resourcesDirs = resourcesDirs;
        cl = Thread.currentThread().getContextClassLoader();
        registeredClasses = new HashSet<>();
        registeredClasses.add("com.occamlab.te.parsers.HTTPParser");
        registeredClasses
                .add("com.occamlab.te.parsers.SchematronValidatingParser");
        registeredClasses.add("com.occamlab.te.parsers.XMLValidatingParser");
        registeredClasses
                .add("com.occamlab.te.parsers.XSLTransformationParser");
    }

    public URL getResource(String name) {
        for (File resourcesDir : resourcesDirs) {
            File f = new File(resourcesDir, name);
            try {
                return f.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "getResource", e);
            }
        }
        return cl.getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        if (resourcesDirs.size() > 0) {
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
        if (resourcesDirs.size() > 0 && u != null) {
            Vector<URL> v = new Vector<>();
            v.add(u);
            while (resources.hasMoreElements()) {
                v.add(resources.nextElement());
            }
            return v.elements();
        }
        return resources;
    }

    // public void registerClass(String name) {
    // if (!registeredClasses.contains(name)) {
    // registeredClasses.add(name);
    // }
    // }

    private Class<?> readClass(String name) {
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
            return defineClass(name, baos.toByteArray(), 0, baos.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "readClass", e);
            return null;
        }
    }

    public Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            for (String registeredClass : registeredClasses) {
                if (name.startsWith(registeredClass)) {
                    c = readClass(name);
                    break;
                }
            }
        }
        if (c == null) {
            try {
	            c = cl.loadClass(name);
            } catch (ClassNotFoundException e) {
            }
        }
        if (c == null) {
            c = readClass(name);
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
