package shared;

import java.util.List;

import maps.Node;

public class NeighborsResponse implements Response {
	
	private final List<Node> _neighbors;
	private final boolean _isSource;
	
	public NeighborsResponse(List<Node> neighbors, boolean isSource) {
		_neighbors = neighbors;
		_isSource = isSource;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.NEAREST_NEIGHBORS;
	}

	public List<Node> getNeighbors() {
		return _neighbors;
	}
	
	public boolean isSource() {
		return _isSource;
	}
}