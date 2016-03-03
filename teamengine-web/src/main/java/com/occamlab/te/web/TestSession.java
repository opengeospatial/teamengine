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

import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates all information pertaining to a test session.
 */
public class TestSession {
    String sessionId;
    String sourcesName;
    String description;
    String suiteName;
    String currentDate;
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
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        currentDate=dateFormat.format(date);
        PrintStream out = new PrintStream(new File(sessionDir, "session.xml"));
        out.println("<session id=\"" + sessionId + "\" sourcesId=\""
                + sourcesName +"\" date=\""+currentDate+"\"  >");
        out.println("<suite>" + suiteName + "</suite>");
        for (String profile : profiles) {
            out.println("<profile>" + profile + "</profile>");
        }
        
        String description_data;
        description_data = StringUtils.escapeXML(description);
        
        out.println("<description>" + description_data + "</description>");
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
        if(null!=session.getAttribute("date")){
        setCurrentDate(session.getAttribute("date"));
        }else{
          setCurrentDate("");
        }
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
    
    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

}
