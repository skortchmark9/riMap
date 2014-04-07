package shared;

import java.util.List;

import maps.Node;

/**
 * The response from the server in the event of a NeighborsRequest
 * @author samkortchmar
 *
 */
public class NeighborsResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	private List<Node> _neighbors; //the list of neighbors to be returned.
	
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