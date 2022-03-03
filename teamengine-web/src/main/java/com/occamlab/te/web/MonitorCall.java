/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.web;

import java.io.File;

import org.w3c.dom.Element;

import com.occamlab.te.TECore;
import com.occamlab.te.util.DomUtils;

import net.sf.saxon.expr.XPathContext;

/**
 * A monitor that examines the content of a service request. It may be
 * configured to modify the response before forwarding it to the requester. A
 * monitor may also perform simple coverage reporting by keeping track of which
 * service capabilities were invoked by the client (for GET requests only).
 *
 */
public class MonitorCall {
    XPathContext context;
    String url;
    String localName;
    String namespaceURI;
    Element params;
    String callId;
    Element parserConfig;
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
        return namespaceURI;
    }

    public void setNamespaceURI(String namespaceURI) {
        this.namespaceURI = namespaceURI;
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
        return parserConfig;
    }

    public void setParserInstruction(Element parserInstruction) {
        this.parserConfig = parserInstruction;
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
        if (null == this.namespaceURI) {
            throw new RuntimeException(
                    "Cannot create coverage monitor: namespaceURI is null.");
        }
        this.coverageMonitor = new CoverageMonitor(this.namespaceURI);
        this.coverageMonitor.setTestSessionDir(new File(core.getLogDir(), core
                .getTestPath()));
    }

    public String getTestPath() {
        return testPath;
    }

    /**
     * Determines which server capabilities are invoked by this query.
     *
     * @param query
     *            The (decoded) query string extracted from the request URI.
     */
    public void checkCoverage(String query) {
        this.coverageMonitor.inspectQuery(query);
    }

    /**
     * Ensures that any resources used by this monitor are cleaned up in an
     * orderly manner.
     */
    public void destroy() {
        if (null != this.coverageMonitor) {
            this.coverageMonitor.writeCoverageResults();
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("MonitorCall {\n");
        str.append(" url: ").append(url).append(",\n");
        str.append(" localName: ").append(localName).append(",\n");
        str.append(" NamespaceURI: ").append(namespaceURI).append(",\n");
        str.append(" callId: ").append(callId).append(",\n");
        str.append(" modifiesResponse: ").append(modifiesResponse)
                .append(",\n");
        str.append(" testPath: ").append(testPath).append(",\n");
        str.append(" params: ").append(DomUtils.serializeNode(params))
                .append(",\n");
        str.append(" parserConfig: ")
                .append(DomUtils.serializeNode(parserConfig)).append(",\n");
        str.append("}");
        return str.toString();
    }

}
