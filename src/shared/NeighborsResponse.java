package shared;

import java.util.List;

import maps.Node;

public class NeighborsResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	private List<Node> _neighbors;
	
	public NeighborsResponse(List<Node> neighbors) {
		_neighbors = neighbors;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.NEAREST_NEIGHBORS;
	}

	public List<Node> getNeighbors() {
		return _neighbors;
	}
}