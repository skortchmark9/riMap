package backend;

import java.io.IOException;
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
import frontend.LoadingPane;
import graph.Edge;
import graph.PathFinder;


/**
 * Backend class for the Maps application.
 * the same backend is used to drive both the command-line-interface and the 
 * GUI.
 * 
 * the backend is sort of used like an interface between the FrontEnd or CLI and
 * the MapFactory and various other classes that actually do the hard work of the program.
 * @author emc3 / skortchm
 */
public class Backend {

	KDTree<Node> kd = null; //the KDTree!
	Engine autoCorrectEngine = null;
	volatile boolean done;
	LoadingPane l = null;

	/**
	 * Main constructor for the backend.
	 */
	public Backend() {
		done = false;
	}
	
	public void setLoadingScreen(LoadingPane l) {
		this.l = l;
	}
	
	public void initBackend() {
		new BackendInitializer().start();;
	}
		
	public boolean isDone() {
		return done;
	}
	
	LoadingPane getLoadingScreen() {
		return l;
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
			kd = MapFactory.createKDTree(l);
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
	
	void initBoundaries() {
		if (kd == null) {
			Util.err("ERROR: Could not load boundaries because KD Tree is not initialized");
		}
		if (l != null) {
			l.updateProgress("Initializing boundaries", 95);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		Constants.MAXIMUM_LATITUDE = kd.getMax(0);
		Constants.MINIMUM_LATITUDE = kd.getMin(0);
		Constants.MAXIMUM_LONGITUDE = kd.getMax(1);
		Constants.MINIMUM_LONGITUDE = kd.getMin(1);
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
			rt = MapFactory.createRadixTree(l);
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
		long start = System.currentTimeMillis();		
		List<Way> results = MapFactory.getWaysInRange(minLat, maxLat, minLon, maxLon);
		Util.out("GETTING WAYS TOOK: ", System.currentTimeMillis() - start);
		return results;
	}

	public List<Node> getNearestNeighbors(int num, KDimensionable testPoint) {
		if (kd != null) {
			return kd.getNearestNeighbors(num, testPoint);
		} else {
			Util.err("ERROR: KD TREE NOT INITIALIZED");
			return null;
		}
	}
	
	public class BackendInitializer extends Thread {
		
		@Override
		public void run() {
			if (l != null) {
			l.updateProgress("Creating Backend", 0);
			}
			initAutoCorrect();
			initKDTree();
			initBoundaries();
			if (l != null) {
			l.updateProgress("Done!", 100);
			}
			done = true;
		}
	}
}

