package maps;
import java.util.HashMap;
import java.util.Map;

import graph.Edge;
import graph.Node;
public class PathNode implements Node<Location> {
	
	private final Location value;
	private double distance;
	private double aStarDistance;
	//NOTE: Eclipse gives compile errors when the map has PathNodes.
	private Map<Integer, Edge<? extends Node<Location>>> neighbors;
	private PathNode previous;
	
	PathNode(Location value) {
		this.value = value;
	}

	

	@Override
	public Map<Integer, Edge<? extends Node<Location>>> getNeighbors() {
			neighbors = new HashMap<>();
			neighbors.put(1, new Way(new PathNode(new Location(1, 2))));
			//TODO: get neighbors from file from the value from
			return neighbors;
	}

	@Override
	public Edge<? extends Node<Location>> getNeighbor(int hashcode) {
		// TODO Auto-generated method stub
		return neighbors.get(1);
	}

	@Override
	public Location getValue() {
		return value;
	}

	@Override
	public double getDistance() {
		return distance;
	}

	@Override
	public void setDistance(Double d) {
		distance = d;
	}

	@Override
	public double getAStarDistance() {
		return aStarDistance;
	}

	@Override
	public void setAStarDistance(Double d) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Node<Location> getPrevious() {
		// TODO Auto-generated method stub
		return previous;
	}

	@Override
	public void setPrevious(Node<Location> n) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDistanceTo(Node<Location> n2) {
		return getValue().distanceTo(n2.getValue());
	}

	@Override
	public int compareTo(Node<Location> other) {
		return Double.compare(getAStarDistance(), other.getAStarDistance());
	}
}
