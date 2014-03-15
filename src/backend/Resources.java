package backend;

import java.io.IOException;


public class Resources {
	/** holds the files to be used in the program for searches. */
	public static BinarySearchFile nodesFile, waysFile, indexFile = null;
	
	Resources(String nodes, String ways, String index) throws IOException {
		nodesFile = new BinarySearchFile(nodes, "id", "id", "latitude", "longitude");
		waysFile = new BinarySearchFile(ways, "id", "id", "name", "start", "end");
		indexFile = new BinarySearchFile(index, "name", "name", "nodes");
	}
	
	Resources(int i) throws IOException {
		nodesFile = new BinarySearchFile("./data/baconfiles/actors.tsv", "id", "id", "name", "film");
		waysFile = new BinarySearchFile("./data/baconfiles/films.tsv", "id", "id", "name", "starring");
		indexFile =  new BinarySearchFile("./data/baconfiles/index.tsv", "name", "name", "id");
	}
	
	Resources() throws IOException {
		nodesFile = new BinarySearchFile("./data/baconfiles/actors.tsv", "id", "id", "latitude", "longitude");
		waysFile = new BinarySearchFile("./data/baconfiles/films.tsv", "id", "id", "name", "start", "end");
		indexFile =  new BinarySearchFile("./data/baconfiles/index.tsv", "name", "name", "nodes");
	}

	public void closeResources() {
		nodesFile.close();
		waysFile.close();
		indexFile.close();
	}
}
