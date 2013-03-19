package com.occamlab.te.web;

import java.io.File;

import org.w3c.dom.Element;

import com.occamlab.te.TECore;

import net.sf.saxon.expr.XPathContext;

public class MonitorCall {
    XPathContext context;
    String url;
    String localName;
    String NamespaceURI;
    Element params;
    String callId;
    Element parserInstruction;
    boolean modifiesResponse;
    TECore core;
    String testPath;
    private CoverageMonitor coverageMonitor;

    MonitorCall(String url) {
        setUrl(url);
    }

    public XPathContext getContext() {
        return context;
    }

    public void setContext(XPathContext context) {
        this.context = context;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getNamespaceURI() {
        return NamespaceURI;
    }

    public void setNamespaceURI(String namespaceURI) {
        NamespaceURI = namespaceURI;
    }

    public Element getParams() {
        return params;
    }

    public void setParams(Element params) {
        this.params = params;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Element getParserInstruction() {
        return parserInstruction;
    }

    public void setParserInstruction(Element parserInstruction) {
        this.parserInstruction = parserInstruction;
    }

    public boolean getModifiesResponse() {
        return modifiesResponse;
    }

    public void setModifiesResponse(boolean modifiesResponse) {
        this.modifiesResponse = modifiesResponse;
    }

    public TECore getCore() {
        return core;
    }

    public void setCore(TECore core) {
        this.core = core;
        this.testPath = core.getTestPath();
        this.coverageMonitor = new CoverageMonitor(this.url, new File(
                core.getLogDir(), core.getTestPath()));
    }

    public String getTestPath() {
        return testPath;
    }

    /**
     * Determines which server capabilities are exercised by this query.
     * 
     * @param query
     *            The (decoded) query string extracted from the request URI.
     */
    public void checkCoverage(String query) {
        this.coverageMonitor.inspectQuery(query);
    }

    @Override
    public String toString() {
        return "MonitorCall [url=" + url + ", localName=" + localName
                + ", NamespaceURI=" + NamespaceURI + ", callId=" + callId
                + ", modifiesResponse=" + modifiesResponse + ", testPath="
                + testPath + "]";
    }

}
