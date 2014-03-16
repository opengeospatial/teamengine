package com.occamlab.te.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * Provides various utility methods to read/write from files and streams.
 * 
 * @author jparrpearson
 */
public class IOUtils {

    private static final Logger LOGR = Logger
            .getLogger(IOUtils.class.getName());

    /**
     * Converts an org.w3c.dom.Document element to an java.io.InputStream.
     * 
     * @param edoc
     *            the org.w3c.dom.Document to be converted
     * @return InputStream the InputStream value of the passed doument
     */
    public static InputStream DocumentToInputStream(Document edoc)
            throws IOException {

        // Create the input and output for use in the transformation
        final org.w3c.dom.Document doc = edoc;
        final PipedOutputStream pos = new PipedOutputStream();
        PipedInputStream pis = new PipedInputStream();
        pis.connect(pos);

        (new Thread(new Runnable() {

            public void run() {
                // Use the Transformer.transform() method to save the Document
                // to a StreamResult
                try {
                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer();
                    transformer.setOutputProperty("encoding", "UTF-8");
                    transformer.setOutputProperty("indent", "yes");
                    transformer.transform(new DOMSource(doc), new StreamResult(
                            pos));
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error converting Document to InputStream.  "
                                    + e.getMessage());
                } finally {
                    try {
                        pos.close();
                    } catch (IOException e) {

                    }
                }
            }
        }, "IOUtils.DocumentToInputStream(Document edoc)")).start();

        return pis;
    }

    /**
     * Reads the content of an input stream into a String value using the UTF-8
     * charset.
     * 
     * @param in
     *            The InputStream from which the content is read.
     * @return A String representing the content of the input stream; an empty
     *         string is returned if an error occurs.
     */
    public static String inputStreamToString(InputStream in) {
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    "UTF-8"), 1024);
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException iox) {
            LOGR.warning(iox.getMessage());
        }
        return buffer.toString();
    }

    /**
     * Converts an InputStream to a byte[]
     * 
     */
    public static byte[] inputStreamToBytes(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            System.out.println("Error converting InputStream to byte[]: "
                    + e.getMessage());
        }
        return out.toByteArray();
    }

    /**
     * Writes a generic object to a file
     */
    public static boolean writeObjectToFile(Object obj, File f) {
        try {
            FileOutputStream fout = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(obj);
            oos.close();
        } catch (Exception e) {
            System.out.println("Error writing Object to file: "
                    + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Writes a byte[] to a file
     */
    public static boolean writeBytesToFile(byte[] bytes, File f) {
        try {
            FileOutputStream fout = new FileOutputStream(f);
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            System.out.println("Error writing byte[] to file: "
                    + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Reads in a file that contains only an object
     */
    public static Object readObjectFromFile(File f) {
        Object obj = null;
        try {
            FileInputStream fin = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fin);
            obj = ois.readObject();
            ois.close();
        } catch (Exception e) {
            System.out.println("Error reading Object from file: "
                    + e.getMessage());
            return null;
        }
        return obj;
    }

    /**
     * Reads in a file as a byte[]
     */
    public static byte[] readBytesFromFile(File f) {
        byte[] bytes = null;
        try {
            int filesize = (int) f.length();
            bytes = new byte[filesize];
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            in.readFully(bytes);
            in.close();
        } catch (Exception e) {
            System.out.println("Error reading byte[] from file: "
                    + e.getMessage());
            return null;
        }
        return bytes;
    }

    /**
     * Polls a file periodically until it 1) exists or 2) the timeout is
     * exceeded (returns null) Reads the file as a java Object
     */
    public static Object pollFile(File file, int timeout, int interval)
            throws InterruptedException {
        // Convert time from s to ms for Thread
        int fullTimeout = Math.round(timeout * 1000);

        // Split up the timeout to poll every x amount of time
        int timeoutShard = Math.round(fullTimeout / interval);

        // Poll until file exists, return if it exists
        for (int i = 0; i < interval; i++) {
            Thread.sleep(timeoutShard);
            if (file.exists()) {
                return readObjectFromFile(file);
            }
        }

        // Return null if time is up and still no file
        return null;
    }

    public static Object pollFile(File file, int timeout)
            throws InterruptedException {
        return pollFile(file, timeout, 25);
    }

}
