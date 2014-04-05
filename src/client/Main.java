package client;
import java.io.*;

public class Main {

	// Change the port number if the port number is already being used.
	private static final int DEFAULT_PORT = 9850;
	public static void main(String[] args) throws IOException {
		// Launch a chat server on the default port.
		int port = DEFAULT_PORT;
		if (args.length == 0) {
			args = new String[] {"localhost"};
		}
		Client client = new Client(args[0], port);
		client.start();
	}
}