package maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kdtree.KDimensionable;
import graph.Edge;
import graph.PathNode;
import maps.Node;
public class PathNodeWrapper implements PathNode<Node> {
	
	private final Node value;
	private double distance = Double.POSITIVE_INFINITY;
	private double aStarDistance = Double.POSITIVE_INFINITY;
	
	private Map<String, Edge<Node>> neighbors;
	private PathNode<Node> previous;
	
	public PathNodeWrapper(Node value) {
		this.value = value;
	}
	
	public PathNodeWrapper(KDimensionable value) {
		this.value = (Node) value;
	}


	@Override
	public Map<String, Edge<Node>> getNeighbors() {
			if (neighbors == null) {
				neighbors = new HashMap<>();
				List<String> wayIDs = getValue().getWayIDs();
				for(String wayID : wayIDs) {
					Way w = MapFactory.createWay(wayID);
					if (w != null) {
						neighbors.put(w.getTarget().getID(), w);
					}
				}
			}
			return neighbors;
	}

	@Override
	public Edge<Node> getNeighbor(String key) {
		// TODO Auto-generated method stub
		return neighbors.get(key);
	}

	@Override
	public Node getValue() {
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
		aStarDistance = d;
	}

	@Override
	public PathNode<Node> getPrevious() {
		return previous;
	}

	@Override
	public void setPrevious(PathNode<Node> n) {
		previous = n;
	}

	@Override
	public String getName() {
		return getValue().getID();
	}

	@Override
	public double getDistanceTo(PathNode<Node> n2) {
		return getValue().distanceTo(n2.getValue());
	}

	@Override
	public int compareTo(PathNode<Node> other) {
		return Double.compare(getAStarDistance(), other.getAStarDistance());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof PathNodeWrapper)) {
			return false;
		}
		PathNodeWrapper p = (PathNodeWrapper) o;
		return getValue().equals(p.getValue());
	}
	
	@Override
	public String toString() {
		return "PW: " + getValue().toString();
	}
}
