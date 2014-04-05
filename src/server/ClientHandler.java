/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.AutocorrectRequest;
import shared.AutocorrectResponse;
import shared.ExitRequest;
import shared.NeighborsRequest;
import shared.NeighborsResponse;
import shared.PathRequest;
import shared.PathResponse;
import shared.Request;
import shared.Response;
import shared.WayRequest;
import shared.WayResponse;
import backend.Backend;
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
	private Backend _b;
	
	/**
	 *  
	 */
	public ClientHandler(ClientPool pool, Socket clientSocket, Backend backend) throws IOException {
		if (pool == null || clientSocket == null)
			throw new IllegalArgumentException("Cannot accept null arguments.");
		
		_client = clientSocket;
		_pool = pool;
		_b = backend;
		
		_input = new ObjectInputStream(_client.getInputStream());
		_output = new ObjectOutputStream(_client.getOutputStream());
		
		_pool.add(this);
	}
	
	public void run() {
		try {
			Request req;
			while ((req = (Request)_input.readObject()) != null) {
				processRequest(req);
			}
			//do some shit (pref shit that throws exceptions)
		} catch(IOException | ClassNotFoundException e) {
			Util.err("User has exited.");
			kill();
		}
	}
	
	
	private Response processRequest(Request req) {
		Response resp;
		switch (req.getType()) {
		case AUTO_CORRECTIONS:
			AutocorrectRequest acReq = (AutocorrectRequest) req;
			return new AutocorrectResponse(_b.getAutoCorrections(acReq.input));
		
		case NEAREST_NEIGHBORS:
			NeighborsRequest nReq = (NeighborsRequest) req;
			return new NeighborsResponse(); //TODO: first fill out neighbors response constructor, then enter proper args here.
		
		case WAYS:
			WayRequest wReq = (WayRequest) req;
			return new WayResponse(); //TODO: implement constructors
			
		case PATH:
			PathRequest pReq = (PathRequest) req;
			return new PathResponse(); //TODO: implement constructor
		default:
			return null;
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
