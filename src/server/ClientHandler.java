/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

import maps.MapFactory;
import maps.Node;
import shared.AutocorrectRequest;
import shared.ClientConnectionResponse;
import shared.NeighborsRequest;
import shared.PathRequest;
import shared.Request;
import shared.Response;
import shared.ServerStatus;
import shared.WayRequest;
import backend.Backend;
import backend.Constants;
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
	private boolean _running = false;
	Backend _b;

	//stuff for threading requests:
	private PathWayGetter _pwGetter;
	private SuggestionGetter _sugGetter;
	private NeighborGetter _nbrGetter;
	private CopyOfWayGetter _wayGetter;

	//they all share this response queue
	ConcurrentLinkedQueue<Response> _responseQueue;
	private PushThread _pushThread;

	/**
	 * Default constructor.
	 * Sets up this handler to be ready to receive requests 
	 * from and write responses to the client.
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
		_responseQueue = new ConcurrentLinkedQueue<>();

		_output = new ObjectOutputStream(_client.getOutputStream());
		_input = new ObjectInputStream(_client.getInputStream());

		//pwGetter and wayGetter are special because they are their own threads
		_pwGetter = new PathWayGetter(this);
		_pwGetter.start();
		_wayGetter = new CopyOfWayGetter(this);
		_wayGetter.start();
		
		_sugGetter = new SuggestionGetter(this);
		_nbrGetter = new NeighborGetter(this);

		_pool.add(this);
	}


	/**
	 * Process requests from the client and
	 * respond with the data resulting from the request.
	 */
	public void run() {
		_running = true;
		_pushThread = new PushThread();
		_pushThread.start();
		try {
			Request req;
			while (_running) {
				req = (Request) _input.readObject();
				processRequest(req);
			}
			Util.out("Running has ceased. Goodbye.");
		} catch(IOException | ClassNotFoundException e) {
			Util.out("-- Client exited. --");
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
		Util.debug("Processing Request...\n", req);
		switch (req.getType()) {
		case AUTO_CORRECTIONS:
			AutocorrectRequest aReq = (AutocorrectRequest) req;
			_sugGetter.suggestFor(aReq.getInput(), aReq.getBoxNo()); //start a new thread for request box #
			break;

		case NEAREST_NEIGHBORS:
			NeighborsRequest nReq = (NeighborsRequest) req;
			_nbrGetter.getNeighbors(nReq.getNumNeighbors(), nReq.getLocation(), nReq.isSource()); //start a new worker thread in getter
			break;

		case WAYS:
			WayRequest wReq = (WayRequest) req;
			_wayGetter.getWays(wReq.getMinLat(), wReq.getMaxLat(), wReq.getMinLon(), wReq.getMaxLon()); //start a new worker thread in the getter
			break;
		case PATH:
			PathRequest pReq = (PathRequest) req;
			//We check if there are intersections to be found
			Node source =  MapFactory.createIntersection(pReq.getCrossStreet(true, 1), pReq.getCrossStreet(true, 2));
			Node dest = MapFactory.createIntersection(pReq.getCrossStreet(false, 1), pReq.getCrossStreet(false, 2));
			if (source == null || source == dest)
				source = pReq.getSource();
			if (dest == null || source == dest)
				dest = pReq.getDest();
			_pwGetter.findPath(source, dest, pReq.getTimeout());
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
			_running = false;
			_pool.remove(this);
			_input.close();
			_output.close();
			_client.close();			
		} catch (IOException e) {
			Util.err("ERROR killing client handler.\n", e.getMessage());	
		}
	}

	private class PushThread extends Thread {

		@Override
		public void run() {
			try {
				if (_b.isDone()) {
					//if backend finished some time in the past, send the client connection response.
					_output.writeObject(new ClientConnectionResponse(_b.getInitialWays(), MapFactory.getTrafficMap(), Constants.MINIMUM_LATITUDE, Constants.MAXIMUM_LATITUDE, Constants.MINIMUM_LONGITUDE, Constants.MAXIMUM_LONGITUDE));
					_output.flush();
				} 
				//if backend is not yet done, it will send the client connection response 
				//when it finishes initializing.
				_output.writeObject(new ServerStatus(_b.isDone()));
				_output.flush();
				
				while (_running) {
					if (!_responseQueue.isEmpty()) {
						_output.writeObject(_responseQueue.poll());
						_output.flush();
					}
				}
			} catch (IOException e) {
				if (!_running)
					Util.out("Connection closed. No more responses will be sent.");
				else
					Util.err("ERROR writing response in push thread");
			}
		}
	}
}
