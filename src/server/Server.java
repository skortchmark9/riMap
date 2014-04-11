/**
 * 
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import maps.Way;
import shared.ClientConnectionResponse;
import shared.ServerStatus;
import shared.TrafficResponse;
import backend.Backend;
import backend.Util;

/**
 * This is the server - it handles all the client requests in clienthandler threads
 */
public class Server extends Thread {

	private int _port;
	private ServerSocket _socket;
	private ClientPool _clientPool;
	private boolean _running;
	Backend _backend;

	/**
	 * This is the main constructor for the server
	 * @param serverPort - the port to connect to
	 * @param b - the backend (may or may not be initialized yet
	 * @throws IOException - if we can't connect to to the port
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
				Util.debug("Waiting for new client connection...");
				Socket clientConn = _socket.accept(); //blocks waiting for connections
				
				new ClientHandler(_clientPool, clientConn, _backend).start();
				Util.out("-- New client connected --");
			}
		} catch (IOException e) {
			//handle
		}
	}

	
//////////////These methods broadcast various messages to all the clients in the pool.\\\\\\\\\\\\\\\\\\\
	
	/**
	 * Normal server status messages - ie running
	 * @param s
	 */
	public void serverOKMessage(String s) {
		_clientPool.broadcast(new ServerStatus(true, s));
	}
	
	public void broadcastClientConnection(List<Way> ways, Map<String, Double> trafficMap, double minLat, double maxLat, double minLon, double maxLon) {
		_clientPool.broadcast(new ClientConnectionResponse(ways, trafficMap, minLat, maxLat, minLon, maxLon));
	}
	
	/**
	 * Indicate to the clients that the server has gone down.
	 * @param s - the particular message to be sent.
	 */
	public void serverDownMessage(String s) {
		_clientPool.broadcast(new ServerStatus(false, s));
	}
	
	/**
	 * Sends traffic updates to the clients - sometimes it will indicate if the
	 * traffic bot has gone down (with the status boolean) . In most other cases,
	 * it returns a street name and a traffic value
	 * @param name - the name of the street
	 * @param val - the traffic value to be assigned
	 * @param status - whether or not there is a connection to the server
	 */
	public void trafficUpdate(String name, Double val, boolean status) {
		_clientPool.broadcast(new TrafficResponse(name, val, status));
	}


	/**
	 * Stop waiting for connections, close all connected clients, and close
	 * this server's {@link ServerSocket}.
	 * 
	 * @throws IOException if any socket is invalid.
	 */
	public void kill() throws IOException {
		Util.debug("Killing Server");
		
		_running = false;
		_clientPool.killall();
		
		if (_socket != null)
			_socket.close();
	}

}
