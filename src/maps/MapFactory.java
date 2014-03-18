package maps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import autocorrect.RadixTree;
import backend.Constants;
import backend.Resources;
import backend.Util;
import edu.brown.cs032.emc3.kdtree.KDStub;
import edu.brown.cs032.emc3.kdtree.KDTree;
import edu.brown.cs032.emc3.kdtree.KDimensionable;

/**
 * 
 * @author emc3 / skortchm
 *
 */
public class MapFactory {
	
	private static HashMap<String, Node> nodes = new HashMap<>();
	private static HashMap<String, Way> ways = new HashMap<>();
	
	public static Way createWay(String wayID) {
		Way possibleWay= ways.get(wayID);
		if (possibleWay != null) {
			return possibleWay;
		}
		String[] wayInfo = Resources.waysFile.getXsByY(wayID, "name", "start", "end");
		if (wayInfo == null) {
			return null;
		}
		Node startLoc = createNode(wayInfo[1]);
		Node endLoc = createNode(wayInfo[2]);
		if (startLoc == null || endLoc == null) {
			return null;
		} else {
			return createWay(wayID, wayInfo[0], startLoc, new PathNodeWrapper(endLoc));
		}
	}
	
	private static Way createWay(String wayID, String name, Node start, PathNodeWrapper end) {
		Way resultWay = new Way(wayID, name, start, end);
		ways.put(wayID, resultWay);
		return resultWay;
	}
	/*
	public List<Way> getWaysInRange(double minLat, double maxLat, double minLon, double maxLon) {
		if (Constants.DEBUG_MODE) {
			Util.out("Looking for Ways in Range");
			Util.resetClock();
		}
		Util.getFirst4Digits(minLat);
	}*/

	
	

	
	//TODO : Potentially we could store the nodes created somewhere so that if
	//we encounter them again we can just call them from a HashMap or something.
	private static Node createNode(String nodeID, String latitude, String longitude, String ways) {
		double lat = 0;
		double lon = 0;
		List<String> waysList;
		try {
			lat = Double.parseDouble(latitude);
			lon = Double.parseDouble(longitude);
			waysList = Arrays.asList(Constants.comma.split(ways));
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
	
	public static Node createIntersection(String streetName1, String streetName2) {
		List<List<String>> street1nodeLists = Resources.indexFile.searchMultiples(streetName1, "nodes");
		Set<String> street1nodeIDs = new HashSet<>();
		for(List<String> nodeList : street1nodeLists) {
			for(String nodes : nodeList) {
				street1nodeIDs.addAll(Arrays.asList(Constants.comma.split(nodes)));				
			}
		}
		List<List<String>> street2nodeLists = Resources.indexFile.searchMultiples(streetName2, "nodes");
		if (street2nodeLists == null) {
			System.out.println("ERROR: StreetName2 has no node lists");
		}
		for(List<String> nodeList : street2nodeLists) {
			for(String nodes : nodeList) {
				for(String node : Constants.comma.split(nodes)) {
					if (street1nodeIDs.contains(node)) {
						//TODO: What if multiples?
						return createNode(node);
					}
				}
			}
		}
		return null;
	}
	
	public static KDTree<Node> createKDTree() {
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
	
	public static RadixTree createRadixTree() {
		long start = 0; //XXX: FOR DEBUGGING
		if (Constants.DEBUG_MODE) {
			Util.out("Reading index file for names:");
			Util.memLog();
			start = Util.resetClock();
		}
		
		List<List<String>> names = Resources.indexFile.readChunks("name");
		
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
		
		if (Constants.DEBUG_MODE) {
			Util.out("Done inserting names from list to Radix Tree.", "(Elapsed:", Util.timeSince(start) + ")");
		}
		
		return rt;
	}
	
}
