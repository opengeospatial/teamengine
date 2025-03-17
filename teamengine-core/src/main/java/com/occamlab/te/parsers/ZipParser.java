/**
 * ********************************************************************
 *
 * Version Date: January 5, 2018
 *
 * Contributor(s):
 *    C. Heazel (WiSC) Modifications to address Fortify issues

 *    Chuck Heazel (WiSC): Modifications to address Fortify issues
 *         Made parse() and saveZipFile() private to discourage their use.
 */
package com.occamlab.te.parsers;

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

import java.io.*;
import java.net.URLConnection;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.util.Utils;

/**
 * Parses a zip file input by extracting the contents into the directory specified by the
 * value of the &lt;code&gt;java.io.tmpdir&lt;/code&gt; system property. The resulting
 * manifest is structured as follows:
 *
 * &lt;ctl:manifest xmlns:ctl="http://www.occamlab.com/ctl"&gt; &lt;ctl:file-entry
 * full-path="${java.io.tmpdir}/dir/doc.kml" size="2048" /&gt; &lt;/ctl:manifest&gt;
 *
 * @author jparrpearson
 */
public class ZipParser {

	public static final String PARSERS_NS = "http://www.occamlab.com/te/parsers";

	public static final String CTL_NS = "http://www.occamlab.com/ctl";

	// Add more mime types as necessary, if mime type not listed a default will
	// be given
	public static String[][] ApplicationMediaTypeMappings = { { "kml", "vnd.google-earth.kml+xml" },
			{ "kmz", "vnd.google-earth.kmz" }, { "xml", "application/xml" }, { "txt", "text/plain" },
			{ "jpg", "image/jpeg" }, { "jpeg", "image/jpeg" }, { "gif", "image/gif" }, { "png", "image/png" } };

	private static Logger jlogger = Logger.getLogger("com.occamlab.te.parsers.ZipParser");

	/**
	 * Returns the mime media type value for the given extension
	 * @param ext the filename extension to lookup
	 * @return String the mime type for the given extension
	 */
	public static String getMediaType(String ext) {
		String mediaType = "";

		// Find the media type value in the lookup table
		for (int i = 0; i < ApplicationMediaTypeMappings.length; i++) {
			if (ApplicationMediaTypeMappings[i][0].equals(ext.toLowerCase())) {
				mediaType = ApplicationMediaTypeMappings[i][1];
			}
		}

		// Give the media type default of "application/octet-stream"
		if (mediaType.isEmpty()) {
			mediaType = "application/octet-stream";
		}

		return mediaType;
	}

	/**
	 * Parses the entity (a ZIP archive) obtained in response to submitting a request to
	 * some URL. The resulting manifest is an XML document with &lt;ctl:manifest&gt; as
	 * the document element.
	 * @param resp the response to parse
	 * @param instruction a DOM Element representation of configuration information for
	 * this parser
	 * @param logger the test logger
	 * @return a DOM Document representing the manifest of items in the archive.
	 * @throws Throwable
	 */
	// Fortify Mod: made private
	private static Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Throwable {
		return parse(uc.getInputStream(), instruction, logger);
	}

	private static Document parse(InputStream is, Element instruction, PrintWriter logger) throws Throwable {

		// Create the response element, <ctl:manifest>
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElementNS(CTL_NS, "manifest");

		// Open the connection to the zip file
		ZipInputStream zis = new ZipInputStream(is);

		// Create the full directory path to store the zip entities
		Document d = instruction.getOwnerDocument();
		NodeList nodes = d.getElementsByTagNameNS(CTL_NS, "SessionDir");
		// Either use the given session directory, or make one in the java temp
		// directory
		String path = "";
		if (nodes.getLength() > 0) {
			Element e = (Element) nodes.item(0);
			path = e.getTextContent();
		}
		else {
			path = System.getProperty("java.io.tmpdir") + "/zipparser.temp";
		}
		String randomStr = Utils.randomString(16, new Random());
		path = path + "/work/" + randomStr;
		new File(path).mkdirs();

		// Unzip the file to a temporary location (java temp)
		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {
			// Open the output file and get info from it
			String filename = entry.getName();
			long size = entry.getSize();
			String ext = filename.substring(filename.lastIndexOf(".") + 1);
			String mediaType = getMediaType(ext);
			// Make the temp directory and subdirectories if needed
			String subdir = "";
			if (filename.lastIndexOf("/") != -1)
				subdir = filename.substring(0, filename.lastIndexOf("/"));
			else if (filename.lastIndexOf("\\") != -1)
				subdir = filename.substring(0, filename.lastIndexOf("\\"));
			new File(path, subdir).mkdirs();
			File outFile = new File(path, filename);
			if (!outFile.toPath().normalize().startsWith(path)) {
				throw new IOException("Bad zip entry");
			}
			if (outFile.isDirectory())
				continue;
			OutputStream out = new FileOutputStream(outFile);

			// Transfer bytes from the ZIP file to the output file
			byte[] buf = new byte[1024];
			int len;
			while ((len = zis.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			// Fortify Mod: Close the OutputStream and release its resources
			out.close();

			// Add the file information to the document
			Element fileEntry = doc.createElementNS(CTL_NS, "file-entry");
			fileEntry.setAttribute("full-path", outFile.getPath().replace('\\', '/'));
			fileEntry.setAttribute("media-type", mediaType);
			fileEntry.setAttribute("size", String.valueOf(size));
			root.appendChild(fileEntry);
		}

		doc.appendChild(root);

		// Return the <ctl:manifest> document
		return doc;
	}

	/**
	 * Extracts the local Zip file and saves to the working directory. The resulting
	 * manifest is an XML document with &lt;ctl:manifest&gt; as the document element.
	 * @param path the full path to the local Zip file
	 * @param instruction a DOM Element representation of configuration information for
	 * this parser
	 * @return a DOM Document representing the manifest of items in the archive.
	 */

	// Fortify Mod - made private
	private Document saveZipFile(String filepath, Document instruction) throws Exception {

		// Get a connection to the Zip file
		FileInputStream is = null;
		ZipInputStream zis = null;
		try {
			is = new FileInputStream(filepath);
			zis = new ZipInputStream(is);
		}
		catch (Exception e) {
			jlogger.log(Level.SEVERE, "saveZipFile", e);

			System.out.println("ERROR: " + e.getMessage());
			return null;
		}

		// Create the response element, <ctl:manifest>
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element root = doc.createElementNS(CTL_NS, "manifest");

		// Create the full directory path to store the zip entities
		Document d = instruction.getOwnerDocument();
		NodeList nodes = d.getElementsByTagNameNS(CTL_NS, "SessionDir");
		// Either use the given session directory, or make one in the java temp
		// directory
		String path = "";
		if (nodes.getLength() > 0) {
			Element e = (Element) nodes.item(0);
			path = e.getTextContent();
		}
		else {
			path = System.getProperty("java.io.tmpdir") + "/zipparser.temp";
		}
		String randomStr = Utils.randomString(16, new Random());
		path = path + "/work/" + randomStr;
		new File(path).mkdirs();

		// Unzip the file to a temporary location (java temp)
		ZipEntry entry = null;
		while ((entry = zis.getNextEntry()) != null) {
			System.out.println("File: " + entry.getName());
			// Open the output file and get info from it
			String filename = entry.getName();
			long size = entry.getSize();
			String ext = filename.substring(filename.lastIndexOf(".") + 1);
			String mediaType = getMediaType(ext);
			// Make the temp directory and subdirectories if needed
			String subdir = "";
			if (filename.lastIndexOf("/") != -1)
				subdir = filename.substring(0, filename.lastIndexOf("/"));
			else if (filename.lastIndexOf("\\") != -1)
				subdir = filename.substring(0, filename.lastIndexOf("\\"));
			new File(path, subdir).mkdirs();
			File outFile = new File(path, filename);
			if (!outFile.toPath().normalize().startsWith(path)) {
				throw new IOException("Bad zip entry");
			}
			if (outFile.isDirectory())
				continue;
			OutputStream out = new FileOutputStream(outFile);

			// Transfer bytes from the ZIP file to the output file
			byte[] buf = new byte[1024];
			int len;
			while ((len = zis.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			// Fortify Mod: close the OutputStream and release its resources
			out.close();

			// Add the file information to the document
			Element fileEntry = doc.createElementNS(CTL_NS, "file-entry");
			fileEntry.setAttribute("full-path", outFile.getPath().replace('\\', '/'));
			fileEntry.setAttribute("media-type", mediaType);
			fileEntry.setAttribute("size", String.valueOf(size));
			root.appendChild(fileEntry);
		}
		// Fortify Mod: Close the ZipInputStream and release resources
		zis.close();

		doc.appendChild(root);

		// Return the <ctl:manifest> document
		return doc;
	}

}
