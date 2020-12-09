/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *****************************************************************************

 The Original Code is TEAM Engine.

 The Initial Developer of the Original Code is Northrop Grumman Corporation
 jointly with The National Technology Alliance.  Portions created by
 Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
 Grumman Corporation. All Rights Reserved.

 Contributor(s):
 	C. Heazel (WiSC): Added Fortify adjudication changes

 ****************************************************************************/
package com.occamlab.te.web;

import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates all information pertaining to a test session.
 */
public class TestSession implements  Comparable<TestSession> {


    private static Logger LOGR = Logger.getLogger( TestSession.class.getName() );

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
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
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
        // Fortify Mod: flush and close the PrintStream
        out.close();
    }

    /**
     * Creates a test session from a previous run.
     */
    public void load(File logdir, String sessionId) throws Exception {
        this.sessionId = sessionId;
        File sessionDir = new File(logdir, sessionId);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
            // Fortify Mod: prevent external entity injection
        dbf.setExpandEntityReferences(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(sessionDir, "session.xml"));
        Element session = (Element) (doc.getElementsByTagName("session")
                .item(0));
        setSourcesName(session.getAttribute("sourcesId"));
        if(session.hasAttribute("date")){
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

    /**
     * This will return the sorted list of TestSession data according to date.
     *
     * @param testData
     *            List of all TestSessions.
     * @return Return the sorted list of testData.
     */
    public List<TestSession> getSortedMap( List<TestSession> testData ) {
        Collections.sort( testData );
        Collections.reverse( testData );
        return testData;
    }

    @Override
    public int compareTo( TestSession other ) {
        if ( other == null ) {
            return -1;
        }
        Date otherDate = convertStringToDate( other.getCurrentDate(), other.getSessionId() );
        if ( otherDate == null ) {
            return -1;
        }
        Date thisDate = convertStringToDate( this.getCurrentDate(), this.getSessionId() );
        if ( thisDate == null )
            return 1;
        return thisDate.compareTo( otherDate );
    }

    private Date convertStringToDate( String dateString, String sessionId ) {
        try {
            if ( dateString != null ) {
                Format formatter = new SimpleDateFormat( "yyyy/MM/dd  HH:mm:ss" );
                return ( (DateFormat) formatter ).parse( dateString );
            }
        } catch ( Exception e ) {
            LOGR.warning( "Could not parse date '" + dateString + "' of session with id " + sessionId );
        }
        return null;
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
