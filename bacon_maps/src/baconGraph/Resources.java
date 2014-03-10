package baconGraph;

import java.io.IOException;

import baconGraph.BinarySearchFile.ParseType;

public class Resources {
	/** holds the files to be used in the program for searches. */
	public BinarySearchFile moviesFile, actorsFile, indexFile = null;
	
	Resources(String films, String actors, String index) throws IOException {
		moviesFile = new BinarySearchFile(films, ParseType.FILM);
		actorsFile = new BinarySearchFile(actors, ParseType.ACTOR);
		indexFile = new BinarySearchFile(index, ParseType.INDEX);
	}
	
	Resources() throws IOException {
		moviesFile = new BinarySearchFile("./data/files/films.tsv", ParseType.FILM);
		actorsFile = new BinarySearchFile("./data/files/actors.tsv",ParseType.ACTOR);
		indexFile =  new BinarySearchFile("./data/files/index.tsv", ParseType.INDEX);
	}

	public void closeResources() {
		moviesFile.close();
		actorsFile.close();
		indexFile.close();
	}
}
