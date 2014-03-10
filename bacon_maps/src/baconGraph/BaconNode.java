package baconGraph;

import graph.Edge;
import graph.Node;

import java.util.HashMap;
import java.util.Map;

/** BaconNode is the main node of the graph. I wanted to make it generic but ran out
 * of time. You can see my attempts in the graph package. However, only the edge and
 * node interfaces from that package are used. */
class BaconNode implements Node<Actor>, Comparable<BaconNode> {
	Actor value;
	Map<Integer, Edge<Actor>> neighbors;
	double minDistance = Double.POSITIVE_INFINITY;
	BaconNode previous;

	BaconNode(Actor value) {
		this.value = value;
		neighbors = new HashMap<Integer, Edge<Actor>>();
	}

	Edge<Actor> getNeighbor(int hashCode) {
		return neighbors.get(hashCode);
	}

	@Override
	public Map<Integer, Edge<Actor>> getNeighbors() {
		return neighbors;
	}

	@Override
	public double getDistance() {
		return minDistance;
	}

	@Override
	public void setDistance(Double d) {
		minDistance = d;
	}

	@Override
	public BaconNode getPrevious() {
		return previous;
	}

	@Override
	public void setPrevious(Node<Actor> n) {
		previous = (BaconNode) n;
	}
	@Override
	public Actor getValue() {
		return value;
	}

	/** This is important for the priority queue we maintain in djikstra.
	 *  We want to make sure we get the nodes with a small minimum distance
	 *  first. 
	 *  @param b - another bacon node to compare against*/
	@Override
	public int compareTo(BaconNode b) {
		return Double.compare(this.getDistance(), b.getDistance());
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof BaconNode)) return false;
		BaconNode s = (BaconNode) o;
		return (this.value.equals(s.value));
	}

	/**
	 * We want the haschode of nodes to be the same as that of the 
	 * actors so we can easily consult hashmaps. 
	 */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	/** An important method which updates the adjacent edges of the current node.
	 *  If the edge has a larger weight, it has a smaller reciprocal, and therefore
	 *  will replace the existing edge. If there is no existing one, we add it anyway.
	 *  @param e - the edge that we are attempting to add.*/
	@Override
	public void addNeighbor(Edge<Actor> e) {
		Edge<Actor> e2 = neighbors.get(e.getTarget().hashCode());
		if (e2 == null || e2.getWeight() < e.getWeight()) {
			neighbors.put(e.getTarget().hashCode(), e);		
		}
	}

	@Override
	public String toString() {
		return "NODE - Value: " + this.value + " Neighbors: "
				+ neighbors.size() + " Previous node: " + 
				((previous != null) ? "EXISTS" : "NULL") + "Distance: " + getDistance();
	}

	@Override
	public String getName() {
		return value.name;
	}
}
