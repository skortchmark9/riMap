package shared;

import kdtree.KDimensionable;

public class NeighborsRequest implements Request {
	
	private static final long serialVersionUID = 1L;
	private final int numNeighbors;
	private final KDimensionable location;
	
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
