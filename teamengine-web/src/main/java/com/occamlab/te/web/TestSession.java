/****************************************************************************

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

 ****************************************************************************/
package com.occamlab.te.web;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.util.DomUtils;

/**
 * Encapsulates all information pertaining to a test session.
 */
public class TestSession {
    String sessionId;
    String sourcesName;
    String description;
    String suiteName;
    ArrayList<String> profiles;

    /**
     * Creates a new test session.
     */
    public TestSession() throws Exception {
        suiteName = null;
        profiles = new ArrayList<String>();
    }

    public void save(File logdir) throws Exception {
        File sessionDir = new File(logdir, sessionId);
        sessionDir.mkdir();
        PrintStream out = new PrintStream(new File(sessionDir, "session.xml"));
        out.println("<session id=\"" + sessionId + "\" sourcesId=\""
                + sourcesName + "\">");
        out.println("<suite>" + suiteName + "</suite>");
        for (String profile : profiles) {
            out.println("<profile>" + profile + "</profile>");
        }
        out.println("<description>" + description + "</description>");
        out.println("</session>");
    }

    /**
     * Creates a test session from a previous run.
     */
    public void load(File logdir, String sessionId) throws Exception {
        this.sessionId = sessionId;
        File sessionDir = new File(logdir, sessionId);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(sessionDir, "session.xml"));
        Element session = (Element) (doc.getElementsByTagName("session")
                .item(0));
        setSourcesName(session.getAttribute("sourcesId"));
        Element suite = DomUtils.getElementByTagName(session, "suite");
        setSuiteName(suite.getTextContent());
        for (Element profile : DomUtils
                .getElementsByTagName(session, "profile")) {
            profiles.add(profile.getTextContent());
        }
        Element description = (Element) (session
                .getElementsByTagName("description").item(0));
        this.description = description.getTextContent();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(ArrayList<String> profiles) {
        this.profiles = profiles;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSourcesName() {
        return sourcesName;
    }

    public void setSourcesName(String sourcesName) {
        this.sourcesName = sourcesName;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

}
