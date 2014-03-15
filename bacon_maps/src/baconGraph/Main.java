package baconGraph;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		final long startTime = System.currentTimeMillis();
		if (args.length != 5) {
			System.out.println("ERROR: invalid args structure");
			System.exit(1);
		}
		try {
			Resources r = new Resources(args[2], args[3], args[4]);
			Bacon b = new Bacon(args[0], args[1], r);
			b.getPath();
			final long endTime = System.currentTimeMillis();
			r.closeResources();
			System.out.println("TIME TAKEN: " + (endTime - startTime));
			System.exit(0);
		} catch (IOException e) {
			System.out.println("ERROR: BAD FILES");
			System.exit(1);
		}
	}
}
