/*

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s):
 	C. Heazel (WiSC): Added Fortify adjudication changes
 */
package com.occamlab.te;

/*-
 * #%L
 * TEAM Engine - Core Module
 * %%
 * Copyright (C) 2006 - 2024 Open Geospatial Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
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

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader; // Fortify mod.

import com.occamlab.te.index.Index;
import com.occamlab.te.util.Misc;
import com.occamlab.te.util.XMLParserUtils;

/**
 * Generates XSL template files from CTL sources and a master index of metadata about the
 * CTL objects. The resulting files are stored in sub-directories of the main work
 * directory (TE_BASE/work).
 */
public class Generator {

	private static final Logger LOGR = Logger.getLogger(Generator.class.getName());

	private static String suiteDefaultResult = "Pass";

	public static void setSuiteDefaultResult(String resultName) {
		suiteDefaultResult = resultName.equals("BestPractice") ? "BestPractice" : "Pass";
	}

	public static String getSuiteDefaultResult() {
		return suiteDefaultResult;
	}

	/**
	 * Generates the XSLT stylesheets for running in doc mode.
	 * @param opts Static configuration settings.
	 * @return A master Index object that describes the resulting stylesheets.
	 * @throws Exception
	 */
	public static Index generateDocXsl(SetupOptions opts) throws Exception {
		return generateXsl(opts, "com/occamlab/te/generate_dxsl.xsl", true);
	}

	/**
	 * Generates the XSLT stylesheets that constitute an executable test suite (ETS).
	 * @param opts Static configuration settings.
	 * @return A master Index object that describes the resulting stylesheets.
	 * @throws Exception
	 */
	public static Index generateXsl(SetupOptions opts) throws Exception {
		return generateXsl(opts, "com/occamlab/te/generate_xsl.xsl", false);
	}

	private static Index generateXsl(SetupOptions opts, String generatorStylesheetResource, boolean docMode)
			throws Exception {
		Index masterIndex = new Index();

		// Create CTL validator
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema ctl_schema = sf.newSchema(new StreamSource(Misc.getResourceURL("com/occamlab/te/schemas/ctl.xsd")));
		Validator ctl_validator = ctl_schema.newValidator();
		CtlErrorHandler validation_eh = new CtlErrorHandler();
		ctl_validator.setErrorHandler(validation_eh);

		// Create a transformer to generate executable scripts from CTL sources
		Processor processor = new Processor(false);
		processor.setConfigurationProperty(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
		XsltCompiler generatorCompiler = processor.newXsltCompiler();
		XsltExecutable generatorXsltExecutable = generatorCompiler
			.compile(new StreamSource(Misc.getResourceURL(generatorStylesheetResource)));
		XsltTransformer generatorTransformer = generatorXsltExecutable.load();

		// Create a list of CTL sources (may be files or dirs)
		ArrayList<File> sources = new ArrayList<>();
		File f = Misc.getResourceAsFile("com/occamlab/te/scripts/parsers.ctl");
		if (f.exists()) {
			sources.add(f.getParentFile());
		}
		sources.addAll(opts.getSources());

		// Create a list of source CTL files only (no dirs),
		// and a corresponding list containing a working dir for each file
		ArrayList<File> sourceFiles = new ArrayList<>();
		ArrayList<File> workDirs = new ArrayList<>();
		Iterator<File> it = sources.iterator();
		while (it.hasNext()) {
			File source = it.next();
			LOGR.log(Level.FINE, "Processing CTL source files in {0}", source.getAbsolutePath());
			String encodedName = createEncodedName(source);
			if (docMode) {
				encodedName += "d";
			}
			File workingDir = new File(opts.getWorkDir(), encodedName);
			if (!workingDir.exists() && !workingDir.mkdir()) {
				LOGR.log(Level.WARNING, "Unable to create working directory at {0}", workingDir.getAbsolutePath());
			}
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
			}
			else {
				sourceFiles.add(source);
				workDirs.add(workingDir);
			}
		}

		// resolve xinclude elements but omit xml:base attributes
		SAXParser parser = XMLParserUtils.createXIncludeAwareSAXParser(false);

		File generatorStylesheet = Misc.getResourceAsFile(generatorStylesheetResource);

		// Process each CTL source file
		for (int i = 0; i < sourceFiles.size(); i++) {
			File sourceFile = sourceFiles.get(i);
			File workingDir = workDirs.get(i);

			// Read previous index for this file (if any), and determine whether
			// the index and xsl need to be regenerated
			File indexFile = new File(workingDir, "index.xml");
			Index index = null;
			boolean regenerate = true;

			if (generatorStylesheet == null) {
				// generatorStylesheet couldn't be found as a file (it was loaded from
				// classpath jar)
				regenerate = true;
			}
			else if (indexFile.isFile()) {
				try {
					if (indexFile.lastModified() > generatorStylesheet.lastModified()) {
						index = new Index(indexFile);
						regenerate = index.outOfDate();
					}
				}
				catch (Exception e) {
					// If there was an exception reading the index file, it is
					// likely corrupt. Regenerate it.
					regenerate = true;
				}
			}

			if (regenerate) {
				// Validate the source CTL file
				boolean validationErrors = false;
				if (opts.isValidate()) {
					int old_count = validation_eh.getErrorCount();
					LOGR.log(Level.CONFIG, "Validating " + sourceFile);
					ctl_validator.validate(new StreamSource(sourceFile));
					validationErrors = (validation_eh.getErrorCount() > old_count);
				}

				if (!validationErrors) {
					// Clean up the working directory
					Misc.deleteDirContents(workingDir);

					InputSource input = new InputSource(new FileInputStream(sourceFile));
					input.setSystemId(sourceFile.toURI().toString());
					// Fortify Mods to prevent External Entity Injection
					XMLReader reader = parser.getXMLReader();
					reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
					Source ctlSource = new SAXSource(reader, input);
					// Source ctlSource = new SAXSource(parser.getXMLReader(),input);
					// End Fortify Mods
					// Run the generator transformation. Output is an index file
					// and is saved to disk. The generator also creates XSL
					// template files in the working dir.
					generatorTransformer.setSource(ctlSource);
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

		// If there were any validation errors, display them and throw an
		// exception
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
	}

	/**
	 * Creates a directory name from a file path.
	 * @param source A File reference.
	 * @return A String representing a legal directory name.
	 */
	public static String createEncodedName(File source) {
		String fileURI = source.toURI().toString();
		String userDirURI = new File(System.getProperty("user.dir")).toURI().toString();
		fileURI = fileURI.replace(userDirURI, "");
		return fileURI.substring(fileURI.lastIndexOf(':') + 1).replace("%20", "-").replace('/', '_');
	}

}
