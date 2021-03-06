package client;
import java.awt.Dimension;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.UIManager;

import kdtree.KDimensionable;
import maps.MapFactory;
import maps.Node;
import maps.Way;
import shared.AutocorrectRequest;
import shared.AutocorrectResponse;
import shared.ClientConnectionResponse;
import shared.NeighborsRequest;
import shared.NeighborsResponse;
import shared.PathRequest;
import shared.PathResponse;
import shared.Request;
import shared.Response;
import shared.ServerStatus;
import shared.TrafficResponse;
import shared.WayRequest;
import shared.WayResponse;
import backend.Constants;
import backend.Util;
import frontend.Frontend;
import frontend.LocalRenderThread;
import frontend.MapPane;

/**
 * A Client Class that sends and receives messages from and to the server.
 */
public class Client {
	

	private Socket _socket;
	private volatile boolean _running, hasServer;
	private int _port;
	private ObjectOutputStream _output;
	private ObjectInputStream _input;
	private ReceiveThread _thread;
	private String _hostName;
	private Queue<Request> _requests;
	ExecutorService _wayCacher, _localPainter;
	private boolean _trafficConnected;
	Frontend _frontend;
	
	private double _minLat, _maxLat, _minLon, _maxLon; //we use these to check the recent-ness of the waysinrange response
	private List<Way> _tempWays;
	
	
	/**
	 * Constructs a Client with the given port.
	 * @param port the port number the client will connect to
	 */
	public Client(String hostName, int port) {
		_wayCacher = Executors.newSingleThreadExecutor();
		_localPainter = Executors.newCachedThreadPool();
		_port = port;
		_hostName = hostName;
		_running = false;
		_trafficConnected = false;
	}

	/**
	 * Starts the Client, so it connects to the sever.
	 * It will set up all the necessary requirements, 
	 * and then launch the GUI.
	 */
	public void start() {
		UIManager.put("swing.boldMetal", Boolean.FALSE);
		_frontend = new Frontend(this);
		new Thread(_frontend).start();
		
		int num_attempts = 0;
		while(!_running) {
			
			if (num_attempts > 7) {
				Util.err("ERROR: Server unavailable. Max number of reconnection attempts reached.");
				return;
			}
			num_attempts++;
			
			try {
				//host is localhost or IP if an IP address is specified
				_socket = new Socket(_hostName, _port);
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
				Util.out("ERROR: Can't connect to server");
				_frontend.guiMessage("Server unavailable!");
				hasServer = false;
				try {
					Thread.sleep(2500);
					_frontend.guiMessage("Attempting to reconnect...");
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					Util.err("ERROR trying to reconnect to server");
					kill();
					return;
				}
			}
		}
	}

	public void run() {
		while (_running && !_socket.isClosed()) {
			if (!_requests.isEmpty() && hasServer) {
				try {
					Util.debug("Sending Request");
					_output.writeObject(_requests.poll());
					_output.flush();
				} catch (IOException e) {
					Util.out("Server closed");
					kill();
				}
			}
		}
	}

	/**
	 * Shuts down the client closing all the connections.
	 */
	public void kill() {
		Util.out("Attempting to kill client.");
		_running = false;
		try {
			_input.close();
			_output.close();
			_socket.close();
			_thread.join();
		} catch (IOException | InterruptedException e) {
			if (e instanceof IOException) Util.err("ERROR closing streams and/or socket");
			else Util.err("ERROR joining receive thread");
			//e.printStackTrace();
		}
	}

	/**
	 * A method that sends a message to the server.
	 * @param message that will be sent to the server for broadcasting.
	 * @throws IOException 
	 */
	public void request(Request r) {
			_requests.add(r);
	}

	public void requestAutocorrections(String input, int boxNo) {
		Util.debug("Requesting corrections");
		request(new AutocorrectRequest(input, boxNo));
	}

	public void requestNearestNeighbors(int i, KDimensionable kd, boolean isSource) {
		request(new NeighborsRequest(i, kd, isSource));
	}

	@SuppressWarnings("static-access")
	public void requestWaysInRange(MapPane map, double minLat, double maxLat, double minLon, double maxLon) {
		Util.debug("Requesting Ways");
		_minLat = minLat; _maxLat = maxLat; _minLon = minLon; _maxLon = maxLon;
		if (hasServer)
			request(new WayRequest(minLat, maxLat, minLon, maxLon, map.scale));
		else
			_localPainter.submit(new LocalRenderThread(map, minLat, maxLat, minLon, maxLon));
	}

	public void requestPath(Node start, Node end, int timeout) {
		request(new PathRequest(start, end, timeout));
	}

	public void requestPath(Node start, Node end, int timeout, String xs1S, String xs2S, String xs1E, String xs2E) {
		request(new PathRequest(start, end, timeout, xs1S, xs2S, xs1E, xs2E));
	}

	public boolean serverReady() {
		return hasServer;
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
				_frontend.giveDirections(pathResp.getPath(), pathResp.getStart(), pathResp.getEnd());
				_wayCacher.submit(new CacheThread(pathResp.getPath(), true));
			}
			break;
		case SERVER_STATUS:
			//If the response is a server status, we'll print the message to the
			//console or the loading screen, whichever is currently being used
			//to display messages to the user.
			ServerStatus statResp = (ServerStatus) resp;
			Util.debug(statResp.toString());
			hasServer = statResp.isServerUp();
			if (_frontend != null)
				_frontend.guiMessage(statResp.getMsg());
			break;
		case WAYS:
			//tell the front end to tell the map to render the new ways
			Util.debug("Got ways");
			WayResponse wayResp = (WayResponse) resp;
			
			//TODO: maybe turn back on
			if (matchesRange(wayResp.getMinMaxLatLon())) {
				_frontend.setWays(wayResp.getWays());
				_wayCacher.submit(new CacheThread(wayResp.getWays(), false));
			}
			break;
		case TRAFFIC:
			TrafficResponse tResp = (TrafficResponse) resp;
			if (tResp.getStatus()) {
				MapFactory.putTrafficValue(tResp.getName(), tResp.getVal());//put traffic value i server side map
				if (!_trafficConnected) {
					//only want to notify of reconnection once
					_frontend.trafficConnection(true); //tell GUI that the traffic connection is solid
					_trafficConnected = true;
				}
				_frontend.refreshMap(); //repaint map pane
			} else {
				_frontend.trafficConnection(false); //tell GUI traffic connection is bad
				_trafficConnected = false;
			}
			break;
		case CLIENT_CONNECT:
			ClientConnectionResponse ccResp = (ClientConnectionResponse) resp;
			Constants.MINIMUM_LATITUDE = ccResp.getMinLat();
			Constants.MAXIMUM_LATITUDE = ccResp.getMaxLat();
			Constants.MINIMUM_LONGITUDE = ccResp.getMinLon();
			Constants.MAXIMUM_LONGITUDE = ccResp.getMaxLon();
			MapFactory.setTrafficMap(ccResp.getTrafficMap());
			_tempWays = ccResp.getWays(); //store for later
			break;
		default:
			throw new IllegalArgumentException("Unsupported Response type");
		}
	}
	
	public boolean hasTrafficConnection() {
		return _trafficConnected;
	}
	
	private boolean matchesRange(double[] respCorners) {
		if (_minLat == respCorners[0] &&
			_maxLat == respCorners[1] &&
			_minLon == respCorners[2] &&
			_maxLon == respCorners[3])
			return true;
		return false;
	}
	
	public Dimension getFrameSize() {
		return _frontend.getSize();
	}
	
	public LinkedList<Way> getAndNullInitialWays() {
		if (_tempWays != null) {
			LinkedList<Way> l = new LinkedList<>(_tempWays);
			_tempWays = null;
			return l;
		}
		return new LinkedList<>();
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
						Util.out("Server has closed.");
						_frontend.guiMessage("WARNING: Server unavailable", 7);
						return;
					} else if (e instanceof SocketException) {
						Util.err("Server unavailable. Please try again later");
						break;
					}
					Util.err("ERROR reading line from socket or write to STD_OUT");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
