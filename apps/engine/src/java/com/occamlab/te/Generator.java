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

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import com.occamlab.te.index.Index;
import com.occamlab.te.util.Misc;

public class Generator {
    // Generates XSL template files from CTL sources and a master index
    // of metadata about the CTL objects
    public static Index generateXsl(SetupOptions opts) throws Exception {
        Index masterIndex = new Index();
        
        // Create CTL validator
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema ctl_schema = sf.newSchema(Misc.getResourceAsFile("com/occamlab/te/schemas/ctl.xsd"));
        Validator ctl_validator = ctl_schema.newValidator();
        CtlErrorHandler validation_eh = new CtlErrorHandler();
        ctl_validator.setErrorHandler(validation_eh);
        
        // Create a transformer to generate executable scripts from CTL sources
        Processor processor = new Processor(false);
        processor.setConfigurationProperty(FeatureKeys.XINCLUDE, Boolean.TRUE);
        XsltCompiler generatorCompiler = processor.newXsltCompiler();
        File generatorStylesheet = Misc.getResourceAsFile("com/occamlab/te/generate_xsl.xsl");
        XsltExecutable generatorXsltExecutable = generatorCompiler.compile(new StreamSource(generatorStylesheet));
        XsltTransformer generatorTransformer = generatorXsltExecutable.load();

        // Create a list of CTL sources (may be files or dirs)
        ArrayList<File> sources = new ArrayList<File>();
        File f = Misc.getResourceAsFile("com/occamlab/te/scripts/parsers.ctl");
        sources.add(f.getParentFile());
        sources.addAll(opts.getSources());

        // Create a list of source CTL files only (no dirs),
        // and a corresponding list containing a working dir for each file
        ArrayList<File> sourceFiles = new ArrayList<File>(); 
        ArrayList<File> workDirs = new ArrayList<File>();
        Iterator<File> it = sources.iterator();
        while (it.hasNext()) {
            File source = it.next();
//System.out.println("Processing source(s) at: " + source.getAbsolutePath());
//          appLogger.log(Level.INFO, "Processing source(s) at: " + source.getAbsolutePath());

            String encodedName = URLEncoder.encode(source.getAbsolutePath(), "UTF-8");
            encodedName = encodedName.replace('%', '~');  // In Java 5, the Document.parse function has trouble with the URL % encoding
            File workingDir = new File(opts.getWorkDir(), encodedName);
            workingDir.mkdir();
            
            if (source.isDirectory()) {
                String[] children = source.list();
                for (int i = 0; i < children.length; i++) {
                    // Finds all .ctl and .xml files in the directory to use
                    String lowerName = children[i].toLowerCase();
                    if (lowerName.endsWith(".ctl") || lowerName.endsWith(".xml")) {
                        File file = new File(source, children[i]);
                        if (file.isFile()) {
                            sourceFiles.add(file);
                            String basename = children[i].substring(0, children[i].length() - 4);
                            File subdir = new File(workingDir, basename);
                            subdir.mkdir();
                            workDirs.add(subdir);
                        }
                    }
                }
            } else {
                sourceFiles.add(source);
                workDirs.add(workingDir);
            }
        }

        // Process each CTL source file
        for (int i = 0; i < sourceFiles.size(); i++) {
            File sourceFile = sourceFiles.get(i);
            File workingDir = workDirs.get(i);

            // Read previous index for this file (if any), and determine whether the
            // index and xsl need to be regenerated
            File indexFile = new File(workingDir, "index.xml");
            Index index = null;
            boolean regenerate = true;
            if (indexFile.isFile()) {
                try {
                    if (indexFile.lastModified() > generatorStylesheet.lastModified()) {
                        index = new Index(indexFile);
                        regenerate = index.outOfDate();
                    }
                } catch (Exception e) {
                    // If there was an exception reading the index file, it is likely corrupt.  Regenerate it.
                    regenerate = true;
                }
            }
            
            if (regenerate) {
                // Validate the source CTL file 
                boolean validationErrors = false;
                if (opts.isValidate()) {
                    int old_count = validation_eh.getErrorCount();
                    ctl_validator.validate(new StreamSource(sourceFile));
                    validationErrors = (validation_eh.getErrorCount() > old_count);
                }
                
                if (!validationErrors) {
                    // Clean up the working directory
                    Misc.deleteDirContents(workingDir);
                    
                    // Run the generator transformation.  Output is an index file and is saved to disk.
                    // The generator also creates XSL template files in the working dir.
                    generatorTransformer.setSource(new StreamSource(sourceFile));
                    Serializer generatorSerializer = new Serializer();
                    generatorSerializer.setOutputFile(indexFile);
                    generatorTransformer.setDestination(generatorSerializer);
                    XdmAtomicValue av = new XdmAtomicValue(workingDir.getAbsolutePath());
                    generatorTransformer.setParameter(new QName("outdir"), av);
                    generatorTransformer.transform();
                    
                    // Read the generated index
                    index = new Index(indexFile);
                }
            }

            // Add new index entries to the master index
            masterIndex.add(index);
        }
            
        // If there were any validation errors, display them and throw an exception
        int error_count = validation_eh.getErrorCount();
        if (error_count > 0) {
            String msg = error_count + " validation error"
                    + (error_count == 1 ? "" : "s");
            int warning_count = validation_eh.getWarningCount();
            if (warning_count > 0) {
                msg += " and " + warning_count + " warning"
                        + (warning_count == 1 ? "" : "s");
            }
            msg += " detected.";
//            appLogger.severe(msg);
            throw new Exception(msg);
        }
        
        return masterIndex;
    }
}
