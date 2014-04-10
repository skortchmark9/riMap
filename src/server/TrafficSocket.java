package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import maps.MapFactory;
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
	private volatile boolean _connected = false;
	private Server _server;
	
	/**
	 * Default constructor
	 */
	public TrafficSocket(int port, Server server) {
			_server = server;
			_port = port;
	}
	
	/**
	 * Runs a loop which waits for traffic data and 
	 * puts the data in MapFactory's trafficMap.
	 */
	public void run() {
		keepTryingToConnect();
		while(_connected) {
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
				Util.err("ERROR: Connection dropped by traffic bot");
				_connected = false;
				try {
					_input.close();
					_socket.close();
				} catch (IOException e1) {
					Util.err("ERROR closing traffic socket and input stream.");
				}
				keepTryingToConnect();
			}
		}
	}
	
	/**
	 * Persistently tries to connect to the 
	 * traffic bot.
	 */
	private void keepTryingToConnect() {
		boolean first = true;
		while(!_connected) {
			try {
				_socket = new Socket("localhost", _port);
				_input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
				_connected = true;
			} catch (IOException e) {
				//tell clients traffic data is not avail (only on first go)
				if (first) {
					Util.err("WARNING: Unable to connect to traffic bot. Continuing without traffic data.");
					_server.trafficUpdate("", 0.0, false);
					first = false;
				}
				
				//sleep for a bit and then try to reconnect.
				try {
					Thread.sleep(10000); //try to connect again in 10 seconds
				} catch (InterruptedException e1) {
					Util.err("ERROR connecting to traffic bot.");
				}
			}
		}
	}
	
	/**
	 * Kills & cleans up the resources of this socket.
	 */
	void kill() {
			Util.debug("Killing traffic socket");
			
			try {
				_connected = false;
				
				if (_input != null)
					_input.close();
				
				if (_socket != null)
					_socket.close();
				
			} catch (IOException e) {
				//do nothing with this, just exit
			}
	}
}
