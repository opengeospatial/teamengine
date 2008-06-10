package com.occamlab.te.index;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FunctionEntry extends IndexEntry implements TemplateEntry {
    boolean isJava;
    boolean initialized;
    boolean usesContext;
    String className;
    String method;
    Object instance;
    int minArgs;
    int maxArgs;
    List<QName> params = null;
    Map<QName, String> classParams = null;
    File templateFile;

    FunctionEntry() {
        super();
    }

    FunctionEntry(Element function) {
        super(function);
        try {
            String type = function.getAttribute("type");
            if (type.equals("xsl")) {
                setJava(false);
                setTemplateFile(new File(new URI(function.getAttribute("file"))));
            } else if (type.equals("java")) {
                setJava(true);
            } else {
                throw new RuntimeException("Invalid function type");
            }
            NodeList nl = function.getElementsByTagName("param");
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
            setUsesContext(Boolean.parseBoolean(function.getAttribute("uses-context")));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public File getTemplateFile() {
        return templateFile;
    }
    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public boolean isInitialized() {
        return initialized;
    }
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    public Object getInstance() {
        return instance;
    }
    public void setInstance(Object instance) {
        this.instance = instance;
    }
    public boolean isJava() {
        return isJava;
    }
    public void setJava(boolean isJava) {
        this.isJava = isJava;
    }
    public int getMaxArgs() {
        return maxArgs;
    }
    public void setMaxArgs(int maxArgs) {
        this.maxArgs = maxArgs;
    }
    public int getMinArgs() {
        return minArgs;
    }
    public void setMinArgs(int minArgs) {
        this.minArgs = minArgs;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public Map<QName, String> getClassParams() {
        return classParams;
    }
    public void setClassParams(Map<QName, String> classParams) {
        this.classParams = classParams;
    }
    public List<QName> getParams() {
        return params;
    }
    public void setParams(List<QName> params) {
        this.params = params;
    }
    public boolean isUsesContext() {
        return usesContext;
    }
    public void setUsesContext(boolean usesContext) {
        this.usesContext = usesContext;
    }
}

