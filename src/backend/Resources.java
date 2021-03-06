package backend;

import java.io.IOException;


public class Resources {
	/** holds the files to be used in the program for searches. */
	public static BinarySearchFile waysFile, nodesFile, indexFile = null;
	
	public Resources(String ways, String nodes, String index) throws IOException {
		waysFile = new BinarySearchFile(ways, "id", "id", "name", "start", "end");
		nodesFile = new BinarySearchFile(nodes, "id", "id", "latitude", "longitude");
		indexFile = new BinarySearchFile(index, "name", "name", "nodes");
	}
	
	public Resources(int i) throws IOException {
		waysFile = new BinarySearchFile("./data/films.tsv", "id", "id", "name", "starring");
		nodesFile = new BinarySearchFile("./data/actors.tsv", "id", "id", "name", "film");
		indexFile =  new BinarySearchFile("./data/baconindex.tsv", "name", "name", "id");
	}
	
	public Resources() throws IOException {
		waysFile = new BinarySearchFile("./data/films.tsv", "id", "id", "name", "start", "end");
		nodesFile = new BinarySearchFile("./data/actors.tsv", "id", "id", "latitude", "longitude");
		indexFile =  new BinarySearchFile("./data/index.tsv", "name", "name", "nodes");
	}

	public static void closeResources() {
		waysFile.close();
		nodesFile.close();
		indexFile.close();
	}
}
