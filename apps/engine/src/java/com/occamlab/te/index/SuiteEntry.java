package com.occamlab.te.index;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.occamlab.te.Engine;
import com.occamlab.te.Test;
import com.occamlab.te.util.DomUtils;

public class SuiteEntry extends IndexEntry {
    QName startingTest;
    Document form = null;
    
    public SuiteEntry() {
        super();
    }

    SuiteEntry(Element suite) throws Exception {
        super(suite);
        Element e = DomUtils.getElementByTagName(suite, "starting-test");
        String prefix = e.getAttribute("prefix");
        String namespaceUri = e.getAttribute("namespace-uri");
        String localName = e.getAttribute("local-name");
        startingTest = new QName(namespaceUri, localName, prefix);
        Element form_e = DomUtils.getElementByTagNameNS(suite, Test.CTL_NS, "form");
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

}
