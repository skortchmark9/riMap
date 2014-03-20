package maps;

import java.util.List;

import backend.BinarySearchFile;
import backend.Resources;
import backend.BinarySearchFile.SearchType;

class SearchMultipleWorker implements Runnable {
	private List<List<String>> wayInfoChunk;
	private String searchCode;
	
	/**
	 * Default Constructor for this worker.
	 * Takes a synchronized list and a searchCode
	 * and stores their references for use in run()
	 * 
	 * @param synchronizedList - the synchronized list we pass, shared across
	 * the thread pool which uses these workers. adding to it concurrently should 
	 * NOT be a problem.  
	 * @param searchCode - the string to search for
	 */
	SearchMultipleWorker(List<List<String>> synchronizedList, String searchCode) {
		this.wayInfoChunk = synchronizedList;
		this.searchCode = searchCode;
	}
	
	/**
	 * Queries the waysFile in Resources for way info 
	 * using a "wild card" search.
	 * The search code for this query is the search code 
	 * supplied to this worker's constructor.
	 */
	@Override
	public void run() {
		BinarySearchFile f = new BinarySearchFile(Resources.waysFile);
		List<List<String>> templist = f.searchMultiples(searchCode, SearchType.WILDCARD, "id", "name", "start", "end");
		synchronized(wayInfoChunk) {
			wayInfoChunk.addAll(templist);
		}
		//wayInfoChunk.addAll(Resources.waysFile.searchMultiples(searchCode, SearchType.WILDCARD, "id", "name", "start", "end"));
	}
	
}