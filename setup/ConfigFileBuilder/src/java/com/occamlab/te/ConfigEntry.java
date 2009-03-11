package com.occamlab.te;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.util.DomUtils;

public class ConfigEntry {
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
    public String webdir;
    
    ConfigEntry(File file) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(file);
        Element config = DomUtils.getElementByTagName(doc, "config");
        organization = DomUtils.getElementByTagName(DomUtils.getElementByTagName(config, "organization"), "name").getTextContent();
        standard = DomUtils.getElementByTagName(DomUtils.getElementByTagName(config, "standard"), "name").getTextContent();
        version = DomUtils.getElementByTagName(DomUtils.getElementByTagName(config, "version"), "name").getTextContent();
        revision = DomUtils.getElementByTagName(DomUtils.getElementByTagName(config, "revision"), "name").getTextContent();
        Element suiteEl = DomUtils.getElementByTagName(config, "suite");
        if (suiteEl != null) {
            String localName = DomUtils.getElementByTagName(suiteEl, "local-name").getTextContent();
            String namespaceUri = DomUtils.getElementByTagName(suiteEl, "namespace-uri").getTextContent();
            String prefix = DomUtils.getElementByTagName(suiteEl, "prefix").getTextContent();
            suite = new QName(namespaceUri, localName, prefix);
            Element titleEl = DomUtils.getElementByTagName(suiteEl, "title");
            if (titleEl != null) title = titleEl.getTextContent();
            Element descriptionEl = DomUtils.getElementByTagName(suiteEl, "description");
            if (descriptionEl != null) description = descriptionEl.getTextContent();
//            link = file.getParentFile().getName();
            for (Element linkEl : DomUtils.getElementsByTagName(suiteEl, "link")) {
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
        for (Element profileEl : DomUtils.getElementsByTagName(config, "profile")) {
            String localName = DomUtils.getElementByTagName(profileEl, "local-name").getTextContent();
            String namespaceUri = DomUtils.getElementByTagName(profileEl, "namespace-uri").getTextContent();
            String prefix = DomUtils.getElementByTagName(profileEl, "prefix").getTextContent();
            profiles.add(new QName(namespaceUri, localName, prefix));
            Element titleEl = DomUtils.getElementByTagName(profileEl, "title");
            profileTitles.add(titleEl == null ? "" : titleEl.getTextContent());
            Element descriptionEl = DomUtils.getElementByTagName(profileEl, "description");
            profileDescriptions.add(descriptionEl == null ? "" : descriptionEl.getTextContent());
        }
        for (Element sourceEl : DomUtils.getElementsByTagName(config, "source")) {
            sources.add(new File(file.getParentFile(), sourceEl.getTextContent()));
        }
        Element webdirEl = DomUtils.getElementByTagName(config, "webdir");
        if (webdirEl == null ) {
            webdir = file.getParentFile().getName();
        } else {
            webdir = webdirEl.getTextContent();
        }
    }
}
