package com.occamlab.te.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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
			System.out.println("Error generating MD5: "+e.getMessage());
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
		// Parse the XPointer into usable namespaces and XPath expressions
		int xmlnsStart = xpointer.indexOf("xmlns(")+"xmlns(".length();
		int xmlnsEnd = xpointer.indexOf(")", xmlnsStart);
		int xpathStart = xpointer.indexOf("xpointer(")+"xpointer(".length();
		int xpathEnd = xpointer.indexOf(")", xpathStart);
		String xmlnsStr = xpointer.substring(xmlnsStart, xmlnsEnd);
		String xpathStr = xpointer.substring(xpathStart, xpathEnd);
		//System.out.println("xmlnsStr: "+xmlnsStr+" xpathStr: "+xpathStr);
	        try {
	        	XPath xpath = XPathFactory.newInstance().newXPath();
	        	String[] namespaces = xmlnsStr.split(",");
	        	// Add namespaces to XPath element
	         	MyNamespaceContext context = new MyNamespaceContext();
	        	for (int i = 0; i < namespaces.length; i++) {
	        		String[] xmlnsParts = namespaces[i].split("=");
	         		context.setNamespace(xmlnsParts[0], xmlnsParts[1]);
	         		xpath.setNamespaceContext(context);
	        	}
	         	InputSource src = new InputSource(is);
	         	results = (String) xpath.evaluate(xpathStr, src);
	         	//System.out.println("results: "+results);
	        } catch (Exception e) {
	        	System.out.println("Error in evaluating XPointer.  "+e.getMessage());
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

	public static String evaluateXPointer(String xpointer, byte[] bytes) {
		Document doc = null;
		try {
			ByteArrayInputStream baip = new ByteArrayInputStream(bytes);
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(baip);
		} catch (Exception e) {}

		return evaluateXPointer(xpointer, doc);
	}

	/**
	 * Custom namespace class for adding additional namespaces for XPath evaluation
	 *
	 */
	public static class MyNamespaceContext implements NamespaceContext {
		private Map<String,String> map;

		public MyNamespaceContext() {
			map = new HashMap<String,String>();
		}

		public void setNamespace(String prefix, String namespaceURI) {
			map.put(prefix, namespaceURI);
		}

		public String getNamespaceURI(String prefix) {
			return (String) map.get(prefix);
		}

		public String getPrefix(String namespaceURI) {
			Set keys = map.keySet();
			for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
				String prefix = (String) iterator.next();
				String uri = (String) map.get(prefix);
				if (uri.equals(namespaceURI)) return prefix;
			}
			return null;
		}

		public Iterator getPrefixes(String namespaceURI) {
			List<String> prefixes = new ArrayList<String>();
			Set keys = map.keySet();
			for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
				String prefix = (String) iterator.next();
				String uri = (String) map.get(prefix);
				if (uri.equals(namespaceURI)) prefixes.add(prefix);
			}
			return prefixes.iterator();
		}
	}

}
