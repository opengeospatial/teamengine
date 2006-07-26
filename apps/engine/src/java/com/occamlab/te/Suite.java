package com.occamlab.te;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Suite {
  private String prefix;
  private String namespaceUri;
  private String localName;
  private String title;
  private String description;
  private String startingTestPrefix;
  private String startingTestNamespaceUri;
  private String startingTestLocalName;
  private String link;
  
  public Suite(Element suiteElement) {
    String name = suiteElement.getAttribute("name");
    int colon = name.indexOf(":");
    prefix = name.substring(0, colon);
    localName = name.substring(colon + 1);
    namespaceUri = suiteElement.lookupNamespaceURI(prefix);

    NodeList titleElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "title");
    title = ((Element)titleElements.item(0)).getTextContent();

    NodeList descElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "description");
    if (descElements.getLength() > 0) {
      description = ((Element)descElements.item(0)).getTextContent();
    } else {
      description = null;
    }

    NodeList linkElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "link");
    if (linkElements.getLength() > 0) {
      link = ((Element)linkElements.item(0)).getTextContent();
    } else {
      link = null;
    }

    NodeList startingTestElements = suiteElement.getElementsByTagNameNS(Test.CTL_NS, "starting-test");
    name = ((Element)startingTestElements.item(0)).getTextContent();
    colon = name.indexOf(":");
    startingTestPrefix = name.substring(0, colon);
    startingTestLocalName = name.substring(colon + 1);
    startingTestNamespaceUri = suiteElement.lookupNamespaceURI(startingTestPrefix);
  }
  
  public String getKey() {
    return namespaceUri + "," + localName;
  }
  
  public String getPrefix() {
    return prefix;
  }
  
  public String getNamespaceUri() {
    return namespaceUri;
  }
  
  public String getLocalName() {
    return localName;
  }
  
  public String getTitle() {
    return title;
  }
  
  public String getDescription() {
    return description;
  }
  
  public String getLink() {
    return link;
  }
  
  public String getStartingTestPrefix() {
    return startingTestPrefix;
  }
  
  public String getStartingTestNamespaceUri() {
    return startingTestNamespaceUri;
  }
  
  public String getStartingTestLocalName() {
    return startingTestLocalName;
  }
}
