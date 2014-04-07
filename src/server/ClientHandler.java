/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import shared.AutocorrectRequest;
import shared.NeighborsRequest;
import shared.PathRequest;
import shared.Request;
import shared.Response;
import shared.WayRequest;
import backend.Backend;
import backend.Util;

/**
 * A thread for handling client connections to the server.
 * @author emc3
 */
public class ClientHandler extends Thread {
	private Socket _client;
	private ObjectInputStream _input;
	private ObjectOutputStream _output;
	private ClientPool _pool;
	Backend _b;
	
	//stuff for threading requests:
	private PathWayGetter _pwGetter;
	private SuggestionGetter _sugGetter;
	private NeighborGetter _nbrGetter;
	private WayGetter _wayGetter;
	
	//they all share this response queue
	ConcurrentLinkedQueue<Response> _responseQueue;
	
	/**
	 * Default constructor.
	 * Sets up this handler to be ready to receive requests 
	 * from and write responses to the client.
	 * 
	 * @param pool - the shared pool of currently running client handlers on this server.
	 * @param clientSocket - the connection to the client
	 * @param backend - the backend which will compute and return the results of the requests sent from the client.
	 * @throws IOException if the client connection is unable to open an input or output stream.
	 */
	public ClientHandler(ClientPool pool, Socket clientSocket, Backend backend) throws IOException {
		if (pool == null || clientSocket == null)
			throw new IllegalArgumentException("Cannot accept null arguments.");
		
		_client = clientSocket;
		_pool = pool;
		_b = backend;
		
		_input = new ObjectInputStream(_client.getInputStream());
		_output = new ObjectOutputStream(_client.getOutputStream());
		
		_pwGetter = new PathWayGetter(this);
		_sugGetter = new SuggestionGetter(this);
		_nbrGetter = new NeighborGetter(this);
		_wayGetter = new WayGetter(this);
		
		_pool.add(this);
	}
	
	
	/**
	 * Process requests from the client and
	 * respond with the data resulting from the request.
	 */
	public void run() {
		try {
			Request req;
			while ((req = (Request)_input.readObject()) != null) {
				processRequest(req);
				while (!_responseQueue.isEmpty()) {
					_output.writeObject(_responseQueue.poll());
					_output.flush();
				}
			}
			Util.out("Nulled request. Terminating client handler");
		} catch(IOException | ClassNotFoundException e) {
			Util.err("User has exited.");
		} finally {
			kill();
		}
	}
	
	
	/**
	 * Processes the parametric request and query the backend for
	 * some type of data based on the type of the request. 
	 * Then wraps the data received from the backend in a Response
	 * object and returns that object to be written to the client.   
	 * @param req - the request to process
	 * @return
	 * A response containing the data received from the backend.
	 */
	private void processRequest(Request req) {
		switch (req.getType()) {
		case AUTO_CORRECTIONS:
			AutocorrectRequest aReq = (AutocorrectRequest) req;
			_sugGetter.suggestFor(aReq.getInput(), aReq.getBoxNo()); //start a new thread for request box #
			break;
			
		case NEAREST_NEIGHBORS:
			NeighborsRequest nReq = (NeighborsRequest) req;
			_nbrGetter.getNeighbors(nReq.getNumNeighbors(), nReq.getLocation()); //start a new worker thread in getter
			break;
			
		case WAYS:
			WayRequest wReq = (WayRequest) req;
			_wayGetter.getWays(wReq.getMinLat(), wReq.getMaxLat(), wReq.getMinLon(), wReq.getMaxLon()); //start a new worker thread in the getter
			break;
		case PATH:
			PathRequest pReq = (PathRequest) req;
			_pwGetter.findPath(pReq.getSource(), pReq.getDest(), pReq.getTimeout());
			break;
			
		default:
			//not much we can do with an invalid request
			throw new IllegalArgumentException("Unsupported request type");
		}
	}
		
	/**
	 * Kills this handler and cleans up its additional resources.
	 * 
	 * @throws IOException when the client's connection or data streams 
	 * is either already closed or cannot be closed for some reason.
	 */
	public void kill() {
		try {
			_pool.remove(this);
			_input.close();
			_output.close();
			_client.close();			
		} catch (IOException e) {
			Util.err("ERROR killing client handler.\n", e.getMessage());	
		}
	}
}
