package maps;

import java.io.IOException;

import maps.BinarySearchFile.ParseType;

public class Resources {
	/** holds the files to be used in the program for searches. */
	public BinarySearchFile moviesFile, actorsFile, indexFile = null;
	
	Resources(String nodes, String ways, String index) throws IOException {
		moviesFile = new BinarySearchFile(nodes, ParseType.FILM);
		actorsFile = new BinarySearchFile(ways, ParseType.ACTOR);
		indexFile = new BinarySearchFile(index, ParseType.INDEX);
	}
	
	
	Resources() throws IOException {
		moviesFile = new BinarySearchFile("./data/baconfiles/films.tsv", ParseType.FILM);
		actorsFile = new BinarySearchFile("./data/baconfiles/actors.tsv",ParseType.ACTOR);
		indexFile =  new BinarySearchFile("./data/baconfiles/index.tsv", ParseType.INDEX);
	}

	public void closeResources() {
		moviesFile.close();
		actorsFile.close();
		indexFile.close();
	}
}
