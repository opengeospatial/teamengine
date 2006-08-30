package com.occamlab.te.realm;

import org.apache.catalina.realm.RealmBase;
import org.apache.catalina.realm.GenericPrincipal;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.security.Principal;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class UserFilesRealm extends RealmBase {
  private String Root = null;
  private DocumentBuilder DB = null;
  private HashMap Principals = new HashMap();

  public String getRoot() {
    return Root;
  }

  public void setRoot(String root) {
    Root = root;
  }

  private GenericPrincipal readPrincipal(String username) {
    String password = null;
    List roles = new ArrayList();
    try {
      if (DB == null) {
        DB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      }
      File userfile = new File(new File(Root, username), "user.xml");
      Document doc = DB.parse(userfile);
      Element userElement = (Element)(doc.getElementsByTagName("user").item(0));
      Element passwordElement = (Element)(userElement.getElementsByTagName("password").item(0));
      password = passwordElement.getTextContent();
      Element rolesElement = (Element)(userElement.getElementsByTagName("roles").item(0));
      NodeList roleElements = rolesElement.getElementsByTagName("name");  
      for (int i = 0; i < roleElements.getLength(); i++) {
        String name = ((Element)roleElements.item(i)).getTextContent();
        roles.add(name);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new GenericPrincipal(this, username, password, roles);
  }

  protected String getPassword(String username) {
    GenericPrincipal principal = (GenericPrincipal)getPrincipal(username);
    if (principal == null) {
      return null;
    } else {
      return principal.getPassword();
    }
  }

  protected Principal getPrincipal(String username) {
    Principal principal;
    synchronized(Principals) {
      principal = (Principal)Principals.get(username);
    }
    if (principal == null) {
      principal = readPrincipal(username);
      synchronized(Principals) {
        Principals.put(username, principal);
      }
    }
    return principal;
  }

  protected String getName() {
    return "UserFilesRealm";
  }
}
