package com.occamlab.te.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.Socket;
import java.net.ServerSocket;

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
    public final static int DEFAULT_TIMEOUT = 10000;

    protected int port;
    protected int timeout;

    protected ServerSocket serverSocket;
    protected InputStream inputStream;

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
            System.err
                    .println("Could not start listener on port: " + this.port);
            iox.printStackTrace();
        }
        System.out.println("Started TCP listener on port " + this.port
        	+ ", with timeout of "+ this.timeout + "s");
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

                // Print stream to STDOUT
                /*BufferedReader in = new BufferedReader(new InputStreamReader(
                        clientSocket.getInputStream()));
                String inputLine = null;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                in.close();*/

                // Get InputStream to the socket data
                this.inputStream = clientSocket.getInputStream();

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public InputStream getInputStream() {
    	return this.inputStream;
    }

    public static void main(String[] args) {
        TcpListener tcpListener = new TcpListener();
        tcpListener.run();
    }
}