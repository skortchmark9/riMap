package client;
import java.awt.Dimension;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kdtree.KDimensionable;
import maps.MapFactory;
import maps.Node;
import shared.AutocorrectRequest;
import shared.AutocorrectResponse;
import shared.NeighborsRequest;
import shared.NeighborsResponse;
import shared.PathRequest;
import shared.PathResponse;
import shared.Request;
import shared.Request.RequestType;
import shared.Response;
import shared.ServerStatus;
import shared.TrafficResponse;
import shared.WayRequest;
import shared.WayResponse;
import backend.Util;
import frontend.Frontend;

/**
 * A Client Class that sends and receives messages from and to the server.
 */
public class Client {

	private Socket _socket;
	private volatile boolean _running, _serverReady;
	private int _port;
	private ObjectOutputStream _output;
	private ObjectInputStream _input;
	private ReceiveThread _thread;
	private String _IP;
	private Queue<Request> _requests;
	ExecutorService _executor;
	Frontend _frontend;

	/**
	 * Constructs a Client with the given port.
	 * @param port the port number the client will connect to
	 */
	public Client(String IPAddress, int port) {
		_executor = Executors.newFixedThreadPool(5);
		_port = port;
		_IP = IPAddress;
	}

	/**
	 * Starts the Client, so it connects to the sever.
	 * It will set up all the necessary requirements, 
	 * and then launch the GUI.
	 */
	public void start() {
		_frontend = new Frontend(this);
		new Thread(_frontend).start();

		try {
			//host is localhost or IP if an IP address is specified
			InetAddress host = _IP.equals("localhost") ? InetAddress.getLocalHost() : InetAddress.getByName(_IP);
			_socket = new Socket();
			
			//connect socket with a 1-second timeout
			_socket.connect(new InetSocketAddress(host, _port), 1000);
			
			_output = new ObjectOutputStream(_socket.getOutputStream());
			_input = new ObjectInputStream(_socket.getInputStream());
			_running = true;
			
			_requests = new LinkedList<>();
			
			_thread = new ReceiveThread();
			_thread.start();

			Util.debug("creating new frontend");


			Util.debug("Frontend done\n","Attempting to connect to server now");
			_frontend.setVisible(true);
			run();
		}
		catch (IOException ex) {
			err("ERROR: Can't connect to server");
		}
	}

	public void run() {
		while (_running && !_socket.isClosed()) {
			if (!_requests.isEmpty() && _serverReady) {
				try {
					Util.debug("Sending Request");
					_output.writeObject(_requests.poll());
					_output.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Shuts down the client closing all the connections.
	 */
	public void kill() {
		out("Attempting to kill client.");
		_running = false;
		try {
			_input.close();
			_output.close();
			_socket.close();
			_thread.join();
		} catch (IOException | InterruptedException e) {
			if (e instanceof IOException) err("ERROR closing streams and/or socket");
			else err("ERROR joining receive thread");
			e.printStackTrace();
		}
	}

	/**
	 * A method that sends a message to the server.
	 * @param message that will be sent to the server for broadcasting.
	 * @throws IOException 
	 */
	public void request(Request r) {
		if (r.getType() == RequestType.EXIT) {
			this.kill();
		} else {
			_requests.add(r);
		}
	}

	public void requestAutocorrections(String input, int boxNo) {
		Util.debug("Requesting corrections");
		request(new AutocorrectRequest(input, boxNo));
	}

	public void requestNearestNeighbors(int i, KDimensionable kd, boolean isSource) {
		request(new NeighborsRequest(i, kd, isSource));
	}

	public void requestWaysInRange(double minLat, double maxLat, double minLon, double maxLon) {
		Util.debug("Requesting Ways");
		request(new WayRequest(minLat, maxLat, minLon, maxLon));
	}

	public void requestPath(Node start, Node end, int timeout) {
		request(new PathRequest(start, end, timeout));
	}
	/*	
	public void removeAllResponses(ResponseType type) {
		Iterator<Response>  itr = _responses.iterator();
		while(itr.hasNext()) {
			if (itr.next().getType() == type)
				itr.remove();
		}
	}*/

	public boolean serverReady() {
		return _serverReady;
	}

	/**
	 * Process a response from the queue. Figures out which kind of response it
	 * is and then acts accordingly.
	 * @param resp - the response to be processed
	 */
	public void processResponse(Response resp) {
		switch (resp.getType()) {
		case AUTO_CORRECTIONS:
			//If the response is an auto correction, we'll send the list of
			//suggestions to the appropriate text box.
			AutocorrectResponse autocResp = (AutocorrectResponse) resp;
			Util.debug(autocResp.toString());
			_frontend.getBox(autocResp.getBoxNo()).setSuggestions(autocResp.getAutocorrections());
			break;
		case NEAREST_NEIGHBORS:
			//If the response is a neighbors list, then we'll send the list
			NeighborsResponse nbrResp = (NeighborsResponse) resp;
			Util.debug(nbrResp.toString());
			_frontend.updateNeighbor(nbrResp.getNeighbor(), nbrResp.isSource(), nbrResp.getStreet1(), nbrResp.getStreet2());
			break;
		case PATH:
			//If the response is a path of ways, we'll send it to the mapPane.
			PathResponse pathResp = (PathResponse) resp;
			Util.debug(pathResp.toString());
			if (pathResp.getPath().isEmpty()) {
				_frontend.guiMessage(pathResp.getMsg());
			} else {
				_frontend.giveDirections(pathResp.getPath());
				_executor.submit(new CacheThread(pathResp.getPath()));
			}
			break;
		case SERVER_STATUS:
			//If the response is a server status, we'll print the message to the
			//console or the loading screen, whichever is currently being used
			//to display messages to the user.
			ServerStatus statResp = (ServerStatus) resp;
			Util.debug(statResp.toString());
			_serverReady = statResp.isServerUp();
			if (_frontend != null)
				_frontend.guiMessage(statResp.getMsg());
			break;
		case WAYS:
			//tell the front end to tell the map to render the new ways
			Util.debug("Got ways");
			WayResponse wayResp = (WayResponse) resp;
			_frontend.setWays(wayResp.getWays());
			_executor.submit(new CacheThread(wayResp.getWays()));
			break;
		case TRAFFIC:
			TrafficResponse tResp = (TrafficResponse) resp;
			MapFactory.putTrafficValue(tResp.getName(), tResp.getVal());//put traffic value i server side map
			_frontend.refreshMap(); //repaint map pane
			break;
		default:
			//We should never get here.
			throw new IllegalArgumentException("Unsupported Response type");
		}
	}
	
	public Dimension getFrameSize() {
		return _frontend.getSize();
	}
	
	

	/**
	 * A thread that will receive the messages sent by the server to
	 * display to the user.
	 */
	class  ReceiveThread extends Thread {
		public void run() {
			while(_running) {
				try {
					Response received = (Response) _input.readObject();
					Util.debug("Response received");
					processResponse(received);
				} catch (IOException e) {
					if (_running == false || e instanceof EOFException) {
						err("Error message:", e.getMessage());
						out("Input Stream Closed");
						return;
					}
					err("ERROR reading line from socket or write to STD_OUT");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
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
