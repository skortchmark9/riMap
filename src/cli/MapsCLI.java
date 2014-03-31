package cli;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import backend.Backend;

/**
 * This class runs the command-line interface of the 
 * program.
 * @author skortchm
 *
 */
public class MapsCLI implements Runnable{
	
	Backend b;
	
	/**
	 * Default constructor.
	 * sets its internal reference to the parametric backend and
	 * runs itself.
	 * @param b - the backend to use to drive the CLI
	 */
	public MapsCLI(Backend b) {
		this.b = b;
		run();
	}

	/**
	 * Runs the CLI.
	 * spins forever to read from Standard in.
	 * The app will exit when the user enters "exit", "quit", "EOF", or 
	 * simple the empty string, which is usually sent with CTRL+D. 
	 */
	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String userInput = "";
		while(true) {
			System.out.println("\nReady");
			try {
				userInput = reader.readLine();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			if (userInput.equals("exit") || userInput.equals("quit")) //Exit command
				break;
			else if (userInput.equals("EOF")) //Exit command
				break;
			else if (userInput.equals("")) //Empty line is exit as well.
				break;
			else {
				new PathTester(userInput, b);
			}
		}
	}

}