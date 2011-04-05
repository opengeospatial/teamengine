package com.occamlab.te.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;

public class HttpURLConnectionCopy extends HttpURLConnection {
    HttpURLConnection uc = null;
    
    public HttpURLConnectionCopy(HttpURLConnection uc) {
        super(null);
        this.uc = uc;
    }

    @Override
    public InputStream getErrorStream() {
        return uc.getErrorStream();
    }

    @Override
    public String getHeaderField(int n) {
        return uc.getHeaderField(n);
    }

    @Override
    public long getHeaderFieldDate(String name, long Default) {
        return uc.getHeaderFieldDate(name, Default);
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return uc.getHeaderFieldKey(n);
    }

    @Override
    public boolean getInstanceFollowRedirects() {
        return uc.getInstanceFollowRedirects();
    }

    @Override
    public Permission getPermission() throws IOException {
        return uc.getPermission();
    }

    @Override
    public String getRequestMethod() {
        return uc.getRequestMethod();
    }

    @Override
    public int getResponseCode() throws IOException {
        return uc.getResponseCode();
    }

    @Override
    public String getResponseMessage() throws IOException {
        return uc.getResponseMessage();
    }

    @Override
    public void setChunkedStreamingMode(int chunklen) {
        uc.setChunkedStreamingMode(chunklen);
    }

    @Override
    public void setFixedLengthStreamingMode(int contentLength) {
        uc.setFixedLengthStreamingMode(contentLength);
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        uc.setInstanceFollowRedirects(followRedirects);
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        uc.setRequestMethod(method);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        uc.addRequestProperty(key, value);
    }

    @Override
    public boolean getAllowUserInteraction() {
        return uc.getAllowUserInteraction();
    }

    @Override
    public int getConnectTimeout() {
        return uc.getConnectTimeout();
    }

    @Override
    public Object getContent() throws IOException {
        return uc.getContent();
    }

    @Override
    public Object getContent(@SuppressWarnings("rawtypes") Class[] classes) throws IOException {
        return uc.getContent(classes);
    }

    @Override
    public String getContentEncoding() {
        return uc.getContentEncoding();
    }

    @Override
    public int getContentLength() {
        return uc.getContentLength();
    }

    @Override
    public String getContentType() {
        return uc.getContentType();
    }

    @Override
    public long getDate() {
        return uc.getDate();
    }

    @Override
    public boolean getDefaultUseCaches() {
        return uc.getDefaultUseCaches();
    }

    @Override
    public boolean getDoInput() {
        return uc.getDoInput();
    }

    @Override
    public boolean getDoOutput() {
        return uc.getDoOutput();
    }

    @Override
    public long getExpiration() {
        return uc.getExpiration();
    }

    @Override
    public String getHeaderField(String name) {
        return uc.getHeaderField(name);
    }

    @Override
    public int getHeaderFieldInt(String name, int Default) {
        return uc.getHeaderFieldInt(name, Default);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return uc.getHeaderFields();
    }

    @Override
    public long getIfModifiedSince() {
        return uc.getIfModifiedSince();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return uc.getInputStream();
    }

    @Override
    public long getLastModified() {
        return uc.getLastModified();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return uc.getOutputStream();
        
    }

    @Override
    public int getReadTimeout() {
        return uc.getReadTimeout();
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return uc.getRequestProperties();
    }

    @Override
    public String getRequestProperty(String key) {
        return uc.getRequestProperty(key);
    }

    @Override
    public URL getURL() {
        return uc.getURL();
    }

    @Override
    public boolean getUseCaches() {
        return uc.getUseCaches();
    }

    @Override
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        uc.setAllowUserInteraction(allowuserinteraction);
    }

    @Override
    public void setConnectTimeout(int timeout) {
        uc.setConnectTimeout(timeout);
    }

    @Override
    public void setDefaultUseCaches(boolean defaultusecaches) {
        uc.setDefaultUseCaches(defaultusecaches);
    }

    @Override
    public void setDoInput(boolean doinput) {
        uc.setDoInput(doinput);
    }

    @Override
    public void setDoOutput(boolean dooutput) {
        uc.setDoOutput(dooutput);
    }

    @Override
    public void setIfModifiedSince(long ifmodifiedsince) {
        uc.setIfModifiedSince(ifmodifiedsince);
    }

    @Override
    public void setReadTimeout(int timeout) {
        uc.setReadTimeout(timeout);
    }

    @Override
    public void setRequestProperty(String key, String value) {
        uc.setRequestProperty(key, value);
    }

    @Override
    public void setUseCaches(boolean usecaches) {
        uc.setUseCaches(usecaches);
    }

    @Override
    public String toString() {
        return uc.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return uc.equals(obj);
    }

    @Override
    public int hashCode() {
        return uc.hashCode();
    }

    @Override
    public void disconnect() {
        uc.disconnect();
    }

    @Override
    public boolean usingProxy() {
        return uc.usingProxy();
    }

    @Override
    public void connect() throws IOException {
        uc.connect();
    }
}
