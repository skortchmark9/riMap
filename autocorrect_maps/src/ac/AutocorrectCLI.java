package ac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AutocorrectCLI {
	/** The commandline interface for autocorrect.
	 *  Most of the work is done in the engine.*/
	Engine e;
	AutocorrectCLI(Engine e) {
		this.e = e;
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
				for(String s : e.suggest(userInput)) { //Pass input straight to the engine.
					System.out.println(s);
				}
			}
		}
	}

}
