package com.occamlab.te.spi.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.occamlab.te.spi.jaxrs.TestSuiteController;

public class TestRunUtils {

    public static String getUserName(List<String> authCredentials) {
        String authCredential = authCredentials.get(0);
        final String encodedUserPassword = authCredential.replaceFirst("Basic" + " ", "");
        String usernameAndPassword = null;
        
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        return username;
    }
    
    public static void save(File outputDir, String sessionId, String sourcesId) {
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        File sessionDir = new File(outputDir, sessionId);
        sessionDir.mkdir();
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
        Date date = new Date();
        String currentDate = dateFormat.format(date);
        
        PrintStream out = null;
        try {
            out = new PrintStream(new File(sessionDir, "session.xml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.println("<session id=\"" + sessionId + "\" sourcesId=\""
                + sourcesId +"\" date=\""+currentDate+"\"  >");
        out.println("</session>");
        out.close();
    }
    
    public static String getSourcesId(TestSuiteController controller) {
        String sourcesId = "OGC_" + controller.getTitle() + "_" + controller.getVersion();
        return sourcesId;
    }

}
