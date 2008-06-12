package com.occamlab.te.index;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestEntry extends IndexEntry implements TemplateEntry {
    File templateFile;
    boolean usesContext;
    List<QName> params = null;
    String assertion;

    TestEntry() {
        super();
    }

    TestEntry(Element test) {
        super(test);
        try {
            setTemplateFile(new File(new URI(test.getAttribute("file"))));
            NodeList nl = test.getElementsByTagName("param");
            if (nl.getLength() > 0) {
                params = new ArrayList<QName>();
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (Element)nl.item(i);
                    String prefix = el.getAttribute("prefix");
                    String namespaceUri = el.getAttribute("namespace-uri");
                    String localName = el.getAttribute("local-name");
                    params.add(new QName(namespaceUri, localName, prefix));
                }
            }
            setUsesContext(Boolean.parseBoolean(test.getAttribute("uses-context")));
            setAssertion(test.getElementsByTagName("assertion").item(0).getTextContent());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAssertion() {
        return assertion;
    }

    public void setAssertion(String assertion) {
        this.assertion = assertion;
    }

    public File getTemplateFile() {
        return templateFile;
    }
    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    public List<QName> getParams() {
        return params;
    }

    public void setParams(List<QName> params) {
        this.params = params;
    }

    public boolean usesContext() {
        return usesContext;
    }

    public void setUsesContext(boolean usesContext) {
        this.usesContext = usesContext;
    }
}
