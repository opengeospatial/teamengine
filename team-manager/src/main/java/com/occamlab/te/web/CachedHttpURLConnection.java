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
