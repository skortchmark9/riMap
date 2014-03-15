package cli;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MapsCLI {

	MapsCLI() {
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
				new PathTester(userInput);
			}
		}
	}

}