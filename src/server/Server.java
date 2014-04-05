/**
 * 
 */
package server;

/**
 * @author emc3
 *
 */
public class Server extends Thread {
	private int _port;
	private ServerSocket _socket;
	private ClientPool _clientPool;
	private boolean _running;
	Backend _backend;
	
	
	/**
	 * 
	 */
	public Server(int serverPort, Backend b) {
		
	}
	
	public void run() {
		
	}

}
