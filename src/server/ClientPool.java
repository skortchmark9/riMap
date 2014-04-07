/**
 * 
 */
package server;

import java.util.LinkedList;

import shared.Response;
import shared.ServerStatus;

/**
 * @author emc3
 */
public class ClientPool {
	private LinkedList<ClientHandler> _clients;
	
	/**
	 * 
	 */
	public ClientPool() {
		_clients = new LinkedList<ClientHandler>();
	}
	
	
	/**
	 * Add a new client to the chat room.
	 * @param client to add
	 */
	public synchronized void add(ClientHandler client) {
		_clients.add(client);
	}
	
	
	/**
	 * Remove a client from the pool. Only do this if you intend to clean up
	 * that client later.
	 * 
	 * @param client to remove
	 * @return true if the client was removed, false if they were not there.
	 */
	public synchronized boolean remove(ClientHandler client) {
		return _clients.remove(client);
	}
	
	public synchronized void broadcast(Response r) {
		for (ClientHandler client : _clients) {
			client._responseQueue.add(r);
		}
	}

	
	
	
	
	/**
	 * Close all {@link ClientHandler}s and empty the pool
	 */
	public synchronized void killall() {
		for (ClientHandler client : _clients)
			client.kill();
		
		_clients.clear();
	}
}
