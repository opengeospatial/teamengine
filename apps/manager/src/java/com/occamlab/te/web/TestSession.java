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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.Test;
import com.occamlab.te.TECore;

public class TestSession implements Runnable, HttpSessionBindingListener {
  Test TestClass;
  File LogDir;
  String SourcesId;
  String SessionId;
  String Description;
  String Suite;
  int Mode;
  ArrayList TestList;
  TECore Core;
  ByteArrayOutputStream Out;
  Thread ActiveThread = null;
  boolean Complete = false;

  static TestSession create(File logdir, String sourcesId, String suite, String description) throws Exception {
    TestSession s = new TestSession();
    s.LogDir = logdir;
    s.SourcesId = sourcesId;
    s.SessionId = Test.newSessionId(logdir);
    s.Suite = suite;
    s.Description = description;
    File dir = new File(logdir, s.SessionId);
    dir.mkdir();
    PrintStream out = new PrintStream(new File(dir, "session.xml"));
    out.println("<session id=\"" + s.SessionId + "\" sourcesId=\"" + s.SourcesId + "\">");
    out.println("<suite>" + s.Suite + "</suite>");
    out.println("<description>" + s.Description + "</description>");
    out.println("</session>");
    return s;
  }

  public static TestSession load(DocumentBuilder db, File logdir, String sessionId) throws Exception {
    TestSession s = new TestSession();
    s.SessionId = sessionId;
    File dir = new File(logdir, s.SessionId);
    Document doc = db.parse(new File(dir, "session.xml"));
    Element session = (Element)(doc.getElementsByTagName("session").item(0));
    s.SourcesId = session.getAttribute("sourcesId");
    Element suite = (Element)(session.getElementsByTagName("suite").item(0));
    s.Suite = suite.getTextContent();
    Element description = (Element)(session.getElementsByTagName("description").item(0));
    s.Description = description.getTextContent();
    s.LogDir = logdir;
    return s;
  }
  
  void prepare(Map testClasses, int mode) {
    TestClass = (Test)testClasses.get(SourcesId);
    Mode = mode;
    TestList = new ArrayList();
  }
  
  void prepare(Map testClasses, int mode, String test) {
    prepare(testClasses, mode);
    if (mode == Test.RETEST_MODE) {
      TestList.add(test);
    }
  }
  
  synchronized public String getOutput() {
    String output = Out.toString();
    Out.reset();
    return output;
  }
  
  public boolean isComplete() {
    return Complete;
  }
  
  public TECore getCore() {
    return Core;
  }

  public String getSessionId() {
    return SessionId;
  }

  public String getDescription() {
    return Description;
  }

  public void run() {
    try {
      ActiveThread = Thread.currentThread();
      LogDir.mkdir();
      Out = new ByteArrayOutputStream();
      PrintStream ps = new PrintStream(Out);
      Core = new TECore(ps, true);
      TestClass.test(Mode, LogDir, Suite, SessionId, TestList, Core);
      ps.close();
    } catch(Exception e) {
      e.printStackTrace(System.out);
    }
    ActiveThread = null;
    Complete = true;
  }
  
  public void valueBound(HttpSessionBindingEvent e) {
  }

  public void valueUnbound(HttpSessionBindingEvent e) {
    if (e.getName().equals("testsession")) {
      if (ActiveThread != null) {
        ActiveThread.interrupt();
      }
    }
  }
}
