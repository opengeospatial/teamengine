/****************************************************************************

 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 the specific language governing rights and limitations under the License.

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Fabrizio Vitale
 jointly with the Institute of Methodologies for Environmental Analysis 
 (IMAA) part of the Italian National Research Council (CNR). 
 Portions created by Fabrizio Vitale are Copyright (C) 2009. All Rights Reserved.

 Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

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
		} catch (MalformedURLException e) {
			this.xsltSystemId="";
		}
	}
	
	public DocumentationHelper(URL xslStylesheetURL) {
		this.xsltSystemId = xslStylesheetURL.toExternalForm();
	    try {
	       	this.xsltFileHandler= new File(URLDecoder.decode(xslStylesheetURL.getFile(), "UTF-8"));
	    } catch (UnsupportedEncodingException e) {
	       	this.xsltFileHandler= new File("");
	    }
	}

	public DocumentationHelper(File xslStylesheetFile) {
		this.xsltFileHandler= xslStylesheetFile;	
	}

	
	/**
	 * Create the pretty print HTML document of log report.
	 * Verify the content of log root directory and create the report.html
	 * file if it not exists. 
	 * @param logDir existing logs directory
	 */
	public void prettyPrintsReport(File logDir) throws Exception {
		if ((!logDir.exists()) || (!logDir.isDirectory())) {
			throw new Exception("Error: LOGDIR " + logDir.getAbsolutePath() + " seems not a valid directory. ");
		}  
		File html_logs_report_file=new File(logDir.getAbsolutePath()+File.separator+"report.html");
		prettyPrintsReport(logDir, html_logs_report_file);
	}
	
   
	/**
	 * Apply xslt stylesheet to xml logs file and crate an HTML report file.
	 * 
	 * @param xmlLogsFile
	 * @param htmlReportFile
	 */
	private void prettyprint(String xmlLogsFile,FileOutputStream htmlReportFile) throws Exception{
		//System.setProperty("javax.xml.transform.TransformerFactory",
		//"net.sf.saxon.TransformerFactoryImpl");
		TransformerFactory tFactory = TransformerFactory.newInstance();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		DocumentBuilder parser = factory.newDocumentBuilder();			
		Document document =  parser.parse(xmlLogsFile);			
		Transformer transformer = tFactory.newTransformer(new StreamSource(xsltFileHandler));
		transformer.transform(new DOMSource(document), new StreamResult(htmlReportFile));		
	}
	
	/**
	 * Generate pseudocode documentation for CTL test scripts.
	 * Apply the stylesheet to documentate the sources of tests.
	 * 
	 * @param sourcecodePath main file of test source
	 * @param suiteName name of the suite to be documented (TBD)
	 * @param htmlFileOutput path of generated file
	 * @throws Exception
	 */
	public void generateDocumentation(String sourcecodePath, String suiteName, FileOutputStream htmlFileOutput) throws Exception {
		TransformerFactory tFactory = TransformerFactory.newInstance();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);
		DocumentBuilder parser = factory.newDocumentBuilder();			
		Document document =  parser.parse(sourcecodePath);			
		Transformer transformer = tFactory.newTransformer(new StreamSource(xsltSystemId));
		transformer.transform(new DOMSource(document), new StreamResult(htmlFileOutput));
		
	}

	public void generateDocumentation(String sourcecodePath, FileOutputStream htmlFileOutput) throws Exception{
		generateDocumentation(sourcecodePath, null,htmlFileOutput);
	}
	
	

	/**
	 * Create the pretty print HTML document of log report.
	 * Verify the content of log root directory and create the report.html
	 * file if it not exists. 
	 * @param sessionDir existing logs directory
	 */
	public void prettyPrintsReport(File sessionDir, File prettyPrintReportFile)throws Exception {
		if ((!sessionDir.exists()) || (!sessionDir.isDirectory())) {
			throw new Exception("Error: LOGDIR " + sessionDir.getAbsolutePath() + " seems not a valid directory. ");
		}  
		File xml_logs_report_file= new File(sessionDir.getAbsolutePath()+File.separator+"report_logs.xml");
		if (!xml_logs_report_file.exists()) {
			//throw new Exception("Error: missing logfile  " + xml_logs_report_file.getAbsolutePath() + " ! ");
			System.out.println("Warning: missing logfile  " + xml_logs_report_file.getAbsolutePath() + " ! ");
			System.out.println("Trying to create it!");
			LogUtils.createFullReportLog(sessionDir.getAbsolutePath());
		}
		File html_output_report_file=prettyPrintReportFile; //new File(sessionDir.getAbsolutePath()+File.separator+"report.html");
		if (html_output_report_file.exists()) {
			System.out.println("Report file \""+html_output_report_file.getAbsolutePath()+"\" reused!");
			return;
		}
		prettyprint(xml_logs_report_file.toURI().toURL().toExternalForm(),  new FileOutputStream(html_output_report_file));	
		System.out.println("Report file \""+html_output_report_file.getAbsolutePath()+"\" created!");
	}

	

}
