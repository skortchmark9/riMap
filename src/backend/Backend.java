package backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import autocorrect.Engine;
import autocorrect.RadixTree;
import maps.MapFactory;
import maps.Node;
import maps.PathNodeWrapper;
import maps.Way;
import edu.brown.cs032.emc3.kdtree.KDStub;
import edu.brown.cs032.emc3.kdtree.KDTree;
import edu.brown.cs032.emc3.kdtree.KDimensionable;

public class Backend {
	
	KDTree<Node> kd = null;
	Engine autoCorrectEngine = null;
	
	public Backend(String[] args) throws IOException {
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
		initKDTree();
		initAutoCorrect();
	}
	
	private void initKDTree() {
		if (Constants.DEBUG_MODE) {
			Util.resetClock();
			Util.out("Start KD Build");
			Util.memLog();
		}
		kd = MapFactory.createKDTree();
		if (Constants.DEBUG_MODE) {
			Util.out("End KD Build (Elapsed:", Util.lap()+")");
			Util.memLog();
		}
	}
	
	private void initAutoCorrect() {
		if (Constants.DEBUG_MODE) {
			Util.resetClock();
			Util.out("Start Autoc build");
			Util.memLog();
		}
		
		RadixTree rt = MapFactory.createRadixTree();
		if (rt.isEmpty()) {
			Util.err("ERROR: Could not instantiate AutoCorrectEngine");
			return;
		} else {
			if(Constants.DEBUG_MODE) System.out.print("Done with radix tree. building engine...");
			autoCorrectEngine = new Engine(Constants.defaultGenerator, Constants.defaultRanker, rt);
			if(Constants.DEBUG_MODE) System.out.println("Done.");
		}
		if (Constants.DEBUG_MODE) {
			Util.out("End Autoc build (Elapsed:", Util.lap()+")");
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
	
	public List<Node> getNearestNeighbors(int num, KDimensionable testPoint) {
		if (kd != null) {
			return kd.getNearestNeighbors(num, testPoint);
		} else {
			Util.err("ERROR: KD TREE NOT INITIALIZED");
			return null;
		}
	}
	
	public List<Way> getWaysInRange(double minLat, double maxLat, double minLon, double maxLon) {
		if (Constants.DEBUG_MODE) {
			Util.out("Looking for Ways in Range");
			Util.resetClock();
		}
		double lat1 = minLat + maxLat / 2;
		double lon1 = minLon + maxLon / 2;
		//The midpoint of the map range. 
		KDimensionable midpoint = new KDStub(lat1, lon1);
		
		double radiusSquared = Math.pow((maxLat - lat1), 2) + Math.pow(maxLon - lon1, 2);
		List<Node> nodes = kd.getObjectsWithinRadius(Math.sqrt(radiusSquared), midpoint); //changed to use square root
		List<Way> ways = new ArrayList<>();
		for(Node n : nodes) {
			for(String wayID : n.getWayIDs()) {
				ways.add(MapFactory.createWay(wayID));
			}
		}
		return ways;
	}

}
