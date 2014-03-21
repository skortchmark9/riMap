package backend;

import graph.Edge;
import graph.PathFinder;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import kdtree.KDTree;
import kdtree.KDimensionable;
import maps.MapFactory;
import maps.Node;
import maps.PathNodeWrapper;
import maps.Way;
import autocorrect.Engine;
import autocorrect.RadixTree;


/**
 * Backend class for the Maps application.
 * the same backend is used to drive both the command-line-interface and the 
 * GUI.
 * 
 * the backend is sort of used like an interface between the FrontEnd or CLI and
 * the MapFactory and various other classes that actually do the hard work of the program.
 * @author emc3 / skortchm
 *
 */
public class Backend {

	KDTree<Node> kd = null; //the KDTree!
	Engine autoCorrectEngine = null;
	public enum BackendType {KD, AC};
	
	/**
	 * Main constructor for the backend.
	 * Init's the ways / nodes / index files by creating a new Resources(),
	 * BackendType is an enum used for testing. 
	 * @param args - an array containing the filepaths of the resource files (ways.tsv / nodes.tsv / index.tsv)
	 * @param ts - the Backend type. used for testing.
	 * @throws IOException if the files are invalid and Resources could not be instantiated properly. 
	 */
	public Backend(String[] args, BackendType...ts) throws IOException {
		if (args.length != 3) {
			Util.err("ERROR: Incorrect number of resources");
			throw new IOException();
		}
		try {
			new Resources(args[0], args[1], args[2]);
		} catch (IOException e) {
			Util.err("ERROR: Could not generate Resources");
			throw e;
		}
		List<BackendType> options = Arrays.asList(ts);
		if (options.size() == 0) {
			initAutoCorrect();
			initKDTree();
		} else {
			if (options.contains(BackendType.KD)) {
				initKDTree();			
			}
			if (options.contains(BackendType.AC)) {
				initAutoCorrect();
			}
		}
	}
	
	/**
	 * Initializes a new KDTree by querying MapFactory
	 */
	private void initKDTree() {
		long start = 0; //FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			start = Util.resetClock();
			Util.out("Start KD Build");
			Util.memLog();
		}
		
		try {
			kd = MapFactory.createKDTree();
		} catch (IOException e) {
			Util.err("ERROR: Loading KDTree from nodes file failed");
			System.exit(1);
		}

		if (Constants.DEBUG_MODE) {
			Util.out("Created KDTree from Node List", "(Elapsed:", Util.lap()+")");
			Util.out("End KD Build (Total Elapsed:", Util.timeSince(start)+")");
			Util.memLog();
		}
	}
	
	/**
	 * Initializes the AutoCorrect tree by querying the MapFactory
	 */
	private void initAutoCorrect() {
		long start = 0; //XXX: FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			start = Util.resetClock();
			Util.out("Start Autoc build");
			Util.memLog();
		}

		RadixTree rt; //we store our words in a RadixTree rather than a Trie.
		try {
			rt = MapFactory.createRadixTree();
			if (rt.isEmpty()) {
				Util.err("ERROR: Could not instantiate AutoCorrectEngine");
				return;
			} else {
				if(Constants.DEBUG_MODE) Util.out("Done with radix tree. building engine...");
				autoCorrectEngine = new Engine(Constants.defaultGenerator, Constants.defaultRanker, rt);
				if(Constants.DEBUG_MODE) Util.out("Done.");
			}
			if (Constants.DEBUG_MODE) {
				Util.out("End Autoc build (Total Elapsed:", Util.timeSince(start)+")");
				Util.memLog();
			}
		} catch (IOException e) {
			Util.out("ERROR: Unable to load RadixTree from index file");
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public List<String> getAutoCorrections(String name) {
		if (autoCorrectEngine != null) {
			return autoCorrectEngine.suggest(name);
		} else {
			Util.err("ERROR: AutoCorrectEngine is not initialized");
			return null;
		}
	}
	
	public List<Way> getPath(Node source, Node dest) {
		PathFinder<PathNodeWrapper, Node> p = new PathFinder<>(new PathNodeWrapper(source), new PathNodeWrapper(dest));
		List<Way> ways = new LinkedList<>();
		for(Edge<?> edge : p.getPath()) {
			ways.add((Way) edge);
		}
		return ways;
	}

	public List<Way> getWaysInRange(double minLat, double maxLat, double minLon, double maxLon) {
		return MapFactory.getWaysInRange(minLat, maxLat, minLon, maxLon);
	}

	public List<Node> getNearestNeighbors(int num, KDimensionable testPoint) {
		if (kd != null) {
			return kd.getNearestNeighbors(num, testPoint);
		} else {
			Util.err("ERROR: KD TREE NOT INITIALIZED");
			return null;
		}
	}	
}
