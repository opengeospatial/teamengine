package net.sf.teamengine.async;

import java.io.File;
import java.io.InputStream;

import java.net.URLConnection;
import java.net.HttpURLConnection;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.occamlab.te.TECore;
import com.occamlab.te.util.Utils;
import com.occamlab.te.util.IOUtils;

/**
* Manager class for the various asynchronous methods available.
*
*/
public class AsyncManager {

	public static Document executeAsyncRequest(Node xml) throws Throwable {
		HttpResponse[] respArray = build_async_request(xml);
		return parse_async(respArray);
	}

	// Build and send off the request, get the acknowledgement and final response (web context only at this time)
	public static HttpResponse[] build_async_request(Node xml) {
		// Retrieve the initial acknowledgement
		HttpResponse ackResp = null;
		try {
			URLConnection uc = TECore.build_request(xml);
			ackResp = NetUtils.getHttpResponse((HttpURLConnection)uc);
		} catch (Exception e){
			System.err.println("ERROR: Could not retrieve acknowledgement. "+e.getMessage());
		}

		// Get the port (ignored at this time), timeout, and xpointer-id values
		NamedNodeMap nnm = xml.getAttributes();
		Attr portAttr = ((Attr) nnm.getNamedItem("port"));
		int port = 80;
		if (portAttr != null) {
			port = Integer.parseInt(portAttr.getValue());
		}
		Attr timeoutAttr = ((Attr) nnm.getNamedItem("timeout"));
		int timeout = 10;
		if (timeoutAttr != null) {
			timeout = Integer.parseInt(timeoutAttr.getValue());
		}
		Attr xpointerIdAttr = ((Attr) nnm.getNamedItem("xpointer-id"));
		String xpointerId = "//@requestId";
		if (xpointerIdAttr != null) {
			xpointerId = xpointerIdAttr.getValue();
		}

		// Retrieve the requestId from the acknowledgement
		String reqId = "";
		try {
			InputStream is = ackResp.getEntity().getContent();
			byte[] copyBytes = IOUtils.inputStreamToBytes(is);
			HttpEntity entity = new ByteArrayEntity(copyBytes);
			ackResp.setEntity(entity);
			reqId = Utils.evaluateXPointer(xpointerId, copyBytes);
		} catch (Exception e){
			System.err.println("ERROR: Could not determining request id. "+e.getMessage());
		}
		if (reqId.equals("")) {
			System.err.println("ERROR: No request id was found in the acknowledgement.");
			return new HttpResponse[] {ackResp, null};
		}

		// Check for the response until it is available, then retrieve it
		String hash = Utils.generateMD5(reqId);
		String path = System.getProperty("java.io.tmpdir") + "/async/" + hash;
		File file = new File(path, "HttpResponse.dat");
		BasicHttpResponse resp = null;
		try {
			resp = (BasicHttpResponse) NetUtils.pollHttpResponseFile(file, timeout);
		} catch (Exception e) {
			System.err.println("ERROR: Could not poll and retrieve file. "+e.getMessage());
		}

		return new HttpResponse[] {ackResp, resp};
	}

	// Parse both the acknowledgement and response with the same parser
	public static Document parse_async(HttpResponse[] respArray, String response_id)
	  throws Throwable {
		System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
		  "org.apache.xerces.parsers.XIncludeParserConfiguration");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setFeature(
		  "http://apache.org/xml/features/xinclude/fixup-base-uris",
		false);
		DocumentBuilder db = dbf.newDocumentBuilder();

		Transformer t = TransformerFactory.newInstance().newTransformer();
		Document response_doc = db.newDocument();
		Element response_e = response_doc.createElement("responses");
		if (response_id != null) {
			response_e.setAttribute("id", response_id);
		}

		BasicHttpResponse respAck = (BasicHttpResponse) respArray[0];
		BasicHttpResponse resp = null;
		if (respArray.length > 1) {
			resp = (BasicHttpResponse) respArray[1];
		}

		Element content_eAck = response_doc.createElement("content");
		Element content_eResp = response_doc.createElement("content");
		try {
			t.transform(new StreamSource(respAck.getEntity().getContent()),
			new DOMResult(content_eAck));
			if (resp != null) {
				t.transform(new StreamSource(resp.getEntity().getContent()),
				new DOMResult(content_eResp));
			}
		} catch (Exception e) {
			System.err.println("ERROR: Could not parse responses. "+e.getMessage());
		}

		response_e.appendChild(content_eAck);
		response_e.appendChild(content_eResp);
		response_doc.appendChild(response_e);
		return response_doc;
	}

	public static Document parse_async(HttpResponse[] respArray) throws Throwable {
		return parse_async(respArray, null);
	}

}
