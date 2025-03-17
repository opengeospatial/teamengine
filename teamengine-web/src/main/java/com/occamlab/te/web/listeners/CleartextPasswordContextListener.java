/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.web.listeners;

/*-
 * #%L
 * TEAM Engine - Web Application
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
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.impl.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import com.occamlab.te.SetupOptions;
import com.occamlab.te.realm.PasswordStorage;

/**
 * A context listener that checks for the presence of clear text passwords in user info
 * files (<code>{TE_BASE}/users/{userid}/user.xml</code>). If one is found that does not
 * conform to the expected hash format, it is replaced with the corresponding hash.
 * <p>
 * The password digest must be generated using the {@link PasswordStorage PBKDF2}
 * function; it consists of five fields separated by the colon (':') character. For
 * example:
 * <code>sha1:64000:18:a6BHX18eMTR1WnCvyR6NzG6VMJcdJE2D:8qPU0jpdPIapbyC+H5dqiaNE</code>
 * </p>
 *
 * Version Date: January 4, 2018
 *
 * Contributor(s): C. Heazel (WiSC) Applied corrections to address Fortify issues
 */
@WebListener
public class CleartextPasswordContextListener implements ServletContextListener {

	private static final Logger LOGR = Logger.getLogger(CleartextPasswordContextListener.class.getPackage().getName());

	@Override
	public void contextDestroyed(ServletContextEvent evt) {
	}

	/**
	 * Checks that passwords in the user files are not in clear text. If a hash value is
	 * found for some user, it is assumed that all user passwords have previously been
	 * hashed and no further checks are done.
	 */
	@Override
	public void contextInitialized(ServletContextEvent evt) {
		File usersDir = new File(SetupOptions.getBaseConfigDirectory(), "users");
		if (!usersDir.isDirectory()) {
			return;
		}
		DocumentBuilder domBuilder = null;
		try {
			domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			LOGR.warning(e.getMessage());
			return;
		}
		DOMImplementationLS lsFactory = buildDOM3LoadAndSaveFactory();
		LSSerializer serializer = lsFactory.createLSSerializer();
		serializer.getDomConfig().setParameter(Constants.DOM_XMLDECL, Boolean.FALSE);
		serializer.getDomConfig().setParameter(Constants.DOM_FORMAT_PRETTY_PRINT, Boolean.TRUE);
		LSOutput output = lsFactory.createLSOutput();
		output.setEncoding("UTF-8");
		for (File userDir : usersDir.listFiles()) {
			File userFile = new File(userDir, "user.xml");
			if (!userFile.isFile()) {
				continue;
			}
			try {
				Document doc = domBuilder.parse(userFile);
				Node pwNode = doc.getElementsByTagName("password").item(0);
				if (null == pwNode) {
					continue;
				}
				String password = pwNode.getTextContent();
				if (password.split(":").length == 5) {
					break;
				}
				pwNode.setTextContent(PasswordStorage.createHash(password));
				// overwrite contents of file
				// Fortify Mod: Make sure that the output stream is closed
				// output.setByteStream(new FileOutputStream(userFile, false));
				FileOutputStream os = new FileOutputStream(userFile, false);
				output.setByteStream(os);
				serializer.write(doc, output);
				os.close();
			}
			catch (Exception e) {
				LOGR.info(e.getMessage());
				continue;
			}
		}
	}

	/**
	 * Builds a DOMImplementationLS factory that supports the "DOM Level 3 Load and Save"
	 * specification. It provides various factory methods for creating the objects
	 * required for loading and saving DOM nodes.
	 * @return A factory object, or <code>null</code> if one cannot be created (in which
	 * case a warning will be logged).
	 *
	 * @see <a href="https://www.w3.org/TR/DOM-Level-3-LS/">Document Object Model (DOM)
	 * Level 3 Load and Save Specification, Version 1.0</a>
	 */
	DOMImplementationLS buildDOM3LoadAndSaveFactory() {
		DOMImplementationLS factory = null;
		try {
			DOMImplementationRegistry domRegistry = DOMImplementationRegistry.newInstance();
			factory = (DOMImplementationLS) domRegistry.getDOMImplementation("LS 3.0");
		}
		catch (Exception e) {
			LOGR.log(Level.WARNING, "Failed to create DOMImplementationLS", e);
		}
		return factory;
	}

}
