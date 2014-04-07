/**
 * 
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import backend.Backend;
import backend.Constants;
import backend.Util;

/**
 * @author emc3
 *
 */
public class Server extends Thread {
	
	private int _port;
	private ServerSocket _socket;
	private ClientPool _clientPool;
	private boolean _running;
	Backend _backend;
	
	
	/**
	 * 
	 */
	public Server(int serverPort, Backend b) throws IOException {
		if (serverPort <= 1024)
			throw new IllegalArgumentException("Ports 1024 and under are reserved.");
		_port = serverPort;
		_backend = b;
		_clientPool = new ClientPool();
		_socket = new ServerSocket(_port);
		_running = true;
	}
	
	public void run() {
		try {
		while(_running) {
			if (Constants.DEBUG_MODE)
				Util.out("Waiting for new client connection...");
			
			Socket clientConn = _socket.accept(); //blocks waiting for connections
			
			if (Constants.DEBUG_MODE)
				Util.out("-- NEW CONNECTION ACCEPTED --");
			
			new ClientHandler(_clientPool, clientConn, _backend);
			Util.out("--New client connection!");
		}
		} catch (IOException e) {
			//handle
		}
	}
	
	public void serverMessage(String s) {
		_clientPool.broadcast(s);
	}
	
	
	/**
	 * Stop waiting for connections, close all connected clients, and close
	 * this server's {@link ServerSocket}.
	 * 
	 * @throws IOException if any socket is invalid.
	 */
	public void kill() throws IOException {
		_running = false;
		_clientPool.killall();
		_socket.close();
	}

}
