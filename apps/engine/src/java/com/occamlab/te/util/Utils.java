package com.occamlab.te.util;

import java.io.InputStream;
import java.util.Random;
import java.util.Iterator;
import java.security.MessageDigest;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

/**
 * Provides various utility methods (general collection).
 * 
 * @author jparrpearson
 */
public class Utils {

	/**
	 * Returns a random string of a certain length
	 *
	 */
	public static String randomString(int len, Random random) {
		if (len < 1) {
			return "";
		}
		int start = ' ';
		int end = 'z' + 1;

		StringBuffer buffer = new StringBuffer();
		int gap = end - start;

		while (len-- != 0) {
			char ch;
			ch = (char) (random.nextInt(gap) + start);

			if (Character.isLetterOrDigit(ch)) {
				buffer.append(ch);
			} else {
				len++;
			}
		}
		return buffer.toString();
	}

	/**
	 * Uses MD5 to create a hash value for the given String
	 *
	 */
	public static String generateMD5(String text) {
		byte[] md5hash = null;
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			md5hash = new byte[8];
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			md5hash = md.digest();
		} catch (Exception e) {
			System.out.println("ERROR: "+e.getMessage());
			return "";
		}
		return convertToHex(md5hash);
	}

	/**
	 * Converts a String to Hex digits
	 *
	 */
	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				}
				else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while(two_halfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Converts an XPointer to XPath and evaulates the result (JAXP)
	 *
	 */
	public static String evaluateXPointer(String xpointer, InputStream is) {
		String results = "";
		// Parse the XPointer into usable namespaces and XPath expressions (assumes 1 only)
		int xmlnsStart = xpointer.indexOf("xmlns(")+"xmlns(".length();
		int xmlnsEnd = xpointer.indexOf(")", xmlnsStart);
		int xpathStart = xpointer.indexOf("xpointer(")+"xpointer(".length();
		int xpathEnd = xpointer.indexOf(")", xpathStart);
		String xmlnsStr = xpointer.substring(xmlnsStart, xmlnsEnd);
		String xpathStr = xpointer.substring(xpathStart, xpathEnd);
		//System.out.println("xmlnsStr: "+xmlnsStr+" xpathStr: "+xpathStr);
	        try {
	        	XPath xpath = XPathFactory.newInstance().newXPath();
	        	String[] xmlnsParts = xmlnsStr.split("=");
	         	xpath.setNamespaceContext(new MyNamespaceContext(xmlnsParts[0], xmlnsParts[1]));
	         	InputSource src = new InputSource(is);
	         	results = (String) xpath.evaluate(xpathStr, src);
	        } catch (Exception e) {
	        	System.out.println("ERROR in evaluating XPointer.  "+e.getMessage());
	        } 
		return results;
	}

	public static String evaluateXPointer(String xpointer, Document doc) {
		InputStream is = null;
		try {
			is = IOUtils.DocumentToInputStream(doc);
		} catch (Exception e) {}

		return evaluateXPointer(xpointer, is);
	}

	/**
	 * Custom namespace class for adding additional namespaces for XPath evaluation
	 *
	 */
	public static class MyNamespaceContext implements NamespaceContext {

		protected String currentPrefix = null;
		protected String currentUri = null;

		public void setCurrentNamespace(String prefix, String uri) {
			this.currentPrefix = prefix;
			this.currentUri = uri;
		}

		public MyNamespaceContext(String prefix, String uri) {
			super();
			this.currentPrefix = prefix;
			this.currentUri = uri;
		}

		public String getNamespaceURI(String prefix) {
		    if (prefix.equals(currentPrefix)) {
		        return currentUri;
		    }
		    else {
		        return XMLConstants.NULL_NS_URI;
		    }
		}

		public String getPrefix(String namespace) {
		    if (namespace.equals(currentUri)) {
		        return currentPrefix;
		    }
		    else {
		        return null;
		    }
		}

		public Iterator getPrefixes(String namespace) {
		    return null;
		}
	}

}