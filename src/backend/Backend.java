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
		try {
			new Resources(args[0], args[1], args[2]);
		} catch (IOException e) {
			System.out.println("ERROR: Could not generate Resources");
			throw e;
		}
		initKDTree();
		initAutoCorrect();
	}
	
	private void initKDTree() {
		kd = MapFactory.createKDTree();
	}
	
	private void initAutoCorrect() {
		RadixTree rt = MapFactory.createRadixTree();
		if (rt.isEmpty()) {
			System.out.println("ERROR: Could not instantiate AutoCorrectEngine");
			return;
		} else {
			autoCorrectEngine = new Engine(Constants.defaultGenerator, Constants.defaultRanker, rt);
		}
	}
	
	public List<String> getAutoCorrections(String name) {
		if (autoCorrectEngine != null) {
			return autoCorrectEngine.suggest(name);
		} else {
			System.err.println("ERROR: AutoCorrectEngine is not initialized");
			return null;
		}
	}
	
	public List<Node> getNearestNeighbors(int num, KDimensionable testPoint) {
		if (kd != null) {
			return kd.getNearestNeighbors(num, testPoint);
		} else {
			System.err.println("ERROR: KD TREE NOT INITIALIZED");
			return null;
		}
	}
	
	public List<Way> getWaysInRange(double minLat, double maxLat, double minLon, double maxLon) {
		double lat1 = minLat + maxLat / 2;
		double lon1 = minLon + maxLon / 2;
		//The midpoint of the map range. 
		KDimensionable midpoint = new KDStub(lat1, lon1);
		
		double radiusSquared = Math.pow((maxLat - lat1), 2) + Math.pow(maxLon - lon1, 2);
		List<Node> nodes = kd.getObjectsWithinRadius(radiusSquared, midpoint);
		List<Way> ways = new LinkedList<>();
		for(Node n : nodes) {
			for(String wayID : n.getWayIDs()) {
				ways.add(MapFactory.createWay(wayID));
			}
		}
		return ways;
	}
	
	public static List<Way> getWays_TEST() {
		List<Way> waysList = new ArrayList<>();
		//TOPLEFT: 41.842678, -71.417541
//		waysList.add(new Way("one", "one", new Node("oneStart", 41.842678, -71.417541, null), new PathNodeWrapper(new Node("oneEnd", 41.822678, -71.397541, null))));
//		waysList.add(new Way("two", "two", new Node("twoStart", 41.832678, -71.407541, null), new PathNodeWrapper(new Node("twoEnd", 41.832678, -71.404541, null))));
		return waysList;
	}

}
