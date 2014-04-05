package shared;

import java.util.List;

import maps.Node;

public class NeighborsResponse implements Response {
	
	List<Node> neighbors;

	@Override
	public ResponseType getType() {
		return ResponseType.NEAREST_NEIGHBORS;
	}
	
	List<Node> getNeighbors() {
		return neighbors;
	}
}