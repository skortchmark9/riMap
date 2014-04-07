/**
 * 
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
	Backend _b;
	
	//stuff for threading requests:
	private Executor _requestHandlerPool;
	private PathWayGetter _pwGetter;
	ConcurrentLinkedQueue<Response> _responseQueue;
	private AtomicInteger _autocThreadCount, _neighborThreadCount, _wayGetThreadCount, _pathGetThreadCount;
	
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
		//We need atomic int's for each type of request/response 
		_autocThreadCount = new AtomicInteger(0);
		_neighborThreadCount = new AtomicInteger(0);
		_wayGetThreadCount = new AtomicInteger(0);
		_pathGetThreadCount = new AtomicInteger(0);

		//XXX: I'm making the max size 8 instead of 4
		_requestHandlerPool = new ThreadPoolExecutor(Constants.THREADPOOL_CORE_SIZE, 8, Constants.THREADPOOL_TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
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
			//submit a new suggestion getter to its pool
			_requestHandlerPool.execute(new SuggestionGetter(aReq.getInput(), aReq.getBoxNo()));
			break;
			
		case NEAREST_NEIGHBORS:
			NeighborsRequest nReq = (NeighborsRequest) req;
			//submit a new nearest-neighbors getter to its pool
			_requestHandlerPool.execute(new NeighborGetter(nReq.getNumNeighbors(), nReq.getLocation()));
			break;
			
		case WAYS:
			WayRequest wReq = (WayRequest) req;
			//submit a new way getter to the its pool
			_requestHandlerPool.execute(new WayGetter(wReq.getMinLat(), wReq.getMaxLat(), wReq.getMinLon(), wReq.getMaxLon()));
			break;
			
		case PATH:
			PathRequest pReq = (PathRequest) req;
			_pwGetter.findPath(pReq.getSource(), pReq.getDest(), pReq.getTimeout());
			break;
			
		default:
			//not much we can do with an invalid request
			throw new IllegalArgumentException("Unsupported request type");
			break;
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
	
	
	
	
	private class SuggestionGetter implements Runnable {
		private int _threadID, _boxNum;
		private String _input;
		
		private SuggestionGetter(String input, int boxNum) {
			_input = input;
			_boxNum = boxNum;
		}

		@Override
		public void run() {
			_threadID = _autocThreadCount.incrementAndGet();
			Response resp = new AutocorrectResponse(_b.getAutoCorrections(_input), _boxNum);
			if (_threadID == _autocThreadCount.get()) {
				_responseQueue.add(resp);
			}
		}
		
	}
	
	private class NeighborGetter implements Runnable {
		private int _threadID, _numNeighbors;
		private KDimensionable _location;
		
		private NeighborGetter(int numNeighbors, KDimensionable location) {
			_numNeighbors = numNeighbors;
			_location = location;
		}
		
		@Override
		public void run() {
			_threadID = _neighborThreadCount.incrementAndGet();
			Response resp = new NeighborsResponse(_b.getNearestNeighbors(_numNeighbors, _location));
			if (_threadID == _neighborThreadCount.get()) {
				_responseQueue.add(resp);
			}
		}
		
	}
	
	private class WayGetter implements Runnable {
		private int _threadID;
		private double _minLat, _maxLat, _minLon, _maxLon; 
		
		public WayGetter(double minLat, double maxLat, double minLon, double maxLon) {
			_minLat = minLat;
			_maxLat = maxLat;
			_minLon = minLon;
			_maxLon = maxLon;
		}

		@Override
		public void run() {
			_threadID = _wayGetThreadCount.incrementAndGet();
			Response resp = new WayResponse(_b.getWaysInRange(_minLat, _maxLat, _minLon, _maxLon));
			if (_threadID == _wayGetThreadCount.get()) {
				_responseQueue.add(resp);
			}
		}
		
	}
}
