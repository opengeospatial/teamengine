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
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static Logger logger = Logger.getLogger("com.occamlab.te.Generator");

	public static Index generateXsl(SetupOptions opts) throws Exception {
		InputStream is = null;
		try {
			Index masterIndex = new Index();

			// Create CTL validator
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema ctl_schema = sf.newSchema(Generator.class.getResource("/com/occamlab/te/schemas/ctl.xsd"));
			Validator ctl_validator = ctl_schema.newValidator();
			CtlErrorHandler validation_eh = new CtlErrorHandler();
			ctl_validator.setErrorHandler(validation_eh);

			// Create a transformer to generate executable scripts from CTL sources
			Processor processor = new Processor(false);
			processor.setConfigurationProperty(FeatureKeys.XINCLUDE, Boolean.TRUE);
			processor.setConfigurationProperty(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
			XsltCompiler generatorCompiler = processor.newXsltCompiler();
			URL generatorStylesheet = Generator.class.getResource("/com/occamlab/te/generate_xsl.xsl");
			is = generatorStylesheet.openStream();
			XsltExecutable generatorXsltExecutable = generatorCompiler.compile(new StreamSource(is, generatorStylesheet
					.toExternalForm()));
			XsltTransformer generatorTransformer = generatorXsltExecutable.load();

			// Create a list of CTL sources (may be files or dirs)
			ArrayList<File> sources = new ArrayList<File>();
			sources.addAll(opts.getSources());

			// Create a list of source CTL files only (no dirs),
			// and a corresponding list containing a working dir for each file
			ArrayList<File> sourceFiles = new ArrayList<File>();
			ArrayList<File> workDirs = new ArrayList<File>();
			Iterator<File> it = sources.iterator();
			while (it.hasNext()) {
				File source = it.next();
				// System.out.println("Processing source(s) at: " + source.getAbsolutePath());
				// appLogger.log(Level.INFO, "Processing source(s) at: " + source.getAbsolutePath());

				String encodedName = URLEncoder.encode(source.getAbsolutePath(), "UTF-8");
				// encodedName = encodedName.replace('%', '~'); // In Java 5, the Document.parse function has trouble
				// with the URL % encoding
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

			// formerly used to be in a resources classpath directory, but now are included in the .jar
			String encodedName = URLEncoder.encode("/com/occamlab/te/scripts/parsers.ctl", "UTF-8");
			File workingDir = new File(opts.getWorkDir(), encodedName);
			workingDir.mkdir();
			File indexFile = new File(workingDir, "index.xml");
			Index tmp = generate(opts, Generator.class.getResource("/com/occamlab/te/scripts/parsers.ctl"),
					ctl_validator, validation_eh, generatorTransformer, workingDir, indexFile);
			if (tmp != null) {
				masterIndex.add(tmp);
			}
			encodedName = URLEncoder.encode("/com/occamlab/te/scripts/functions.ctl", "UTF-8");
			workingDir = new File(opts.getWorkDir(), encodedName);
			workingDir.mkdir();
			indexFile = new File(workingDir, "index.xml");
			tmp = generate(opts, Generator.class.getResource("/com/occamlab/te/scripts/functions.ctl"), ctl_validator,
					validation_eh, generatorTransformer, workingDir, indexFile);
			if (tmp != null) {
				masterIndex.add(tmp);
			}
			// Process each CTL source file
			for (int i = 0; i < sourceFiles.size(); i++) {
				File sourceFile = sourceFiles.get(i);
				workingDir = workDirs.get(i);

				// Read previous index for this file (if any), and determine whether the
				// index and xsl need to be regenerated
				indexFile = new File(workingDir, "index.xml");
				Index index = null;
				boolean regenerate = true;
				if (indexFile.isFile()) {
					try {
						index = new Index(indexFile);
						regenerate = index.outOfDate();
					} catch (Exception e) {
						// If there was an exception reading the index file, it is likely corrupt. Regenerate it.
						regenerate = true;
					}
				}

				if (regenerate) {
					tmp = generate(opts, sourceFile.toURI().toURL(), ctl_validator, validation_eh,
							generatorTransformer, workingDir, indexFile);
					if (tmp != null) {
						index = tmp;
					}
				}

				// Add new index entries to the master index
				masterIndex.add(index);
			}

			// If there were any validation errors, display them and throw an exception
			int error_count = validation_eh.getErrorCount();
			if (error_count > 0) {
				String msg = error_count + " validation error" + (error_count == 1 ? "" : "s");
				int warning_count = validation_eh.getWarningCount();
				if (warning_count > 0) {
					msg += " and " + warning_count + " warning" + (warning_count == 1 ? "" : "s");
				}
				msg += " detected.";
				// appLogger.severe(msg);
				throw new Exception(msg);
			}

			return masterIndex;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	private static Index generate(SetupOptions opts, URL sourceFile, Validator ctl_validator,
			CtlErrorHandler validation_eh, XsltTransformer generatorTransformer, File workingDir, File indexFile)
			throws Exception {
		InputStream in = null;
		try {
			// Validate the source CTL file
			boolean validationErrors = false;
			if (opts.isValidate()) {
				int old_count = validation_eh.getErrorCount();
				logger.log(Level.INFO, "Validating " + sourceFile);
				in = sourceFile.openStream();
				ctl_validator.validate(new StreamSource(in, sourceFile.toExternalForm()));
				validationErrors = (validation_eh.getErrorCount() > old_count);
			}

			if (in != null) {
				in.close();
			}

			if (!validationErrors) {
				in = sourceFile.openStream();
				// Clean up the working directory
				Misc.deleteDirContents(workingDir);

				// Run the generator transformation. Output is an index file and is saved to disk.
				// The generator also creates XSL template files in the working dir.
				generatorTransformer.setSource(new StreamSource(in, sourceFile.toExternalForm()));
				Serializer generatorSerializer = new Serializer();
				generatorSerializer.setOutputFile(indexFile);
				generatorTransformer.setDestination(generatorSerializer);
				XdmAtomicValue av = new XdmAtomicValue(workingDir.getAbsolutePath());
				generatorTransformer.setParameter(new QName("outdir"), av);
				generatorTransformer.transform();

				// Read the generated index
				return new Index(indexFile);
			}
			return null;
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

}
