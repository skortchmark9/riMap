package shared;

import kdtree.KDimensionable;

public class NeighborsRequest implements Request {
	
	public final int numNeighbors;
	public final KDimensionable location;
	
	NeighborsRequest(int numNeighbors, KDimensionable location) {
		this.numNeighbors = numNeighbors;
		this.location = location;
	}

	@Override
	public RequestType getType() {
		return RequestType.NEAREST_NEIGHBORS;
	}
}
