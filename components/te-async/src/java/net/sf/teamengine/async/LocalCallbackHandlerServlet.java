package net.sf.teamengine.async;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.File;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import com.occamlab.te.util.IOUtils;
import com.occamlab.te.TECore;

/**
 * An HTTP servlet that parses the incoming request (response) and sends an HTTP
 * status code 204 response to the client.  The original request (response) is then
 * saved for further processing by the TEAM engine.
 *
 * @author jparrpearson
 */
public class LocalCallbackHandlerServlet extends HttpServlet {
	
	private static final long serialVersionUID = 2144575227068756158L;

	/** Internal class to close down the server when the given timeout elapses with a response recieved */
	private class TimeoutExit extends Thread {

		private int timeout = 10;

		public TimeoutExit(int timeout) {
			super();
			this.timeout = timeout;
		}
		
		public void run() {
	        	// Convert s to ms
			int timeInMs = Math.round(this.timeout * 1000);
	        	try {
				Thread.sleep(timeInMs);
				System.out.println("Timeout encountered, shutting down the servlet...");
				System.exit(0);
			} catch (Exception e) {}
		}
	}

	public void init() throws ServletException {
		super.init();
		// Get timeout from web.xml config
		String timeout = getServletConfig().getInitParameter("timeout");
		if (timeout == null) {
			timeout = "10";
		}
		// Stop the server when the timeout is reached
		Thread thread = new TimeoutExit(Integer.parseInt(timeout));
		thread.start();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// 1) Process the request (response from a previous request)
		try {
			// Only process actual responses, length of 1 or more bytes (-1 is unknown)
			int length = request.getContentLength();
			if (length > 0) {
				// Parse the response as a DOM Document
				InputStream is = request.getInputStream();
				byte[] respBytes = IOUtils.inputStreamToBytes(is);

				// Construct the HttpResponse (HttpBasicResponse) to send to parsers
				// TODO: Get actual status from servlet request, don't assumed HTTP/1.1, 200 OK
				HttpVersion version = new HttpVersion(1,1);
				BasicStatusLine statusLine = new BasicStatusLine(version, 200, "OK");
				BasicHttpResponse resp = new BasicHttpResponse(statusLine);
				// Set headers
				Enumeration headerNames = request.getHeaderNames();
				for(; headerNames.hasMoreElements(); ) {
					String name = (String) headerNames.nextElement();
					String value = request.getHeader(name);
					if (name == null) continue;
					resp.addHeader(name, value);
				}
				// Set XML body
				HttpEntity entity = new ByteArrayEntity(respBytes);
				resp.setEntity(entity);

				// TODO: Save the response somewhere (TECore?)
			}
		} catch (Exception e){
			System.out.println("Error reading XML response: "+e.getMessage());
		}
		
		// 2) Send a simple acknowledgement response, status code 204 (No Content)
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);

		// Temp: for debugging purposes (http://localhost:PORT/LocalCallbackHandlerServlet)
		/*response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("RESPONSE SENT!");
		out.close();*/

		// Stop the server when we get a response
		System.out.println("Response sent, shutting down the servlet...");
		System.exit(0);
	}
}