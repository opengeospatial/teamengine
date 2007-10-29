package net.sf.teamengine.async;

import java.util.Map;
import java.util.LinkedHashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.Socket;
import java.net.ServerSocket;

import com.occamlab.te.util.IOUtils;
import com.occamlab.te.TECore;

/**
 * Establishes a TCP listener for handling asynchronous callbacks. It is not
 * multi-threaded, and incoming connection requests are queued and handled
 * sequentially (FIFO).
 *
 * @author rmartell
 * @author jparrpearson
 */
public class TcpListener implements Runnable {

    public final static int DEFAULT_PORT = 7777;
    public final static int DEFAULT_TIMEOUT = 100;

    protected int port;
    protected int timeout;

    protected ServerSocket serverSocket;
    protected byte[] bytes;
    protected String status;
    protected Map<String,String> headers;

    public TcpListener() {
        this(DEFAULT_PORT, DEFAULT_TIMEOUT);
    }

    /**
     * Constructs a TCP listener on the requested port. Uses the default port
     * number (7777) if it is not in the range 1-65535, and the default timeout
     * if the timeout is not greater than 0s.
     *
     * @param requestedPort
     *            requested TCP port number
     * @param requestedTimeout
     *            requested timeout in seconds to wait for a response
     */
    public TcpListener(int requestedPort, int requestedTimeout) {
    	headers = new LinkedHashMap<String,String>();
        if (requestedPort < 1 || requestedPort > 65535) {
            this.port = DEFAULT_PORT;
        } else {
            this.port = requestedPort;
        }
        if ((requestedTimeout*1000) < 1) {
            this.timeout = DEFAULT_TIMEOUT;
        } else {
            this.timeout = (requestedTimeout*1000);
        }
        try {
            this.serverSocket = new ServerSocket(this.port);
            this.serverSocket.setSoTimeout(this.timeout);	// Set timeout in ms
        } catch (IOException iox) {
            System.err.println("Could not start listener on port: " + this.port);
            iox.printStackTrace();
        }
        System.out.println("Started TCP listener on port " + this.port
        	+ ", with timeout of "+ Math.round(this.timeout/1000) + "s");
    }

    /**
     * Starts the callback listener.
     *
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // wait until a client connects
        while (true) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Accepted connection from "
                        + clientSocket.getInetAddress() + ":"
                        + clientSocket.getPort());

                // Read in the data
                InputStream is = clientSocket.getInputStream();
		byte[] inputByte = IOUtils.inputStreamToBytes(is);
		String inputStr = new String(inputByte, "UTF-8");

            	// Split on the \r\n\r\n sequence between headers and body
            	String[] message = inputStr.split("\r\n\r\n");

		// Get headers
		String[] headerStr = message[0].split("\n");
		for (int i = 0; i < headerStr.length; i++) {
			// HTTP status line
			if (i == 0) {
			    this.status = headerStr[i];
			    continue;
			}
			int firstSpace = headerStr[i].indexOf(" ");
			String key = headerStr[i].substring(0,firstSpace-1);
   			String value = headerStr[i].substring(firstSpace+1);
   			//System.out.println("Header: "+key+": "+value);
   			this.headers.put(key, value);
		}
		// Save the body
		this.bytes = message[1].getBytes("UTF-8");

                clientSocket.close();
            } catch (IOException e) {
                System.out.println("ERROR: "+e.getMessage());
                this.bytes = null;
                this.headers = null;
                return;
            }
        }
    }

    public static void printStream(InputStream in) {
    	try {
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        String inputLine = null;
	        while ((inputLine = br.readLine()) != null) {
	            System.out.println(inputLine);
	        }
	} catch (Exception e) {
		System.out.println("ERROR: "+e.getMessage());
	}
}

    public String getStatus() {
    	return this.status;
    }

    public byte[] getBytes() {
    	return this.bytes;
    }

    public Map getHeaders() {
    	return this.headers;
    }

    public static void main(String[] args) {
        TcpListener tcpListener = new TcpListener();
        tcpListener.run();
    }
}