package maps;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kdtree.KDTree;
import autocorrect.RadixTree;
import backend.BinarySearchFile.SearchType;
import backend.Constants;
import backend.Resources;
import backend.Util;
import frontend.LoadingPane;
/**
 * @author emc3 / skortchm
 */
public class MapFactory {

	private static Map<String, Node> nodes = new HashMap<>(65000);
	private static Map<String, Way> ways = new HashMap<>(35000); //way IDs mapped to way objects
	//TODO init to a better number
	private static Map<String, List<String>> wayArray = new HashMap<>(1000); //SearchCode, WayIDs within search code bounds
	private static Map<String, Double> trafficMap = new HashMap<>();
	private static Map<String, Integer> roadLengthMap;
	/**
	 * Creates a way from the wayID. Attempts to find a cached way if one
	 * doesn't exist. If not, searches the waysFile.
	 * @param wayID - the wayID in question. 
	 * @return - a Way, or null if one cannot be created.
	 */
	public static Way createWay(String wayID) {
		Way possibleWay= ways.get(wayID);
		if (possibleWay != null) {
			return possibleWay;
		}
		String[] wayInfo = null;
		if (Resources.waysFile != null)
			wayInfo = Resources.waysFile.getXsByY(wayID, "name", "start", "end");
		if (wayInfo == null) {
			return null;
		}
		String name = wayInfo[0] == null ? "" : wayInfo[0];
		Node startLoc = createNode(wayInfo[1]);
		Node endLoc = createNode(wayInfo[2]);
		if (startLoc == null || endLoc == null) {
			return null;
		} else {
			return createWay(wayID, name, startLoc, endLoc);
		}
	}

	public static void setTrafficMap(Map<String, Double> newMap) {
		trafficMap = newMap;
	}
	
	public static void cacheBlock(String searchCode, List<String> wayIDs) {
		List<String> oldList = wayArray.get(searchCode);
		if (oldList == null || oldList.size() < wayIDs.size())
			wayArray.put(searchCode, wayIDs);
	}

	public static void cacheWay(Way way) {
		ways.put(way.uniqueID, way);
	}

	/**
	 * Creates a way from the following pieces of information.
	 * @param wayID - the ID of the way
	 * @param name - its name, if it has one
	 * @param start - its starting coordinate
	 * @param end - its ending point on the map.
	 * @return - a Way
	 */
	private static Way createWay(String wayID, String name, Node start, Node end) {
		Way resultWay = ways.get(wayID);
		if (resultWay == null) {
			resultWay = new Way(wayID, name, start, end);
			ways.put(wayID, resultWay);
		}
		return resultWay;
	}

	public static List<String> getIntersectingStreets(Node n) {
		List<String> wayIDs = n.getWayIDs();
		String s1 = "", s2 = "";
		
		if (wayIDs.size() >= 1) {
			Way way1 = MapFactory.createWay(wayIDs.get(0));
			if (way1 != null)
				s1 = way1.getName();
		}

		for(int i = 1; i < wayIDs.size(); i++) {
			Way way2 = MapFactory.createWay(wayIDs.get(i));
			if (way2 == null)
				continue;
			
			String possible = way2.getName();
			if (!possible.equals(s1)) {
				s2 = possible;
				break;
			}
		}
		LinkedList<String> result = new LinkedList<>();
		result.add(s1);
		result.add(s2);
		return result;
	}

	/**
	 * Creates a way if we don't have the actual nodes, but only their names
	 * @param wayID - the wayID we are searching for. 
	 * @param name - the name of the way in question
	 * @param start - the name of its start node
	 * @param end - the name of its end node
	 * @return - a way.
	 */
	private static Way createWay(String wayID, String name, String start, String end) {
		Way resultWay = ways.get(wayID);
		if (resultWay == null) {
			Node startNode = createNode(start);
			Node endNode = createNode(end);
			if (endNode != null && startNode != null) {
				resultWay = createWay(wayID, name, startNode, endNode);
			}
			if (resultWay != null) {
				ways.put(wayID, resultWay);
			} else {
				return null;
			}
		}
		return resultWay;
	}

	/**
	 * A method for the curious to see how many ways there are.
	 */
	public static void getNumWays() {
		if (Constants.DEBUG_MODE) {
			Util.out("Num Ways", ways.size());
		}
	}

	/**
	 * A method for the curious to see how many ways there are.
	 */
	public static void getNumNodes() {
		if (Constants.DEBUG_MODE) {
			Util.out("Num Nodes", nodes.size());
		}
	}

	public static Map<String, Double> getTrafficMap() {
		return trafficMap;
	}



	/*******	NODE CREATION WRAPPERS - THEY ARE ALL KINDA THE SAME ********/

	/**
	 * Method for creating nodes from the following pieces of information.
	 * @param nodeID - the ID of the node in question.
	 * @param latitude - the latitude of the node in question 
	 * @param longitude - the longitude of the node in question.
	 * @param ways
	 * @return
	 */
	private static Node createNode(String nodeID, String latitude, String longitude, String ways) {
		double lat = 0;
		double lon = 0;
		List<String> waysList;
		try {
			lat = Double.parseDouble(latitude);
			lon = Double.parseDouble(longitude);
			waysList = Arrays.asList(Constants.comma.split(ways));
			//If we can't parse its coordinates, we'll return null;
		} catch (NumberFormatException nfe) {
			return null;
		}
		try {
			Node node = new Node(nodeID, lat, lon, waysList);
			nodes.put(nodeID, node);
			return node;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Attempts to find the node from the hashmap of nodes, if not, gets it from file.
	 * @param nodeID - the ID of the node in question.
	 * @return
	 */
	static Node createNode(String nodeID) {
		Node possibleNode = nodes.get(nodeID);
		if (possibleNode != null) {
			return possibleNode;
		}
		//^ So that we don't need to make the system call if we don't have to.
		String[] nodeInfo = Resources.nodesFile.getXsByY(nodeID, "latitude", "longitude", "ways");
		if (nodeInfo == null) {
			return null;
		} else {
			return createNode(nodeID, nodeInfo[0], nodeInfo[1], nodeInfo[2]);
		}
	}

	/** Creates an intersection from two street names.
	 * Finds the names of the streets in the file, and then finds nodes that appear
	 * in both of those streets' ways. 
	 * @param streetName1
	 * @param streetName2
	 * @return - the Node that is their intersection.
	 */
	public static Node createIntersection(String streetName1, String streetName2) {
		if (streetName1.equals("") || streetName2.equals("")) {
			return null;
		}
		List<List<String>> street1nodeLists = Resources.indexFile.searchMultiples(streetName1, "nodes");
		Set<String> street1nodeIDs = new HashSet<>();
		for(List<String> nodeList : street1nodeLists) {
			for(String nodes : nodeList) {
				street1nodeIDs.addAll(Arrays.asList(Constants.comma.split(nodes)));				
			}
		}
		List<List<String>> street2nodeLists = Resources.indexFile.searchMultiples(streetName2, "nodes");
		if (street2nodeLists == null) {
			Util.err("ERROR: StreetName2 has no node lists");
			return null;
		}
		for(List<String> nodeList : street2nodeLists) {
			for(String nodes : nodeList) {
				for(String node : Constants.comma.split(nodes)) {
					if (street1nodeIDs.contains(node)) {

						Node result =  createNode(node);
						if (Constants.DEBUG_MODE) {
							Util.out(result);
						}
						return result;
					}
				}
			}
		}
		return null;
	}


	/** Attempts to create a KDTree from the nodes file we have. */
	public static KDTree<Node> createKDTree() throws IOException {
		long start = 0; //XXX: FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			Util.out("Reading node data from file to List (System Calls):");
			start = Util.resetClock();
		}
		List<List<String>> nodes = Resources.nodesFile.readChunks("id", "latitude", "longitude", "ways");
		if (Constants.DEBUG_MODE) {
			Util.out("Populated List with node data.", "(Elapsed:", Util.timeSince(start) + ")");
			Util.memLog();
			Util.out("Creating Node objects from node data:");
			start = Util.resetClock();
		}
		List<Node> nodeList = new LinkedList<>();
		//Iterators here because we are parsing a lot of data and we want to make the best use of our
		//underlying data structure. The wrapper list is a LinkedList and the underlying one is an arrayList.
		Iterator<List<String>> outerListIterator = nodes.iterator();
		while (outerListIterator.hasNext()) {
			List<String> nodeInfo = outerListIterator.next();
			if (nodeInfo.size() == 4) {
				Node node = createNode(nodeInfo.get(0), nodeInfo.get(1), nodeInfo.get(2), nodeInfo.get(3));
				if (node != null) {
					nodeList.add(node);
				}
			}
		}

		if (Constants.DEBUG_MODE) {
			Util.out("Finished Nodes creation", "(Elapsed:", Util.timeSince(start) + ")");
			Util.memLog();
			Util.out("Creating KDTree from Nodes List:");
			Util.resetClock();
		}
		return new KDTree<Node>(nodeList);
	}
	
	public static RadixTree createRadixTreeAndInitRoadLengths() throws IOException {
		List<List<String>> names;
		names = Resources.indexFile.readChunks("name", "nodes");
		roadLengthMap = new HashMap<String, Integer>(29223);
		RadixTree rt = new RadixTree();
		for(List<String> nameAndNodes : names) {
			String lastWord = "";
			String nameField = nameAndNodes.get(0);
			for (String word : Constants.spaces.split(nameField)) {
				if (word.length() > 0) {
					word = word.toLowerCase();
					rt.insert(word, lastWord);
					lastWord = word;
				}
			}
			String nodes = nameAndNodes.get(1);
			roadLengthMap.put(nameField, nodes.length() / 23);
		}
		Util.out("RLM: ", roadLengthMap.size());
		return rt;
	}

	public static RadixTree createRadixTree() throws IOException {
		return createRadixTree(null);
	}

	public static RadixTree createRadixTree(LoadingPane l) throws IOException {
		long start = 0; //XXX: FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			Util.out("Reading index file for names:");
			Util.memLog();
			start = Util.resetClock();
		} else if (l != null) {
			l.updateProgress("Reading index file for names", 2);
		}

		List<List<String>> names = Resources.indexFile.readChunks("name");
		if (l!= null) {
			l.updateProgress("Done reading words to list.", 10);
			l.updateProgress("Creating tree from list.", 11);
		}

		if (Constants.DEBUG_MODE) {
			Util.out("Done reading names to list.", "(Elapsed:", Util.timeSince(start) + ")");
			Util.out("Creating Tree from List:");
			start = Util.resetClock();
		}

		RadixTree rt = new RadixTree();
		for(List<String> nameList : names) {
			String lastWord = "";
			String nameField = nameList.get(0);
			for (String word : Constants.spaces.split(nameField)) {
				if (word.length() > 0) {
					word = word.toLowerCase();
					rt.insert(word, lastWord);
					lastWord = word;
				}
			}
		}
		if ( l!= null) {
			l.updateProgress("Done creating Radix Tree", 20);
		}

		Util.debug("Done inserting names from list to Radix Tree.", "(Elapsed:", Util.timeSince(start) + ")");
		return rt;
	}

	/**
	 * Gets all the ways within a given lat-lng range. Threaded, to allow 
	 * quick access and loading of ways while keeping the GUI responding.
	 * @param minLat - the minLat of the block
	 * @param maxLat - the maxLat of the block
	 * @param minLon - the minLon of the block
	 * @param maxLon - the maxLon of the block
	 * @return - 
	 */
	public static synchronized List<Way> getWaysInRange(double minLat, double maxLat, double minLon, double maxLon, double zoom) {
		Util.debug("Looking for Ways in Range");
		Util.resetClock();
		if (wayArray == null) {
			int size = (int) Math.ceil((Constants.MAXIMUM_LATITUDE - Constants.MINIMUM_LATITUDE) * 100);
			wayArray = new HashMap<>(size);
		}

		List<Way> ways = new LinkedList<>();
			if (wayArray == null) {
				int size = (int) Math.ceil((Constants.MAXIMUM_LATITUDE - Constants.MINIMUM_LATITUDE) * 100);
				wayArray = new HashMap<>(size);
			}

			for(double i = minLat; i <= maxLat + 0.01; i+=0.01) {
				for(double j = maxLon; j >= minLon - 0.01; j-=0.01) {
					ways.addAll(getWaysSquare(i, j, zoom));
				}
			}
		return ways;
	}
	
	public static List<Way> getLocalWaysInRange(double minLat, double maxLat, double minLon, double maxLon, double zoom) {
		List<Way> ways = new LinkedList<>();
		for(double i = minLat; i <= maxLat + 0.01; i+=0.01) {
			for(double j = maxLon; j >= minLon - 0.01; j-=0.01) {
				ways.addAll(getLocalWays(i, j, zoom));
			}
		}
		return ways;
	}
	
	static List<Way> getLocalWays(double lat, double lon, double zoom) {
		List<Way> wayList = new LinkedList<>();
		Util.out("WAYARRAY SIZE: ", wayArray.size());
		String searchCode = "/w/" + Util.getFirst4Digits(lat) + "." + Util.getFirst4Digits(lon); //lAT/LNG
		List<String> wayIDsInRange = wayArray.get(searchCode);
		if (wayIDsInRange != null) {
			for(String wayID : wayIDsInRange) {
				Way way = ways.get(wayID);
				if (longRoad(way, zoom)) 
					wayList.add(way);
			}
		}
		return wayList;
	}

	static List<Way> getWaysSquare(double lat, double lon) {
		return getWaysSquare(lat, lon, 1);
	}
	
	static boolean longRoad(Way way, double zoom) {
		if (way == null) {
			return false;
		} else if (zoom >= 1) {
			return true;
		} else {
			Integer numNodes = null;
			if (roadLengthMap != null) {
				numNodes = roadLengthMap.get(way.getName());
			}
			if (numNodes == null) {
				return true;
			}
			return numNodes * zoom > 1;
		}
	}
		
	static List<Way> getWaysSquare(double lat, double lon, double zoom) {
		List<Way> ways = new LinkedList<>();
		String searchCode = "/w/" + Util.getFirst4Digits(lat) + "." + Util.getFirst4Digits(lon); //lAT/LNG
		List<String> wayIDsInRange = wayArray.get(searchCode);
		Way possibleWay;
		if (wayIDsInRange !=null) {
			for(String wayID : wayIDsInRange) {
				possibleWay = createWay(wayID);
				if (longRoad(possibleWay, zoom))
					ways.add(possibleWay);
			}
		} else {				
			List<List<String>> chunk = new LinkedList<>();
			chunk = Resources.waysFile.searchMultiples(searchCode, SearchType.WILDCARD, "id", "name", "start", "end");
			List<String> wayIDsInBlock = new LinkedList<>();
			for (List<String> wayInfo : chunk) {
				if (wayInfo != null && !wayInfo.isEmpty()) {
					possibleWay = createWay(wayInfo.get(0), wayInfo.get(1), wayInfo.get(2), wayInfo.get(3));
					if (longRoad(possibleWay, zoom)) {
						ways.add(possibleWay);
					}
					wayIDsInBlock.add(wayInfo.get(0));
				}
			}
			wayArray.put(searchCode, wayIDsInBlock);
		}
		return ways;
	}

	/**
	 *
	 * @param wayID
	 * @return
	 */
	public static double getTrafficValue(String wayID) {
		Way way = createWay(wayID);
		String streetName;
		if (way != null) {
			streetName = way.getName().toLowerCase();
			Double val;

			synchronized (trafficMap) {
				val = trafficMap.get(streetName);
			}
			return (val == null) ? 1.0 : val;
		}

		Util.debug("Could not find way: " + wayID);

		return 1.0;
	}

	public static void putTrafficValue(String street, Double val) {
		synchronized (trafficMap) {
			trafficMap.put(street, val);
		}
	}
}
