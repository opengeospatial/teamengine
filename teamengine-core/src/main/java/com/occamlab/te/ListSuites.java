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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.index.Index;
import com.occamlab.te.index.SuiteEntry;

/**
 * Command line utility for listing test suites in CTL sources.
 *
 * C. Heazel: modified to address Fortify issues 1/24/18
 *
 */
public class ListSuites {

	public static void main(String[] args) throws Exception {
		SetupOptions setupOpts = new SetupOptions();
		File scriptsDir = new File(SetupOptions.getBaseConfigDirectory(), "scripts");
		String cmd = "java com.occamlab.te.ListSuites";

		// Parse source command-line argument
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
			String path = SetupOptions.getBaseConfigDirectory() + "/config.xml";
			if (new File(path).exists()) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				dbf.setExpandEntityReferences(false);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(path);
				NodeList nl = doc.getElementsByTagName("source");
				if (nl != null) {
					for (int i = 0; i < nl.getLength(); i++) {
						Element source = (Element) nl.item(i);
						File f = new File(scriptsDir, source.getTextContent());
						if (!f.exists() || !setupOpts.addSourceWithValidation(f)) {
							System.out.println("Error: Can't find CTL script(s) at " + f.getAbsolutePath());
						}
						listSuites(setupOpts, true);
						setupOpts.getSources().clear();
					}
				}
			}
			else {
				System.out.println("No config.xml file found in TE_BASE path " + SetupOptions.getBaseConfigDirectory());
			}
		}
		else {
			listSuites(setupOpts, false);
		}
	}

	static void listSuites(SetupOptions setupOpts, boolean printSource) throws Exception {
		Index index = Generator.generateXsl(setupOpts);
		if (printSource) {
			System.out.println("Source: " + setupOpts.getSources().get(0));
		}
		for (String suiteId : index.getSuiteKeys()) {
			SuiteEntry suite = index.getSuite(suiteId);
			if (printSource) {
				System.out.print("  ");
			}
			System.out.print("Suite " + suite.getPrefix() + ":" + suite.getLocalName());
			System.out.println(" (" + suiteId + ")");
			System.out.println("  Title: " + suite.getTitle());
			String desc = suite.getDescription();
			if (desc != null) {
				System.out.println("  Description: " + desc);
			}
			String link = suite.getLink();
			if (link != null) {
				System.out.println("  Link: " + link);
			}
			System.out.println();
		}
		if (index.getSuiteKeys().isEmpty()) {
			System.out.println("No suites found.");
			System.out.println("Check the sources in config.xml or supply -source=path option(s).");
		}
		System.out.println();
	}

	static void syntax(String cmd) {
		System.out.println();
		System.out.println("Lists available test suites:");
		System.out.println();
		System.out.println(cmd + " [-source=ctlfile|dir]...\n");
	}

}
