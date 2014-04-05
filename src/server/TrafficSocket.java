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
	
	/**
	 * Default constructor
	 */
	public TrafficSocket(int port) {
		try {
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
							Double val = Double.parseDouble(respData[1]);
							MapFactory.putTrafficValue(respData[0].toLowerCase(), val); //store the traffic info in map factory.
						} catch(NumberFormatException e) {
							if (Constants.DEBUG_MODE)
								Util.err("Looks like server sent a funky value? (TrafficSocket.run())"); //notify but keep reading
						}
					}
				}
				kill(); //kill this socket when done with reading
			} catch (IOException e) {
				if(e instanceof IOException) 
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
