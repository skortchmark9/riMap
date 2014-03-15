package backend;

import java.io.IOException;

import backend.BinarySearchFile.ParseType;

public class Resources {
	/** holds the files to be used in the program for searches. */
	public static BinarySearchFile nodesFile, waysFile, indexFile = null;
	
	Resources(String nodes, String ways, String index) throws IOException {
		nodesFile = new BinarySearchFile(nodes, ParseType.NODES);
		waysFile = new BinarySearchFile(ways, ParseType.WAYS);
		indexFile = new BinarySearchFile(index, ParseType.INDEX);
	}
	
	Resources() throws IOException {
		nodesFile = new BinarySearchFile("./data/baconfiles/films.tsv", ParseType.NODES);
		waysFile = new BinarySearchFile("./data/baconfiles/actors.tsv",ParseType.WAYS);
		indexFile =  new BinarySearchFile("./data/baconfiles/index.tsv", ParseType.INDEX);
	}

	public void closeResources() {
		nodesFile.close();
		waysFile.close();
		indexFile.close();
	}
}
