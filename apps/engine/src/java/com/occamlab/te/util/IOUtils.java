package com.occamlab.te.util;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Provides various utility methods to read/write from files and streams.
 * 
 * @author jparrpearson
 */
public class IOUtils {

	/**
	 * Converts an InputStream to a String
	 *
	 */
	public static String inputStreamToString(InputStream in) {
	    	StringBuffer buffer = new StringBuffer();
	    	try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in), 1024);
			char[] cbuf = new char[1024];
			int bytesRead;
			while ((bytesRead = br.read(cbuf, 0, cbuf.length)) != -1) {
				buffer.append(cbuf, 0, bytesRead);
			}
		} catch (Exception e) {
			System.out.println("ERROR: "+e.getMessage());
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
			while((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {
			System.out.println("ERROR: "+e.getMessage());
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
	   		System.out.println("ERROR: "+e.getMessage());
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
			System.out.println("ERROR: "+e.getMessage());
			return null;
		}
		return obj;
	}

}