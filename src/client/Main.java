package client;
import java.io.*;

import backend.Constants;
import backend.Util;

public class Main {

	// Change the port number if the port number is already being used.
	private static final int DEFAULT_PORT = 9850;
	public static void main(String[] args) throws IOException {
		
		if (args.length != 2) {
			Util.out("Incorrect number of arguments\n","Usage: trafficClient <hostname> <serverport>");
			return;
		}
		
		int port;
		
		try {
			port = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			port = Constants.DEFAULT_SERVER_PORT;
			Util.err("WARNING: Invalid argument for server port. setting to default port #:", port);
		}
//		
//		if (condition) {
//			
//		}
		
		Client client = new Client(args[0], port);
		client.start();
	}
}