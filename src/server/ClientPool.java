package server;
import java.io.IOException;
import java.util.*;
/**
 * A group of {@link ClientHandler}s representing a "chat room".
 */
public class ClientPool {
	private LinkedList<ClientHandler> _clients;
	
	/**
	 * Initialize a new {@link ClientPool}.
	 */
	public ClientPool() {
		_clients = new LinkedList<ClientHandler>();
	}
	
	/**
	 * Add a new client to the chat room.
	 * 
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
	
	/**
	 * Send a message to clients in the pool, but the sender.
	 * 
	 * @param message to send
	 * @param sender the client _not_ to send the message to (send to everyone
	 *          if null)
	 */
	public synchronized void broadcast(String message, ClientHandler sender) {
		for (ClientHandler client : _clients) {
			if (sender != null && sender == client) {
				continue;
			}

			client.send(message);
		}
	}
	
	/**
	 * Close all {@link ClientHandler}s and empty the pool
	 */
	public synchronized void killall() {
		this.broadcast("The server is quitting now. Goodbye.", null);

		for (ClientHandler client : _clients) {
			try {
				client.kill();
			} catch (IOException e) {
				// There's nothing we can do here.
			}
		}

		_clients.clear();
	}
}

