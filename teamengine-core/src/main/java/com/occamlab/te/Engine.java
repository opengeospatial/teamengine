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

package com.occamlab.te;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

import com.occamlab.te.index.FunctionEntry;
import com.occamlab.te.index.Index;
import com.occamlab.te.index.TemplateEntry;
import com.occamlab.te.index.TestEntry;
import com.occamlab.te.saxon.TEFunctionLibrary;

public class Engine {
    int cacheSize = 50;
    Processor processor = null;
    XsltCompiler compiler = null;
    DocumentBuilder builder = null;
    TeErrorListener errorListener = null;
    XsltExecutable formExecutable = null;

    // Map of loaded executables, ordered by access order
    public Map<String, XsltExecutable> loadedExecutables = Collections
            .synchronizedMap(new LinkedHashMap<String, XsltExecutable>(256,
                    0.75f, true));

    public Map<String, TEClassLoader> classLoaders;

    public Engine(Index index, String sourcesName, TEClassLoader cl)
            throws Exception {
        this();
        ArrayList<Index> indexes = new ArrayList<Index>();
        indexes.add(index);
        classLoaders = new HashMap<String, TEClassLoader>();
        classLoaders.put(sourcesName, cl);
        addFunctionLibrary(indexes);
    }

    public Engine(Collection<Index> indexes,
            Map<String, TEClassLoader> classLoaders, int cacheSize)
            throws Exception {
        this();
        this.classLoaders = classLoaders;
        if (cacheSize > 0) {
            this.cacheSize = cacheSize;
        }
        addFunctionLibrary(indexes);
    }

    public Engine() throws Exception {
        String s = System.getProperty("te.cacheSize");
        if (s != null) {
            cacheSize = Integer.parseInt(s);
        }

        // Create processor
        processor = new Processor(false);

        // Modify its configuration settings
        Configuration config = processor.getUnderlyingConfiguration();
        config.setVersionWarning(false);

        // Use our custom error listener which reports line numbers in the CTL
        // source file
        errorListener = new TeErrorListener();
        config.setErrorListener(errorListener);

        // Create a compiler and document builder
        compiler = processor.newXsltCompiler();
        builder = processor.newDocumentBuilder();

        // Load an executable for the TECore.form method
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream("com/occamlab/te/formfn.xsl");
        formExecutable = compiler.compile(new StreamSource(is));
    }

    public void addFunctionLibrary(Collection<Index> indexes) {
        // Change the function library to a new library list that includes
        // our custom java function library
        Configuration config = processor.getUnderlyingConfiguration();
        FunctionLibraryList liblist = new FunctionLibraryList();
        for (Index index : indexes) {
            TEFunctionLibrary telib = new TEFunctionLibrary(config, index);
            liblist.addFunctionLibrary(telib);
        }
        liblist.addFunctionLibrary(config.getExtensionBinder("java"));
        config.setExtensionBinder("java", liblist);

    }

    /**
     * Loads all of the XSL executables. This is a time consuming operation.
     * 
     * @param index
     * @param sourcesName
     *            A stylesheet reference.
     * @throws Exception
     *             If the stylesheet fail to compile.
     */
    public void preload(Index index, String sourcesName) throws Exception {
        for (String key : index.getTestKeys()) {
            TestEntry te = index.getTest(key);
            loadExecutable(te, sourcesName);
        }
        for (String key : index.getFunctionKeys()) {
            List<FunctionEntry> functions = index.getFunctions(key);
            for (FunctionEntry fe : functions) {
                if (!fe.isJava()) {
                    loadExecutable(fe, sourcesName);
                }
            }
        }
    }

    boolean freeExecutable() {
        Set<String> keys = loadedExecutables.keySet();
        synchronized (loadedExecutables) {
            Iterator<String> it = keys.iterator();
            if (it.hasNext()) {
                loadedExecutables.remove(it.next());
                return true;
            }
        }
        return false;
    }

    public XsltExecutable loadExecutable(TemplateEntry entry, String sourcesName)
            throws Exception {
        String key = sourcesName + "," + entry.getId();
        if (entry instanceof FunctionEntry) {
            key += "_" + Integer.toString(((FunctionEntry) entry).getMinArgs());
        }
        XsltExecutable executable = loadedExecutables.get(key);
        while (executable == null) {
            // capture messages written to System.err by default message emitter
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream console = System.err;
            try {
                System.setErr(new PrintStream(baos));
                Source source = new StreamSource(entry.getTemplateFile());
                executable = compiler.compile(source);
                loadedExecutables.put(key, executable);
            } catch (OutOfMemoryError e) {
                boolean freed = freeExecutable();
                if (!freed) {
                    throw e;
                }
            } catch (SaxonApiException e) {
                throw new Exception(baos.toString() + e.getMessage(),
                        e.getCause());
            } finally {
                System.setErr(console);
            }
        }
        while (loadedExecutables.size() > cacheSize) {
            boolean freed = freeExecutable();
            if (!freed) {
                break;
            }
        }

        return executable;
    }

    public Map<String, XsltExecutable> getLoadedExecutables() {
        return loadedExecutables;
    }

    public TEClassLoader getClassLoader(String sourcesName) {
        return classLoaders.get(sourcesName);
    }

    public DocumentBuilder getBuilder() {
        return builder;
    }

    public XsltCompiler getCompiler() {
        return compiler;
    }

    public TeErrorListener getErrorListener() {
        return errorListener;
    }

    public XsltExecutable getFormExecutable() {
        return formExecutable;
    }

    public Processor getProcessor() {
        return processor;
    }

    public void setClassLoader(String sourcesName, TEClassLoader cl) {
        classLoaders.put(sourcesName, cl);
    }

}
