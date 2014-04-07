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
	
	/**
	 * Will almost always require only 1 numNeighbor, but we thought we'd keep
	 * it this way for flexibility.
	 * @param numNeighbors - the number of neighbors to be found.
	 * @param location - the location to search at.
	 */
	public NeighborsRequest(int numNeighbors, KDimensionable location) {
		this.numNeighbors = numNeighbors;
		this.location = location;
	}
	
	public int getNumNeighbors() {
		return numNeighbors;
	}
	
	public KDimensionable getLocation() {
		return location;
	}

	@Override
	public RequestType getType() {
		return RequestType.NEAREST_NEIGHBORS;
	}
}
