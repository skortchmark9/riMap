/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import backend.Util;

/**
 * @author emc3
 *
 */
public class ClientHandler extends Thread {
	private Socket _client;
	private ObjectInputStream _input;
	private ObjectOutputStream _output;
	private ClientPool _pool;
	
	/**
	 *  
	 */
	public ClientHandler(ClientPool pool, Socket clientSocket) throws IOException {
		if (pool == null || clientSocket == null)
			throw new IllegalArgumentException("Cannot accept null arguments.");
		
		_client = clientSocket;
		_pool = pool;
		
		_input = new ObjectInputStream(_client.getInputStream());
		_output = new ObjectOutputStream(_client.getOutputStream());
		
		_pool.add(this);
	}
	
	public void run() {
		try {
			//do some shit (pref shit that throws exceptions)
		} catch(IOException | ClassNotFoundException e) {
			Util.err("User has exited.");
			kill();
		}
	}
	
	
	/**
	 * 
	 * @throws IOException
	 */
	public void kill() {
		try {
			_pool.remove(this);
			_input.close();
			_output.close();
			_client.close();			
		} catch (IOException e) {
			System.err.println(e.getMessage());	
		}
	}

}
