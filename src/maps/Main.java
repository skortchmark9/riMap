package maps;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cli.MapsCLI;
import backend.Backend;
import frontend.Frontend;

public class Main {
	
	/**
	 * the main line of the Maps program.
	 * Parses the arguments for correctness.
	 * The arguments should be of the form 
	 * <p>
	 * &lt;wayz&gt; &lt;nodez&gt; &lt;indecks&gt;
	 * <p>
	 * If the --gui flag is set, a new FrontEnd() is created
	 * otherwise the program will run simpleton in the command line. 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean gui = false;
		List<String> WNI = new LinkedList<>();
		if (!(args.length == 4 || args.length == 3)) {
			System.out.println("ERROR: Incorrect number of arguments.");
			System.exit(1);
		} else {
			for(int i = 0; i < args.length; i++) {
				if (args[i].equals("--gui")) {
					gui = true;
				} else {
					WNI.add(args[i]);
				}
			}
		}
		Backend b = null;
			try {
				b = new Backend(WNI.toArray(new String[3]));
			} catch (IOException e) {
			System.out.println("ERROR: Could not instantiate backend");
			System.exit(1);
		}
		
		if (gui) {
			new Frontend(b);
		} else {
			new MapsCLI(b);
		}
	}
}
