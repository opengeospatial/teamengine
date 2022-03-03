/*
 * The Open Geospatial Consortium licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package com.occamlab.te.web;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class CachedHttpURLConnection extends HttpURLConnectionCopy {
    byte[] content = null;

    public CachedHttpURLConnection(HttpURLConnectionCopy uc) {
        super(uc);
    }

    public CachedHttpURLConnection(HttpURLConnection uc) {
        this(new HttpURLConnectionCopy(uc));
    }

    public void connect() throws IOException {
        super.connect();
        BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = bis.read();
        while (i >= 0) {
            baos.write(i);
            i = bis.read();
        }
        bis.close();
        baos.close();
        content = baos.toByteArray();
    }

    public InputStream getInputStream() throws IOException {
        if (content == null) {
            connect();
        }
        return new ByteArrayInputStream(content);
    }

    public int getLength() throws IOException {
        if (content == null) {
            connect();
        }
        return content.length;
    }
}
