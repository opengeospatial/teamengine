package com.occamlab.te.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
class TcpListener implements Runnable {

	public final static int DEFAULT_PORT = 7777;

	protected int port;

	protected ServerSocket serverSocket;

	public TcpListener() {
		this(DEFAULT_PORT);
	}

	/**
	 * Constructs a TCP listener on the requested port. Uses the default port
	 * number if it is not in the range 1-65535.
	 * 
	 * @param requestedPort
	 *            requested TCP port number
	 */
	public TcpListener(int requestedPort) {
		if (requestedPort < 1 || requestedPort > 65535) {
			this.port = DEFAULT_PORT;
		} else {
			this.port = requestedPort;
		}
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException iox) {
			System.err
					.println("Could not start listener on port: " + this.port);
			iox.printStackTrace();
		}
		System.out.println("Started TCP listener on port " + this.port);
	}

	public void run() {
        // wait until a client connects
		while (true) {
			try {
				Socket clientSocket = this.serverSocket.accept();
				System.out.println("Accepted connection from "
						+ clientSocket.getInetAddress() + ":"
						+ clientSocket.getPort());
				BufferedReader in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				String inputLine = null;
				while ((inputLine = in.readLine()) != null) {
					System.out.println(inputLine);
				}
				in.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		TcpListener tcpListener = new TcpListener();
		tcpListener.run();
	}
}