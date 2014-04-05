package server;
import java.io.*;
import java.net.*;

/**
 * Encapsulate IO for the given client {@link Socket}, with a group of
 * other clients in the given {@link ClientPool}.
 */
public class ClientHandler extends Thread {
	private ClientPool _pool;
	private Socket _client;
	private BufferedReader _input;
	private PrintWriter _output;
	private boolean _running;
	
	/**
	 * Constructs a {@link ClientHandler} on the given client with the given pool.
	 * 
	 * @param pool a group of other clients to chat with
	 * @param client the client to handle
	 * @throws IOException if the client socket is invalid
	 * @throws IllegalArgumentException if pool or client is null
	 */
	public ClientHandler(ClientPool pool, Socket client) throws IOException {
		if (pool == null || client == null) {
			throw new IllegalArgumentException("Cannot accept null arguments.");
		}
		
		_pool = pool;
		_client = client;
		_pool.add(this);
		_input = new BufferedReader(new InputStreamReader(_client.getInputStream()));
		_output = new PrintWriter(_client.getOutputStream(), true);
	}
	
	/**
	 * Send and receive data from the client. The first line received will be
	 * interpreted as the client's user-name.
	 */
	public void run() {
		String user;
		_running = true;
		try {
			_output.println("- What's your username?");
			user = _input.readLine();
			_pool.broadcast("-- User " + user + " logged in. --", this);
			while (_running) {
				String msg = _input.readLine();
				if (msg == null || msg.length() == 0 || msg.equalsIgnoreCase("logoff")) {
					break;
				}
				_pool.broadcast(user + ": " + msg, this);
			}
			_pool.broadcast("System: User: " + user + " logged off.", this);
			send("");
			kill();
			
		} catch (IOException e) {
			err("ERROR reading from client");
			e.printStackTrace();
		}
	}
	
	/**
	 * Send a string to the client via the socket
	 * 
	 * @param message text to send
	 */
	public void send(String message) {
		_output.println(message);
		_output.flush();
	}

	
	/**
	 * Close this socket and its related streams.
	 * 
	 * @throws IOException Passed up from socket
	 */
	public void kill() throws IOException {
		out("Killing Client Handler");
		_running = false;
		_input.close();
		_output.close();
		_client.close();
		_pool.remove(this); //remove this from the pool since we are killing it.
	}
	
	
	/**	
	 * Utilities for printing.
	 * @param strs
	 */
		void out(Object...strs) {
			System.out.println(composeString(strs));
		}
		
		void err(Object...strs) {
			System.err.println(composeString(strs));
		}
		
		String composeString(Object...strs) {
			String s = "" + strs[0];
			for (int i = 1; i < strs.length; i++) {
				s += (strs[i] +" ");
			}
			return s;
		}
}