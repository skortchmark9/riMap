package backend;

import graph.Edge;
import graph.PathFinder;
import graph.PathNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import kdtree.KDStub;
import kdtree.KDTree;
import kdtree.KDimensionable;
import backend.BinarySearchFile.SearchType;
import autocorrect.Engine;
import autocorrect.RadixTree;
import maps.MapFactory;
import maps.Node;
import maps.PathNodeWrapper;
import maps.Way;

public class Backend {

	KDTree<Node> kd = null;
	Engine autoCorrectEngine = null;
	public enum BackendType {KD, AC};

	public Backend(String[] args, BackendType...ts) throws IOException {
		if (args.length != 3) {
			Util.err("ERROR: Incorrect number of resources");
			System.exit(1);
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

	private void initKDTree() {
		long start = 0; //FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			start = Util.resetClock();
			Util.out("Start KD Build");
			Util.memLog();
		}

		kd = MapFactory.createKDTree();

		if (Constants.DEBUG_MODE) {
			Util.out("Created KDTree from Node List", "(Elapsed:", Util.lap()+")");
			Util.out("End KD Build (Total Elapsed:", Util.timeSince(start)+")");
			Util.memLog();
		}
	}

	private void initAutoCorrect() {
		long start = 0; //XXX: FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			start = Util.resetClock();
			Util.out("Start Autoc build");
			Util.memLog();
		}

		RadixTree rt = MapFactory.createRadixTree();
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
	}

	public List<String> getAutoCorrections(String name) {
		if (autoCorrectEngine != null) {
			return autoCorrectEngine.suggest(name);
		} else {
			Util.err("ERROR: AutoCorrectEngine is not initialized");
			return null;
		}
	}
	
	public List<Way> getPath(Node source, Node dest) {
		PathFinder<PathNodeWrapper, Node> p = new PathFinder<PathNodeWrapper, Node>(new PathNodeWrapper(source), new PathNodeWrapper(dest));
		List<Way> ways = new LinkedList<>();
		for(Edge<? extends PathNode<Node>> edge : p.getPath()) {
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
