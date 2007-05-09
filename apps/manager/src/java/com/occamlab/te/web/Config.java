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

import com.occamlab.te.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.lang.ClassLoader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Config {
  private String Home;
  private File UsersDir;
  private LinkedHashMap SourcesHash;

  public Config() {
    try {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Document doc = db.parse(cl.getResourceAsStream("com/occamlab/te/web/config.xml"));
      Element config = (Element)(doc.getElementsByTagName("config").item(0));
      Element home = (Element)(config.getElementsByTagName("home").item(0));
      Home = home.getTextContent();
      Element usersdir = (Element)(config.getElementsByTagName("usersdir").item(0));
      UsersDir = getFile(usersdir.getTextContent());
      if (!UsersDir.isDirectory()) {
        System.out.println("Error: Directory " + usersdir.getTextContent() + " does not exist.");
      }

      File script_dir = Test.getResourceAsFile("com/occamlab/te/scripts/parsers.ctl").getParentFile();

      SourcesHash = new LinkedHashMap();
      NodeList sourcesList = config.getElementsByTagName("sources");
      for (int i = 0; i < sourcesList.getLength(); i++) {
        ArrayList list = new ArrayList();
        list.add(script_dir);
        Element sources = (Element)sourcesList.item(i);
        String id = sources.getAttribute("id"); 
        NodeList sourceList = sources.getElementsByTagName("source");  
        for (int j = 0; j < sourceList.getLength(); j++) {
          Element source = (Element)sourceList.item(j);
          File f = getFile(source.getTextContent());
          if (!f.exists()) {
            System.out.println("Error: Source " + source.getTextContent() + " does not exist.");
          }
          list.add(f);
        }
        SourcesHash.put(id, list);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public String getHome() {
    return Home;
  }
  
  public File getUsersDir() {
    return UsersDir;
  }
  
  public LinkedHashMap getSources() {
    return SourcesHash;
  }

  private File getFile(String path) {
    File f = new File(path);
    if (f.exists()) {
      return f;
    } else {
      return new File(System.getProperty("catalina.base"), path);
    }
  }
}
