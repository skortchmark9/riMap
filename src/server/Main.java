/**
 * 
 */
package server;

import java.io.IOException;

import backend.Backend;
import backend.Constants;
import backend.Resources;
import backend.Util;

/**
 * @author emc3
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 6) {
			Util.err("ERROR: incorrect number of arguments.\n", "Usage: trafficServer <ways> <nodes> <index> <hostname> <trafficport> <serverport>" );
			return;
		}
		
		int serverPort, trafficPort;
		//get traffic port
		try {
			trafficPort = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			trafficPort = Constants.DEFAULT_TRAFFIC_PORT;
			Util.err("WARNING: Invalid argument for traffic port. setting to default port #:", trafficPort);
		}
		
		//Get server port
		try {
			serverPort = Integer.parseInt(args[5]);
		} catch (NumberFormatException e) {
			serverPort = Constants.DEFAULT_SERVER_PORT;
			Util.err("WARNING: Invalid argument for server port. setting to default port #:", serverPort);
		}
		
		TrafficSocket trafficSocket = new TrafficSocket(trafficPort);
		trafficSocket.start();
		
		//initialize resource files
		try {
			new Resources(args[0], args[1], args[2]);
		} catch (IOException e) {
			Util.err("ERROR trying to generate Resources");
			return;
		}
		
		//initialize backend
		Backend b = new Backend();
		b.initBackend();
		
		//create a new Server using the backend we just made:
		
		
	}

}
