package shared;

import java.util.List;

import maps.Node;

/**
 * The response from the server in the event of a NeighborsRequest
 * @author samkortchmar
 */
public class NeighborsResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	private final Node _neighbor;
	private String _street1, _street2 = "";
	private final boolean _isSource; //This boolean tells the mapPane if the
	//KDimensionable we searched for was supposed to be the source or the dest.
	
	public NeighborsResponse(Node neighbor, boolean isSource) {
		_neighbor = neighbor;
		_isSource = isSource;
	}
	
	public NeighborsResponse(Node neighbor, boolean isSource, String street1, String street2) {
		this(neighbor, isSource);
		_street1 = street1;
		_street2 = street2;
	}


	@Override
	public ResponseType getType() {
		return ResponseType.NEAREST_NEIGHBORS;
	}

	public Node getNeighbor() {
		return _neighbor;
	}
	
	public String getStreet1() {
		return _street1;
	}
	
	public String getStreet2() {
		return _street2;
	}
	
	public boolean isSource() {
		return _isSource;
	}
	
	@Override
	public String toString() {
		return String.format("NeighborsResponse _neighbor = %s, isSource = %s street 1: %s street 2: %s", _neighbor, _isSource, _street1, _street2);
	}

}