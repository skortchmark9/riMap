package shared;

import java.util.List;

import maps.Node;

public class NeighborsResponse implements Response {
	
	List<Node> neighbors;
	
	public NeighborsResponse() {
		// TODO fill out this constructor.
	}

	@Override
	public ResponseType getType() {
		return ResponseType.NEAREST_NEIGHBORS;
	}
	
	List<Node> getNeighbors() {
		return neighbors;
	}
}