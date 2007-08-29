package com.occamlab.te.util;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

/**
 * Provides various utility methods to read/write from files and streams.
 * 
 * @author jparrpearson
 */
public class IOUtils {

	/**
	* Converts an org.w3c.dom.Document element to an java.io.InputStream.
	*
	* @param edoc
	*      the org.w3c.dom.Document to be converted
	* @return InputStream
	*      the InputStream value of the passed doument
	*/
	public static InputStream DocumentToInputStream(Document edoc) throws IOException {

	    // Create the input and output for use in the transformation
	    final org.w3c.dom.Document doc = edoc;
	    final PipedOutputStream pos = new PipedOutputStream();
	    PipedInputStream pis = new PipedInputStream();
	    pis.connect(pos);

	    (new Thread(new Runnable() {

	            public void run() {
	                    // Use the Transformer.transform() method to save the Document to a StreamResult
	                    try {
	                            TransformerFactory tFactory = TransformerFactory.newInstance();
	                            Transformer transformer = tFactory.newTransformer();
	                            transformer.setOutputProperty("encoding", "UTF-8");
	                            transformer.setOutputProperty("indent", "yes");
	                            transformer.transform(new DOMSource(doc), new StreamResult(pos));
	                    }
	                    catch (Exception e) {
	                            throw new RuntimeException("Error converting Document to InputStream.  "+ e.getMessage());
	                    }
	                    finally {
	                            try {
	                                    pos.close();
	                            }
	                            catch (IOException e) {

	                            }
	                    }
	            }
	    }, "IOUtils.DocumentToInputStream(Document edoc)")).start();

	    return pis;
	}

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
			System.out.println("Error converting InputStream to String: "+e.getMessage());
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
			System.out.println("Error converting InputStream to byte[]: "+e.getMessage());
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
	   		System.out.println("Error writing Object to file: "+e.getMessage());
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
	   		System.out.println("Error writing byte[] to file: "+e.getMessage());
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
			System.out.println("Error reading Object from file: "+e.getMessage());
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
			System.out.println("Error reading byte[] from file: "+e.getMessage());
			return null;
		}
		return bytes;
	}

	/**
	 * Writes a HttpResponseToFile object to a file
	 */
	public static boolean writeHttpResponseToFile(HttpResponse resp, File f) {
		try {
			OutputStreamWriter fw = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f)), "UTF-8");
			//FileWriter fw = new FileWriter(f);

			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fw.write("<httpresponse>\n");
			fw.write("<status>\n");
			BasicStatusLine statusLine = (BasicStatusLine)resp.getStatusLine();
			HttpVersion version = statusLine.getHttpVersion();
			int major = version.getMajor();
			int minor = version.getMinor();
			String reasonPhase = statusLine.getReasonPhrase();
			int statusCode = statusLine.getStatusCode();
			fw.write("<version>"+major+","+minor+"</version>\n");
			fw.write("<reason>"+reasonPhase+"</reason>\n");
			fw.write("<code>"+statusCode+"</code>\n");
			fw.write("</status>\n");
			fw.write("<headers>\n");
			Header[] headers = resp.getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				String name = headers[i].getName();
				String value = headers[i].getValue();
				fw.write("<header name=\""+name+"\">"+value+"</header>\n");
			}
			fw.write("</headers>\n");
			fw.write("<content>");
			InputStream is = resp.getEntity().getContent();
			String entityStr = inputStreamToString(is);
			// Strip XML declaration
			if (entityStr.indexOf("<?xml") != -1) {
				int endOfXmlDecl = entityStr.indexOf("?>")+"?>".length();
				entityStr = entityStr.substring(endOfXmlDecl).trim();
			}
			fw.write(entityStr);
			fw.write("</content>\n");
			fw.write("</httpresponse>");

			fw.flush();
			fw.close();
		} catch (Exception e) {
	   		System.out.println("Error writing HttpResponse to file: "+e.getMessage());
	   		return false;
		}
		return true;
	}

	/**
	 * Reads a HttpResponseToFile object from a file
	 */
	public static HttpResponse readHttpResponseFromFile(File f) {
		BasicHttpResponse resp = null;
		try {
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(f);

			NodeList versionNodes = doc.getElementsByTagName("version");
			NodeList reasonNodes = doc.getElementsByTagName("reason");
			NodeList codeNodes = doc.getElementsByTagName("code");
			String[] versionStr = versionNodes.item(0).getTextContent().split(",");
			int major = Integer.parseInt(versionStr[0]);
			int minor = Integer.parseInt(versionStr[1]);
			HttpVersion version = new HttpVersion(major, minor);
			String reasonPhase = reasonNodes.item(0).getTextContent();
			int statusCode = Integer.parseInt(codeNodes.item(0).getTextContent());
			BasicStatusLine statusLine = new BasicStatusLine(version, statusCode, reasonPhase);
			resp = new BasicHttpResponse(statusLine);

			NodeList headers = doc.getElementsByTagName("header");
			for (int i = 0; i < headers.getLength(); i++) {
				String name = ((Element)headers.item(i)).getAttribute("name");
				String value = ((Element)headers.item(i)).getTextContent();
				resp.addHeader(name, value);
			}

			NodeList contentNodes = doc.getElementsByTagName("content");
			String bodyStr = contentNodes.item(0).getTextContent();
			StringEntity entity = new StringEntity(bodyStr);
			resp.setEntity(entity);
			//System.out.println("HttpResponse elements: "+versionStr+"|"+reasonPhase+"|"+statusCode+"|\n\n"+bodyStr+"\n\n|"+headers.getLength()+" headers");
		} catch (Exception e) {
	   		System.out.println("Error reading HttpResponse from file: "+e.getMessage());
	   		return null;
		}
		return resp;
	}

	/**
	 * Polls a file periodically until it 1) exists or 2) the timeout is exceeded (returns null)
	 * Reads the file as a HttpResponse object
	 */
	public static HttpResponse pollHttpResponseFile(File file, int timeout, int interval) throws InterruptedException {
		// Convert time from s to ms for Thread
		int fullTimeout = Math.round(timeout*1000);

		// Split up the timeout to poll every x amount of time
		int timeoutShard = Math.round(fullTimeout/interval);

		// Poll until file exists, return if it exists
		for (int i = 0; i < interval; i++) {
			Thread.sleep(timeoutShard);
			if (file.exists() && file.canRead()) {
				//Thread.sleep(timeoutShard);
				return readHttpResponseFromFile(file);
			}
		}

		// Return null if time is up and still no file
		return null;
	}

	public static HttpResponse pollHttpResponseFile(File file, int timeout) throws InterruptedException {
		return pollHttpResponseFile(file, timeout, 20);
	}

	/**
	 * Polls a file periodically until it 1) exists or 2) the timeout is exceeded (returns null)
	 * Reads the file as a java Object
	 */
	public static Object pollFile(File file, int timeout, int interval) throws InterruptedException {
		// Convert time from s to ms for Thread
		int fullTimeout = Math.round(timeout*1000);

		// Split up the timeout to poll every x amount of time
		int timeoutShard = Math.round(fullTimeout/interval);

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

	public static Object pollFile(File file, int timeout) throws InterruptedException {
		return pollFile(file, timeout, 25);
	}

}