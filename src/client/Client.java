package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kdtree.KDimensionable;
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
import shared.WayRequest;
import shared.WayResponse;
import backend.Util;
import frontend.Frontend;

/**
 * A Client Class that sends and receives messages from and to the server.
 */
public class Client {

	private Socket _socket;
	private volatile boolean _running;
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
		try {
			_frontend = new Frontend(this);
			_socket = new Socket((_IP.equals("localhost")) ? InetAddress.getLocalHost(): InetAddress.getByName(_IP), _port);
			_input = new ObjectInputStream(_socket.getInputStream());
			_output = new ObjectOutputStream(_socket.getOutputStream());
			_running = true;
			_thread = new ReceiveThread();
			_thread.start();
			_requests = new LinkedList<>();
			run();
		}
		catch (IOException ex) {
			err("ERROR: Can't connect to server");
		}
	}

	public void run() {
		while (_running && !_socket.isClosed()) {
			if (!_requests.isEmpty()) {
				try {
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
		request(new AutocorrectRequest(input, boxNo));
	}

	public void requestNearestNeighbors(int i, KDimensionable kd) {
		request(new NeighborsRequest(i, kd));
	}

	public void requestWaysInRange(double minLat, double maxLat, double minLon, double maxLon) {
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

	public boolean isReady() {
		return _running;
	}

	/**
	 * Process a response from the queue. Figures out which kind of response it
	 * is and then acts accordingly.
	 * @param r - the response to be processed
	 */
	public void processResponse(Response r) {
		switch (r.getType()) {
		case AUTO_CORRECTIONS:
			//If the response is an autocorrection, we'll send the list of
			//suggestions to the appropriate text box.
			AutocorrectResponse acR = (AutocorrectResponse) r;
			_frontend.getBox(acR.getBoxNo()).setSuggestions(
					acR.getAutocorrections());
			break;
		case NEAREST_NEIGHBORS:
			//If the response is a neighbors list, then we'll send the list
			//XXX: not yet implemented.
			NeighborsResponse nR = (NeighborsResponse) r;
			//			mapPane.send(nR.getNeighbors());
			break;
		case PATH:
			//If the response is a path of ways, we'll send it to the mapPane.
			PathResponse pR = (PathResponse) r;
			//			mapPane.setRenderedWays(pR.getPath());
			break;
		case SERVER_STATUS:
			//If the response is a server status, we'll print the message to the
			//console or the loading screen, whichever is currently being used
			//to display messages to the user.
			ServerStatus sR = (ServerStatus) r;
			_frontend.guiMessage(sR.getMsg());
			break;
		case WAYS:
			//If the response is ways, we'll set the mapPane to display them
			WayResponse wR = (WayResponse) r;
			//			mapPane.setWays(wR.getWays());
			break;
		default:
			//We should never get here.
			Util.err("WHOA, we shouldn't be here");
			break;
		}
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
					processResponse(received);
				} catch (IOException e) {
					if (_running == false) {
						err("Error message:", e.getMessage());
						out("Prob just closed the stream via client's kill()");
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
