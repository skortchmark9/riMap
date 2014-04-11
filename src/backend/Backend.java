package backend;

import graph.Edge;
import graph.PathFinder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import kdtree.KDTree;
import kdtree.KDimensionable;
import maps.MapFactory;
import maps.Node;
import maps.PathNodeWrapper;
import maps.Way;
import server.Server;
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
 */
public class Backend {

	KDTree<Node> kd = null; //the KDTree!
	Engine autoCorrectEngine = null;
	volatile boolean done;
	Server _server;

	/**
	 * Main constructor for the backend.
	 */
	public Backend() {
		done = false;
	}

	public void setMessageDestination(Server server) {
		_server = server;
	}

	/**
	 * Sends a message - depending on the backend's done status, it will be okay or not okay.
	 * @param s
	 */
	public void sendStatusMessage(String s) {
		if (_server != null) {
			if (done) 
				_server.serverOKMessage(s);
			else
				_server.serverDownMessage(s);
		}
	}

	/** Initializes the backend in a separate thread so that the server can accept connections
	public void initBackend() {
		new BackendInitializer().start();;
	}

	public boolean isDone() {
		return done;
	}

	/**
	 * Initializes a new KDTree by querying MapFactory
	 */
	private void initKDTree() {
		long start = 0; //FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			start = Util.resetClock();
			Util.debug("Start KD Build");
			Util.memLog();
		}
		sendStatusMessage("Starting KD Build");
		try {
			kd = MapFactory.createKDTree();
		} catch (IOException e) {
			Util.err("ERROR: Loading KDTree from nodes file failed");
			sendStatusMessage("ERROR: Loading KDTree from nodes file failed");
			System.exit(1);
		}

		if (Constants.DEBUG_MODE) {
			Util.out("Created KDTree from Node List", "(Elapsed:", Util.lap()+")");
			Util.out("End KD Build (Total Elapsed:", Util.timeSince(start)+")");
			Util.memLog();
		}
		sendStatusMessage("Finished KD Build");
	}

	void initBoundaries() {
		if (kd == null) {
			Util.err("ERROR: Could not load boundaries because KD Tree is not initialized");
		}
		sendStatusMessage("Initializing Boundaries");
		Constants.MAXIMUM_LATITUDE = kd.getMax(0);
		Constants.MINIMUM_LATITUDE = kd.getMin(0);
		Constants.MAXIMUM_LONGITUDE = kd.getMax(1);
		Constants.MINIMUM_LONGITUDE = kd.getMin(1);
		Util.debug(" Min Lat:", Constants.MINIMUM_LATITUDE, "\n", "Max lat:", Constants.MAXIMUM_LATITUDE, "\n", "Min Lon:", Constants.MINIMUM_LONGITUDE, "\n", "Max Lon:", Constants.MAXIMUM_LONGITUDE);
	}

	/**
	 * Initializes the AutoCorrect tree by querying the MapFactory
	 * This method also (sneak attack) initializes the map of streetnames/node lengths
	 */
	private void initAutoCorrectAndRoadLength() {
		long start = 0; //XXX: FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			start = Util.resetClock();
			Util.out("Start Autoc build");
			Util.memLog();
		}
		sendStatusMessage("Building Autocorrections");
		RadixTree rt; //we store our words in a RadixTree rather than a Trie.
		try {
			rt = MapFactory.createRadixTreeAndInitRoadLengths();
			if (rt.isEmpty()) {
				Util.err("ERROR: Could not instantiate AutoCorrectEngine");
				return;
			} else {
				if (Constants.DEBUG_MODE) Util.out("Done with radix tree. building engine...");
				sendStatusMessage("Done with radix tree. building engine...");
				autoCorrectEngine = new Engine(Constants.defaultGenerator, Constants.defaultRanker, rt);
				if(Constants.DEBUG_MODE) Util.out("Done.");
			}
			if (Constants.DEBUG_MODE) {
				Util.out("End Autoc build (Total Elapsed:", Util.timeSince(start)+")");
				Util.memLog();
			}
			sendStatusMessage("Done with Autocorrection build");
		} catch (IOException e) {
			Util.out("ERROR: Unable to load RadixTree from index file");
		}
	}

	/**
	 * @param name - gets autocorrections from a given input.
	 * @return - returns a list of strings that are the suggestions
	 */
	public List<String> getAutoCorrections(String name) {
		if (autoCorrectEngine != null) {
			return autoCorrectEngine.suggest(name);
		} else {
			Util.err("ERROR: AutoCorrectEngine is not initialized");
			return null;
		}
	}

	/**
	 * Gets a path from the source and dest nodes. PathFinder wraps the nodes independently for each request.
	 * @param source
	 * @param dest
	 * @return
	 */
	public List<Way> getPath(Node source, Node dest) {
		PathFinder<PathNodeWrapper, Node> p = new PathFinder<>(new PathNodeWrapper(source), new PathNodeWrapper(dest));
		List<Way> ways = new LinkedList<>();
		for(Edge<?> edge : p.getPath()) {
			ways.add((Way) edge);
		}
		return ways;
	}

	/**
	 * Gets all the ways in range 
	 * @param minLat - S.E.
	 * @param maxLat
	 * @param minLon
	 * @param maxLon
	 * @param zoom - the zoom factor of the map pane
	 * @return
	 */
	public List<Way> getWaysInRange(double minLat, double maxLat, double minLon, double maxLon, double zoom) {
		long start = System.currentTimeMillis();		
		List<Way> results = MapFactory.getWaysInRange(minLat, maxLat, minLon, maxLon, zoom);
		Util.debug("GETTING WAYS TOOK: ", System.currentTimeMillis() - start);
		return results;
	}

	/**
	 * Gets a nearest neighbors from the KD tree - returning a node.
	 * @param num - the number of nearest neighbors - almost always 1.
	 * @param testPoint - ththe testpoint - the nearest possible kDImensionable to the node we are looking fo.
	 * @return - the node closest to the testpoint. 
	 */
	public List<Node> getNearestNeighbors(int num, KDimensionable testPoint) {
		if (kd != null) {
			return kd.getNearestNeighbors(num, testPoint);
		} else {
			Util.err("ERROR: KD TREE NOT INITIALIZED");
			return null;
		}
	}
	
	/**
	 * The initaliz ways from the backend - the client loads to these 
	 * ways at the beginning so we cache them.
	 * @return
	 */
	public List<Way> getInitialWays() {
		sendStatusMessage("Finding Ways...");
		//TODO: is this too wide?
		return MapFactory.getWaysInRange(Constants.INITIAL_LAT - Constants.GEO_DIMENSION_FACTOR, Constants.INITIAL_LAT, Constants.INITIAL_LON, Constants.INITIAL_LON + (Constants.GEO_DIMENSION_FACTOR*2), 1);
	}

	/**
	 * A separate thread for initializing the backend so the server stays responsive. 
	 * @author samkortchmar
	 *
	 */
	public class BackendInitializer extends Thread {

		@Override
		public void run() {
			sendStatusMessage("Building backend");
			initKDTree();
			initAutoCorrectAndRoadLength();
			initBoundaries();
			List<Way> ways = getInitialWays();
			Util.debug("!! NUM INIT WAYS:", ways.size());
			_server.broadcastClientConnection(ways, MapFactory.getTrafficMap(), Constants.MINIMUM_LATITUDE, Constants.MAXIMUM_LATITUDE, Constants.MINIMUM_LONGITUDE, Constants.MAXIMUM_LONGITUDE);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			done = true;
			sendStatusMessage("Done");
		}
	}
}

