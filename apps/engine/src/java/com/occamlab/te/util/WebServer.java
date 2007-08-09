package com.occamlab.te.util;

import java.util.Properties;

import java.io.IOException;

import Acme.Serve.Serve;
import Acme.Serve.Serve.PathTreeDictionary;

/**
 * Class to create and run an embedded web server (TJWS).
 *
 * Access the server by pointing to http://localhost:PORT/ or
 * http://localhost:PORT/webapps/examples/HelloWorld (location of servlet)
 *
 * @author jparrpearson
 */
public class WebServer {

	// Override the TJWS Serve to allow for public mappings
	private class PublicServe extends Serve {
	        public void setMappingTable(PathTreeDictionary mappingtable) {
                      super.setMappingTable(mappingtable);
                }
	}

	// The web server instance
	private PublicServe srv = new PublicServe();

	public WebServer(int port, int timeout) {
		System.out.println("Starting server on port "+port+", timeout "+timeout+"s");

		// Set server properties
		Properties properties = new Properties();
		properties.put("port", port);
		properties.setProperty(Serve.ARG_NOHUP, "nohup");
		this.srv.arguments = properties;

		// Setup the default servlet to serve files
                PathTreeDictionary aliases = new PathTreeDictionary();
                aliases.put("/", new java.io.File("webroot"));
		this.srv.setMappingTable(aliases);
		this.srv.addDefaultServlets(null);

		// Add code to execute when the server is shut down
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				stopServer();
			}
		}));

		// TODO: Pass on timeout to servlets via web.xml or another method... <getServlet(String)> and then <init(ServletConfig)>

	}

	/** Add additional servlets to the server */
	public void addServlet(String filePath, String className) {
		this.srv.addServlet(filePath, className);
	}

	/** Start the server */
	public void startServer() {
		this.srv.serve();
	}

	/** Stop the server */
	public void stopServer() {
		try {
			this.srv.notifyStop();
		} catch(IOException ioe) {}
		this.srv.destroyAllServlets();
	}

	public static void main(String[] args) {
		// Get the user arguments (port)
		int port = 80;
		int timeout = 30; // seconds
		if (args.length == 1) {
			port = Integer.parseInt(args[0]);
		}
		if (args.length == 2) {
			port = Integer.parseInt(args[0]);
			timeout = Integer.parseInt(args[1]);
		}

		// Create and start the web server
		WebServer ws = new WebServer(port, timeout);
		ws.addServlet("/webapps/examples/HelloWorld", "examples.HelloWorld");
		ws.startServer();
	}
}
