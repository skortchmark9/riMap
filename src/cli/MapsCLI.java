package cli;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import backend.Backend;

public class MapsCLI implements Runnable{
	
	Backend b;

	MapsCLI(Backend b) {
		this.b = b;
		run();
	}
	public static void main(String[] args) {
		Backend b;
		try {
			b = new Backend(args);
			new MapsCLI(b);
		} catch (IOException e) {
			System.err.print("ERROR: Could not load backend");
			System.exit(1);
		}
	}
	
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
			if (userInput.equals("exit")) //Exit command
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