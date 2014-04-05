/**
 * 
 */
package server;

import backend.Constants;
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
		
	}

}
