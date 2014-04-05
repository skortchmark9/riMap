package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kdtree.KDimensionable;
import maps.Node;
import maps.Way;
import shared.Request;
import shared.Request.RequestType;
import shared.Response;
import shared.Response.ResponseType;

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
	private String IP;
	private List<Response> responses;
	ExecutorService executor;


	/**
	 * Constructs a Client with the given port.
	 * @param port the port number the client will connect to
	 */
	public Client(String IPAddress, int port) {
		executor = Executors.newFixedThreadPool(5);
		_port = port;
		IP = IPAddress;
	}

	/**
	 * Starts the Client, so it connects to the sever.
	 * It will set up all the necessary requirements, 
	 * and then launch the GUI.
	 */
	public void start() {
		try {
			_socket = new Socket((IP.equals("localhost")) ? InetAddress.getLocalHost(): InetAddress.getByName(IP), _port);
			_input = new ObjectInputStream(_socket.getInputStream());
			_output = new ObjectOutputStream(_socket.getOutputStream());
			_running = true;
			_thread = new ReceiveThread();
			_thread.start();
		}
		catch (IOException ex) {
			err("ERROR: Can't connect to server");
		}
	}

	/**
	 * A method that sends a message to the server.
	 * @param message that will be sent to the server for broadcasting.
	 * @throws IOException 
	 */
	public void request(Request r) throws IOException {
		if (r.getType() == RequestType.EXIT) {
			this.kill();
		} else {
			_output.writeObject(r);
			_output.flush();
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
	
	public List<String> getAutoCorrections(String input) {
		return null;
	}
	
	public List<Node> getNearestNeighbors(int i, KDimensionable kd) {
		return null;
	}
	
	public List<Way> getWaysInRange(double minLat, double maxLat, double minLon, double maxLon) {
		return null;
	}
	
	public void removeAllResponses(ResponseType type) {
		Iterator<Response>  itr = responses.iterator();
		while(itr.hasNext()) {
			if (itr.next().getType() == type)
				itr.remove();
		}
	}
	
	public boolean isReady() {
		return _running;
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
					responses.add(received);
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
