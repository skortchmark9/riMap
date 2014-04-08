package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import maps.MapFactory;
import backend.Constants;
import backend.Util;

/**
 * 
 * @author emc3
 *
 */
public class TrafficSocket extends Thread {
	
	private int _port;
	private Socket _socket;
	private BufferedReader _input;
	private boolean _connect = true;
	private Server _server;
	
	/**
	 * Default constructor
	 */
	public TrafficSocket(int port, Server server) {
		try {
			_server = server;
			_port = port;
			_socket = new Socket("localhost", _port);
			_input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		} catch (IOException e) {
			Util.err("ERROR connnecting to traffic bot. Please check the port number.");
			_connect = false;
		}
	}
	
	/**
	 * Runs a loop which waits for traffic data and 
	 * puts the data in MapFactory's trafficMap.
	 */
	public void run() {
		if(_connect) {
			try {
				String line;
				while ((line = _input.readLine()) != null) {
					String[] respData = line.split("\t");
					if (respData.length == 2) {
						try {
							String name = respData[0].toLowerCase();
							Double val = Double.parseDouble(respData[1]);
							MapFactory.putTrafficValue(name, val); //store the traffic info in map factory.
							_server.trafficUpdate(name, val);
						} catch(NumberFormatException e) {
							Util.err("WARNING: Looks like the traffic server sent us a funky value (via TrafficSocket)"); //notify but keep reading
						}
					}
				}
				kill(); //kill this socket when done with reading
			} catch (IOException e) {
				Util.err("ERROR IO exception (prob bad read of traffic info from server).");
			}
		}
	}
	
	/**
	 * Kills & cleans up the resources of this socket.
	 */
	private void kill() throws IOException {
			if (Constants.DEBUG_MODE) {
				Util.out("Killing traffic socket...");
			}
			_socket.close();
			_input.close();
	}

}
