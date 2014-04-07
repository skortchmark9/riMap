package shared;

import kdtree.KDimensionable;

/**
 * A request for nearest neighbors to be sent by the client to the server.
 * @author samkortchmar
 */
public class NeighborsRequest implements Request {
	
	private static final long serialVersionUID = 1L;
	private final int numNeighbors;
	private final KDimensionable location;
	private final boolean _isSource;
	
	public NeighborsRequest(int numNeighbors, KDimensionable location, boolean isSource) {
		this.numNeighbors = numNeighbors;
		this.location = location;
		_isSource = isSource;
	}
	
	public int getNumNeighbors() {
		return numNeighbors;
	}
	
	public KDimensionable getLocation() {
		return location;
	}
	
	public boolean isSource() {
		return _isSource;
	}

	@Override
	public RequestType getType() {
		return RequestType.NEAREST_NEIGHBORS;
	}
}
