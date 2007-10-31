package net.sf.teamengine.async;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;

import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.occamlab.te.util.DomUtils;
import com.occamlab.te.util.IOUtils;

/**
 * Network related utility methods
 */
public class NetUtils {

	/**
	 * Writes a HttpResponseToFile object to a file
	 */
	public static boolean writeHttpResponseToFile(HttpResponse resp, File f) {
		try {
			OutputStreamWriter fw = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(f)), "UTF8");
			//FileWriter fw = new FileWriter(f);

			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
			fw.write("<httpresponse>\r\n");
			fw.write("<status>\r\n");
			BasicStatusLine statusLine = (BasicStatusLine)resp.getStatusLine();
			HttpVersion version = statusLine.getHttpVersion();
			int major = version.getMajor();
			int minor = version.getMinor();
			String reasonPhase = statusLine.getReasonPhrase();
			int statusCode = statusLine.getStatusCode();
			fw.write("<version>"+major+","+minor+"</version>\r\n");
			fw.write("<reason>"+reasonPhase+"</reason>\r\n");
			fw.write("<code>"+statusCode+"</code>\r\n");
			fw.write("</status>\r\n");
			fw.write("<headers>\r\n");
			Header[] headers = resp.getAllHeaders();
			for (int i = 0; i < headers.length; i++) {
				String name = headers[i].getName();
				String value = headers[i].getValue();
				fw.write("<header name=\""+name+"\">"+value+"</header>\r\n");
			}
			fw.write("</headers>\r\n");
			fw.write("<content>");
			InputStream is = resp.getEntity().getContent();
			String entityStr = IOUtils.inputStreamToString(is);
			// Strip XML declaration
			if (entityStr.indexOf("<?xml") != -1) {
				int endOfXmlDecl = entityStr.indexOf("?>")+"?>".length();
				entityStr = entityStr.substring(endOfXmlDecl).trim();
			}
			fw.write(entityStr);
			fw.write("</content>\r\n");
			fw.write("</httpresponse>\r\n");

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
			Node bodyNode = (Node)contentNodes.item(0).getFirstChild();
			String bodyStr = DomUtils.serializeNode(bodyNode);
			StringEntity entity = new StringEntity(bodyStr);
			resp.setEntity(entity);
			//System.out.println("HttpResponse elements: "+major+","+minor+"|"+reasonPhase+"|"+statusCode+"|\n\n"+bodyStr+"\n\n|"+headers.getLength()+" headers");
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
	 * Creates an HttpResponse from a java HttpURLConnection
	 *
	 */
	public static HttpResponse getHttpResponse(HttpURLConnection uc) throws IOException {
		// Get URLConnection values
		InputStream is = uc.getInputStream();
		byte[] respBytes = IOUtils.inputStreamToBytes(is);
		int respCode = uc.getResponseCode();
		String respMess = uc.getResponseMessage();
		Map respHeaders = uc.getHeaderFields();

		// Construct the HttpResponse (BasicHttpResponse)
		HttpVersion version = new HttpVersion(1,1);
		BasicStatusLine statusLine = new BasicStatusLine(version, respCode, respMess);
		BasicHttpResponse resp = new BasicHttpResponse(statusLine);
		Set respHeadersSet = respHeaders.keySet();
		for( Iterator it = respHeadersSet.iterator(); it.hasNext(); ) {
			String name = (String) it.next();
			List valueList = (List) respHeaders.get(name);
			String value = (String) valueList.get(0);
			if (name == null) continue;
			resp.addHeader(name, value);
		}
		HttpEntity entity = new ByteArrayEntity(respBytes);
		resp.setEntity(entity);
		return resp;
	}
}

