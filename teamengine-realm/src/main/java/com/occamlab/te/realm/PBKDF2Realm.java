/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * ***********************************************************************
 *
 * Version Date: January 8, 2018
 *
 * Contributor(s):
 *     C. Heazel (WiSC): Applied modifications to address Fortify issues
 *
 * ***********************************************************************
 */

package com.occamlab.te.realm;

/*-
 * #%L
 * TEAM Engine - Tomcat Realm
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
import java.lang.reflect.Constructor;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.catalina.Realm;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.occamlab.te.realm.PasswordStorage.CannotPerformOperationException;
import com.occamlab.te.realm.PasswordStorage.InvalidHashException;

/**
 * A custom Tomcat Realm implementation that reads user information from an XML file
 * located in a sub-directory of the TEAMengine users directory. A sample representation
 * is shown below.
 *
 * <pre>
 * &lt;user&gt;
 *   &lt;name&gt;p.fogg&lt;/name&gt;
 *   &lt;roles&gt;
 *     &lt;name&gt;user&lt;/name&gt;
 *   &lt;/roles&gt;
 *   &lt;password&gt;password-digest&lt;/password&gt;
 *   &lt;email&gt;p.fogg@example.org&lt;/email&gt;
 * &lt;/user&gt;
 * </pre>
 * <p>
 * The password digest must be generated using the {@link PasswordStorage PBKDF2}
 * function; it consists of five fields separated by the colon (':') character. For
 * example:
 * <code>sha1:64000:18:a6BHX18eMTR1WnCvyR6NzG6VMJcdJE2D:8qPU0jpdPIapbyC+H5dqiaNE</code>
 * </p>
 *
 * @see <a href="https://github.com/defuse/password-hashing">Secure Password Storage
 * v2.0</a>
 *
 * <p>
 * Modified to address Fortity path manipulation errors by C. Heazel February 26, 2018
 * </p>
 */
public class PBKDF2Realm extends RealmBase {

	private static final Logger LOGR = Logger.getLogger(PBKDF2Realm.class.getName());

	private DocumentBuilder DB = null;

	private final HashMap<String, Principal> principals = new HashMap<>();

	private String password;

	/**
	 * Return the Principal associated with the specified username and credentials, if one
	 * exists in the user data store; otherwise return null.
	 */
	@Override
	public Principal authenticate(String username, String credentials) {
		GenericPrincipal principal = (GenericPrincipal) getPrincipal(username);
		if (null != principal) {
			try {
				if (!PasswordStorage.verifyPassword(credentials, password)) {
					principal = null;
				}
			}
			catch (CannotPerformOperationException | InvalidHashException e) {
				LOGR.log(Level.WARNING, e.getMessage());
				principal = null;
			}
		}
		return principal;
	}

	@Override
	protected String getPassword(String username) {
		GenericPrincipal principal = (GenericPrincipal) getPrincipal(username);
		if (principal == null) {
			return null;
		}
		else {
			return password;
		}
	}

	/**
	 * Return the Principal associated with the given user name. If the user name starts
	 * with an asterisk (*), the credentials are refreshed without having to restart
	 * Tomcat.
	 */
	@Override
	protected Principal getPrincipal(String username) {
		Principal principal;

		// force lookup of user info
		if (username.startsWith("*")) {
			principal = readPrincipal(username.substring(1));
			if (principal != null) {
				synchronized (principals) {
					principals.put(username.substring(1), principal);
				}
			}
		}

		synchronized (principals) {
			principal = (Principal) principals.get(username);
		}
		if (principal == null) {
			principal = readPrincipal(username);
			if (principal != null) {
				synchronized (principals) {
					principals.put(username, principal);
				}
			}
		}
		return principal;
	}

	void setPassword(String password) {
		this.password = password;
	}

	private GenericPrincipal readPrincipal(String username) {
		List<String> roles = new ArrayList<>();
		File usersdir = new File(System.getProperty("TE_BASE"), "users");
		File userfile = new File(new File(usersdir, username), "user.xml");
		if (!userfile.canRead()) {
			return null;
		}
		Document userInfo = null;
		try {
			if (DB == null) {
				DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			}
			userInfo = DB.parse(userfile);
		}
		catch (Exception e) {
			LOGR.log(Level.WARNING, "Failed to read user info at " + userfile.getAbsolutePath(), e);
			// fortify Mod: If the user info was not read, then there is no point in
			// continuing
			return null;
		}
		Element userElement = (Element) (userInfo.getElementsByTagName("user").item(0));
		Element passwordElement = (Element) (userElement.getElementsByTagName("password").item(0));
		password = passwordElement.getTextContent();
		Element rolesElement = (Element) (userElement.getElementsByTagName("roles").item(0));
		NodeList roleElements = rolesElement.getElementsByTagName("name");
		for (int i = 0; i < roleElements.getLength(); i++) {
			String name = ((Element) roleElements.item(i)).getTextContent();
			roles.add(name);
		}
		return createGenericPrincipal(username, roles);
	}

	/**
	 * Creates a new GenericPrincipal representing the specified user.
	 * @param username The username for this user.
	 * @param roles The set of roles (specified using String values) associated with this
	 * user.
	 * @return A GenericPrincipal for use by this Realm implementation.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	GenericPrincipal createGenericPrincipal(String username, List<String> roles) {
		Class klass = null;
		try {
			klass = Class.forName("org.apache.catalina.realm.GenericPrincipal");
		}
		catch (ClassNotFoundException ex) {
			LOGR.log(Level.SEVERE, ex.getMessage());
			// Fortify Mod: If klass is not populated, then there is no point in
			// continuing
			return null;
		}
		Constructor[] ctors = klass.getConstructors();
		Class firstParamType = ctors[0].getParameterTypes()[0];
		Class[] paramTypes = new Class[] { Realm.class, String.class, List.class };
		Object[] ctorArgs = new Object[] { this, username, roles };
		GenericPrincipal principal = null;
		try {
			if (Realm.class.isAssignableFrom(firstParamType)) {
				// Tomcat 6
				Constructor ctor = klass.getConstructor(paramTypes);
				principal = (GenericPrincipal) ctor.newInstance(ctorArgs);
			}
			else {
				// Realm parameter removed in Tomcat 7
				Constructor ctor = klass.getConstructor(Arrays.copyOfRange(paramTypes, 1, paramTypes.length));
				principal = (GenericPrincipal) ctor.newInstance(Arrays.copyOfRange(ctorArgs, 1, ctorArgs.length));
			}
		}
		catch (Exception ex) {
			LOGR.log(Level.WARNING, ex.getMessage());
		}
		return principal;
	}

}
