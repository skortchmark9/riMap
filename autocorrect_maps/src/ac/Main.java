package ac;

import java.util.LinkedList;
import java.util.List;

public class Main {
	/** Main class for handling the args and then delegating to commandline or gui*/

	public static void main(String[] args) {
		int led = 0;
		boolean prefix = false;
		boolean whitespace = false;
		int ranktype = 0;
		boolean gui = false;
		Generator g;
		Ranker r;
		Engine e;
		List<String> files = new LinkedList<>(); //here we parse the arguments of the file
		for(int i = 0; i < args.length; i++) {
			if (args[i].equals("--led")) { //if --led is an arg, then a number will immediately follow
				try {
					led = Integer.parseInt(args[i + 1]);
					i++;
				} catch (NumberFormatException nfe) {
					throw new NumberFormatException("The levenshtein distance: " + args[ i+ 1] + " is invalid.");
				}
			}
			else if (args[i].equals("--prefix"))
				prefix = true;
			else if (args[i].equals("--whitespace"))
				whitespace = true;
			else if (args[i].equals("--smart"))
				ranktype = 1;
			else if (args[i].equals("--gui"))
				gui = true;
			else {
				files.add(args[i]);
			}
		}
		if (prefix && whitespace) {
			g = new Generator(led, Generator.SuggestType.PREFIX, Generator.SuggestType.WHITESPACE);
		}
		else if (prefix) {
			g = new Generator(led, Generator.SuggestType.PREFIX);
		}
		else if (whitespace) {
			g = new Generator(led, Generator.SuggestType.WHITESPACE);
		}
		else {
			g = new Generator(led);
		}
		r = new Ranker(ranktype);
		e = new Engine(g, r, files);

		if (gui)
			new AutocorrectGUI(e); //The GUI doesn't need any files args to run properly
		else {
			if (files.size() < 1) {//However, the CommandLine does. 
				System.out.println("ERROR: NO FILES");
				return;
			}
			else {
				AutocorrectCLI cl = new AutocorrectCLI(e);
				cl.run();
			}
		}
	}
}
