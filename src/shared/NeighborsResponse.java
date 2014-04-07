package shared;

import java.util.List;

import maps.Node;

/**
 * The response from the server in the event of a NeighborsRequest
 * @author samkortchmar
 */
public class NeighborsResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	private final List<Node> _neighbors;
	private final boolean _isSource; //This boolean tells the mapPane if the
	//KDimensionable we searched for was supposed to be the source or the dest.
	
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
	
	@Override
	public String toString() {
		return String.format("NeighborsResponse _neighbors = %s, isSource = %s", _neighbors, _isSource);
	}

}