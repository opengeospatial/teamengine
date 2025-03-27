/****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Fabrizio Vitale
 jointly with the Institute of Methodologies for Environmental Analysis
 (IMAA) part of the Italian National Research Council (CNR).
 Portions created by Fabrizio Vitale are Copyright (C) 2009. All Rights Reserved.

 Contributor(s):
 	C. Heazel (WiSC): Added Fortify adjudication changes

 ****************************************************************************/
package com.occamlab.te.util;

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
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.XMLConstants; // Addition for Fortify modifications

import org.w3c.dom.Document;

import com.occamlab.te.SetupOptions;

/**
 * The purpose of this class is
 *
 * @author Fabrizio Vitale
 *
 */
public class DocumentationHelper {

	private String xsltSystemId;

	private File xsltFileHandler;

	public DocumentationHelper(String xslStylesheetFilePath) {
		try {
			this.xsltSystemId = new File(xslStylesheetFilePath).toURI().toURL().toExternalForm();
			this.xsltFileHandler = new File(xslStylesheetFilePath);
		}
		catch (MalformedURLException e) {
			this.xsltSystemId = "";
		}
	}

	public DocumentationHelper(URL xslStylesheetURL) {
		this.xsltSystemId = xslStylesheetURL.toExternalForm();
		this.xsltFileHandler = new File(URLDecoder.decode(xslStylesheetURL.getFile(), StandardCharsets.UTF_8));
	}

	public DocumentationHelper(File xslStylesheetFile) {
		this.xsltFileHandler = xslStylesheetFile;
	}

	/**
	 * Create the pretty print HTML document of log report. Verify the content of log root
	 * directory and create the report.html file if it not exists.
	 * @param logDir existing logs directory
	 */
	public File prettyPrintsReport(File logDir) throws Exception {
		if ((!logDir.exists()) || (!logDir.isDirectory())) {
			throw new Exception("Error: LOGDIR " + logDir.getAbsolutePath() + " seems not a valid directory. ");
		}
		File html_logs_report_file = new File(logDir.getAbsolutePath() + File.separator + "report.html");
		prettyPrintsReport(logDir, html_logs_report_file);
		return html_logs_report_file;
	}

	/**
	 * Apply xslt stylesheet to xml logs file and crate an HTML report file.
	 * @param xmlLogsFile
	 * @param htmlReportFile
	 */
	private void prettyprint(String xmlLogsFile, FileOutputStream htmlReportFile) throws Exception {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		// Fortify Mod: prevent external entity injection
		tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Fortify Mod: prevent external entity injection
		factory.setExpandEntityReferences(false);
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.parse(xmlLogsFile);
		Transformer transformer = tFactory.newTransformer(new StreamSource(xsltFileHandler));
		transformer.transform(new DOMSource(document), new StreamResult(htmlReportFile));
	}

	/**
	 * Generate pseudocode documentation for CTL test scripts. Apply the stylesheet to
	 * documentate the sources of tests.
	 * @param sourcecodePath main file of test source
	 * @param suiteName name of the suite to be documented (TBD)
	 * @param htmlFileOutput path of generated file
	 * @throws Exception
	 */
	public void generateDocumentation(String sourcecodePath, String suiteName, FileOutputStream htmlFileOutput)
			throws Exception {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		// Fortify Mod: prevent external entity injection
		tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Fortify Mod: prevent external entity injection
		factory.setExpandEntityReferences(false);
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document document = parser.parse(sourcecodePath);
		Transformer transformer = tFactory.newTransformer(new StreamSource(xsltSystemId));
		transformer.transform(new DOMSource(document), new StreamResult(htmlFileOutput));

	}

	public void generateDocumentation(String sourcecodePath, FileOutputStream htmlFileOutput) throws Exception {
		generateDocumentation(sourcecodePath, null, htmlFileOutput);
	}

	/**
	 * Create the pretty print HTML document of log report. Verify the content of log root
	 * directory and create the report.html file if it not exists.
	 * @param sessionDir existing logs directory
	 */
	public void prettyPrintsReport(File sessionDir, File prettyPrintReportFile) throws Exception {
		if ((!sessionDir.exists()) || (!sessionDir.isDirectory())) {
			throw new Exception("Error: LOGDIR " + sessionDir.getAbsolutePath() + " seems not a valid directory. ");
		}
		File xml_logs_report_file = new File(sessionDir.getAbsolutePath() + File.separator + "report_logs.xml");
		if (!xml_logs_report_file.exists()) {
			// throw new Exception("Error: missing logfile " +
			// xml_logs_report_file.getAbsolutePath() + " ! ");
			System.out.println("Warning: missing logfile  " + xml_logs_report_file.getAbsolutePath() + " ! ");
			System.out.println("Trying to create it!");
			LogUtils.createFullReportLog(sessionDir.getAbsolutePath());
		}
		if (prettyPrintReportFile.exists()) {
			System.out.println("Report file \"" + prettyPrintReportFile.getAbsolutePath() + "\" reused!");
			return;
		}
		// Fortify Mod: Close the FileOutputStream and releaase its resources
		// prettyprint(xml_logs_report_file.toURI().toURL().toExternalForm(),
		// new FileOutputStream(html_output_report_file));
		FileOutputStream fos = new FileOutputStream(prettyPrintReportFile);
		prettyprint(xml_logs_report_file.toURI().toURL().toExternalForm(), fos);
		fos.close();
		System.out.println("Report file \"" + prettyPrintReportFile.getAbsolutePath() + "\" created!");
	}

	public static void main(String[] args) throws Exception {
		SetupOptions setupOpts = new SetupOptions();
		File scriptsDir = new File(SetupOptions.getBaseConfigDirectory(), "scripts");
		String cmd = "java com.occamlab.te.util.DocumentationHelper";
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-source=")) {
				File f = new File(scriptsDir, args[i].substring(8));
				// Fortify Mod: make sure that the -source argument
				// is not pointing to an illegal location
				if (!f.exists() || !setupOpts.addSourceWithValidation(f)) {
					System.out.println("Error: Can't find CTL script(s) at " + f.getAbsolutePath());
					return;
				}
			}
			else if (args[i].startsWith("-cmd=")) {
				cmd = args[i].substring(5);
			}
			else if (args[i].equals("-h") || args[i].equals("-help") || args[i].equals("-?")) {
				syntax(cmd);
				return;
			}
		}

		if (setupOpts.getSources().isEmpty()) {
			syntax(cmd);
			return;
		}

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		DocumentationHelper docCode = new DocumentationHelper(
				cl.getResource("com/occamlab/te/PseudoCTLDocumentation.xsl"));
		File html_output_documentation_file = new File(
				setupOpts.getWorkDir().getAbsolutePath() + File.separator + "documentation.html");
		if (html_output_documentation_file.exists())
			throw new Exception("Error: Documentation file already exists, check the file "
					+ html_output_documentation_file.getAbsolutePath() + " ");
		FileOutputStream fos = new FileOutputStream(html_output_documentation_file);
		docCode.generateDocumentation(setupOpts.getSources().get(0).getAbsolutePath(), fos);
		fos.close();
		System.out
			.println("Test documentation file \"" + html_output_documentation_file.getAbsolutePath() + "\" created!");
	}

	static void syntax(String cmd) {
		System.out.println("Generates documentation of tests.\n");
		System.out.println(cmd + " -source=ctlfile|dir");
	}

}
