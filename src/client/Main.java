package client;
import java.io.*;

import backend.Constants;
import backend.Util;

public class Main {

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
		
		Util.debug("Attempting to create new client.");
		
		Client client = new Client(args[0], port);
		
		Util.debug("client created. \nattempting to start client");
		
		client.start();
	}
}