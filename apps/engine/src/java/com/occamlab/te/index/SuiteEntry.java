package com.occamlab.te.index;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

public class SuiteEntry extends IndexEntry {
    QName startingTest;
    
    SuiteEntry() {
        super();
    }

    SuiteEntry(Element suite) {
        super(suite);
        Element e = (Element)suite.getElementsByTagName("starting-test").item(0);
        String prefix = e.getAttribute("prefix");
        String namespaceUri = e.getAttribute("namespace-uri");
        String localName = e.getAttribute("local-name");
        startingTest = new QName(namespaceUri, localName, prefix);
    }

    public QName getStartingTest() {
        return startingTest;
    }

    public void setStartingTest(QName startingTest) {
        this.startingTest = startingTest;
    }

}
