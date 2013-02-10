package com.occamlab.te.index;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.Test;
import com.occamlab.te.util.DomUtils;

public class SuiteEntry extends IndexEntry {
    String defaultResult = "Pass";
    QName startingTest;
    Document form = null;
    String title = null;
    String description = null;
    String link;
    String dataLink;

    public SuiteEntry() {
        super();
    }

    SuiteEntry(Element suite) throws Exception {
        super(suite);
        title = DomUtils.getElementByTagName(suite, "title").getTextContent();
        description = DomUtils.getElementByTagName(suite, "description")
                .getTextContent();
        Element e = DomUtils.getElementByTagName(suite, "starting-test");
        String prefix = e.getAttribute("prefix");
        String namespaceUri = e.getAttribute("namespace-uri");
        String localName = e.getAttribute("local-name");
        setDefaultResult(DomUtils.getElementByTagName(suite, "defaultResult")
                .getTextContent());
        startingTest = new QName(namespaceUri, localName, prefix);
        Element form_e = DomUtils.getElementByTagNameNS(suite, Test.CTL_NS,
                "form");
        if (form_e != null) {
            form = DomUtils.createDocument(form_e);
        }
    }

    public QName getStartingTest() {
        return startingTest;
    }

    public void setStartingTest(QName startingTest) {
        this.startingTest = startingTest;
    }

    public Document getForm() {
        return form;
    }

    public void setForm(Document form) {
        this.form = form;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDataLink() {
        return dataLink;
    }

    public void setDataLink(String dataLink) {
        this.dataLink = dataLink;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDefaultResult() {
        return defaultResult;
    }

    public void setDefaultResult(String defaultResult) {
        this.defaultResult = defaultResult;
    }

}
