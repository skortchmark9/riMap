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
	private boolean _connect = false;
	private Server _server;
	
	/**
	 * Default constructor
	 */
	public TrafficSocket(int port, Server server) {
		int num_attempts = 0;
		while(!_connect) {
			num_attempts++;
			try {
				_server = server;
				_port = port;
				_socket = new Socket("localhost", _port);
				_input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
				if (num_attempts > 1)
					_server.serverOKMessage("Established Connection to traffic server!");
				_connect = true;
			} catch (IOException e) {
				if (num_attempts == 1) {
					Util.err("WARNING: Unable to connect to traffic bot. Continuing without traffic data.");
					_server.trafficUpdate("", 0.0, false);
				}
				_connect = false;
				try {
					Thread.sleep(10000); //try to connect again in 10 seconds
					Util.out("Attempting to reconnect to traffic bot...");
				} catch (InterruptedException e1) {
					Util.err("ERROR connecting to traffic bot. Traffic data will be unavailable.");
				}
			}
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
							_server.trafficUpdate(name, val, true);
						} catch(NumberFormatException e) {
							Util.err("WARNING: traffic server sent a funky value"); //notify but keep reading
						}
					}
				}
			} catch (IOException e) {
				Util.err("Connection dropped by traffic bot");
				_server.trafficUpdate("", 0.0, false);
			} finally {
				kill(); //kill when finished
			}
		}
	}
	
	/**
	 * Kills & cleans up the resources of this socket.
	 */
	private void kill() {
			Util.debug("Killing traffic socket...");
			
			try {
				_socket.close();
				_input.close();
			} catch (IOException e) {
				//do nothing with this, just exit
			}
	}
}
