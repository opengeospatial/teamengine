package com.occamlab.te.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ConfigEntry {

    public String organization = null;
    public String standard = null;
    public String version = null;
    public String revision = null;
    public QName suite = null;
    public String title = null;
    public String description = null;
    public String link = null;
    public String dataLink = null;
    public List<QName> profiles = new ArrayList<QName>();
    public List<String> profileTitles = new ArrayList<String>();
    public List<String> profileDescriptions = new ArrayList<String>();
    public List<File> sources = new ArrayList<File>();
    public File resources;
    public String webdir;

    ConfigEntry(File file) throws Exception {
        readConfigFile(file);
    }

    void readConfigFile(File file) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document doc = db.parse(file);
        Element config = doc.getDocumentElement();
        Element organizationEl = getElementByTagName(config, "organization");
        if (organizationEl != null) {
            organization = getElementByTagName(organizationEl, "name")
                    .getTextContent();
        }
        Element standardEl = getElementByTagName(config, "standard");
        if (standardEl != null) {
            standard = getElementByTagName(standardEl, "name").getTextContent();
        }
        Element versionEl = getElementByTagName(config, "version");
        if (versionEl != null) {
            version = getElementByTagName(versionEl, "name").getTextContent();
        }
        Element revisionEl = getElementByTagName(config, "revision");
        if (revisionEl != null) {
            revision = getElementByTagName(revisionEl, "name").getTextContent();
        }
        Element suiteEl = getElementByTagName(config, "suite");
        if (suiteEl != null) {
            String localName = getElementByTagName(suiteEl, "local-name")
                    .getTextContent();
            String namespaceUri = getElementByTagName(suiteEl, "namespace-uri")
                    .getTextContent();
            String prefix = getElementByTagName(suiteEl, "prefix")
                    .getTextContent();
            suite = new QName(namespaceUri, localName, prefix);
            Element titleEl = getElementByTagName(suiteEl, "title");
            if (titleEl != null) {
                title = titleEl.getTextContent();
            }
            Element descriptionEl = getElementByTagName(suiteEl, "description");
            if (descriptionEl != null) {
                description = descriptionEl.getTextContent();
            }
            NodeList linkNodes = suiteEl.getElementsByTagName("link");
            for (int i = 0; i < linkNodes.getLength(); i++) {
                Element linkEl = (Element) linkNodes.item(i);
                String value = linkEl.getTextContent();
                if ("data".equals(linkEl.getAttribute("linkType"))) {
                    dataLink = file.getParentFile().getName() + "/" + value;
                } else if (value.startsWith("data/")) {
                    dataLink = file.getParentFile().getName() + "/" + value;
                } else {
                    link = value;
                }
            }
        }
        NodeList profileNodes = config.getElementsByTagName("profile");
        for (int i = 0; i < profileNodes.getLength(); i++) {
            Element profileEl = (Element) profileNodes.item(i);
            String localName = getElementByTagName(profileEl, "local-name")
                    .getTextContent();
            String namespaceUri = getElementByTagName(profileEl,
                    "namespace-uri").getTextContent();
            String prefix = getElementByTagName(profileEl, "prefix")
                    .getTextContent();
            profiles.add(new QName(namespaceUri, localName, prefix));
            Element titleEl = getElementByTagName(profileEl, "title");
            profileTitles.add(titleEl == null ? "" : titleEl.getTextContent());
            Element descriptionEl = getElementByTagName(profileEl,
                    "description");
            profileDescriptions.add(descriptionEl == null ? "" : descriptionEl
                    .getTextContent());
        }
        NodeList sourceNodes = config.getElementsByTagName("source");
        for (int i = 0; i < sourceNodes.getLength(); i++) {
            Element sourceEl = (Element) sourceNodes.item(i);
            sources.add(new File(file.getParentFile(), sourceEl
                    .getTextContent()));
        }
        Element resourcesEl = getElementByTagName(config, "resources");
        if (resourcesEl != null) {
            resources = new File(file.getParentFile(),
                    resourcesEl.getTextContent());
        }
        webdir = file.getParentFile().getName();
        Element webEl = getElementByTagName(config, "web");
        if (webEl != null) {
            String dirname = webEl.getAttribute("dirname");
            if (dirname.length() > 0) {
                webdir = dirname;
            }
        }
    }

    void add(ConfigEntry config) {
        profiles.addAll(config.profiles);
        profileTitles.addAll(config.profileTitles);
        profileDescriptions.addAll(config.profileDescriptions);
        sources.addAll(config.sources);
    }

    Element getElementByTagName(Node node, String tagname) {
        NodeList nl;
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            nl = ((Document) node).getElementsByTagName(tagname);
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            nl = ((Element) node).getElementsByTagName(tagname);
        } else {
            return null;
        }
        if (nl.getLength() >= 0) {
            return (Element) nl.item(0);
        } else {
            return null;
        }
    }
}
