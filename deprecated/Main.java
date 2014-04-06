package maps;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import backend.Backend;
import backend.Resources;
import backend.Util;
import cli.MapsCLI;
import frontend.IndeterminateFrontend;

public class Main {

	/**
	 * the main line of the Maps program.
	 * Parses the arguments for correctness.
	 * The arguments should be of the form 
	 * <p>
	 * &lt;wayz&gt; &lt;nodez&gt; &lt;indecks&gt;
	 * <p>
	 * If the --gui flag is set, a new {@link FrontEnd} is created
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
		try {
			new Resources(WNI.get(0), WNI.get(1), WNI.get(2));
		} catch (IOException e) {
			Util.err("ERROR: Could not generate Resources");
			System.exit(1);
		}
		Backend b = new Backend();
		b.initBackend();
		if (gui) {
			new IndeterminateFrontend(b); //TODO: use client instead of backend 
		}
		else {
			new MapsCLI(b);
		}
	}
}
