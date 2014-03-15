package maps;

import graph.Edge;
import backend.Resources;

public class Way implements Edge<PathNodeWrapper> {
	
	PathNodeWrapper target;
	String name;
	Node start;
	String uniqueID;
	
	/** 
	 * We probably shouldn't use this too often.
	 * @param uniqueID
	 * @param r
	 */
	
	Way(String uniqueID, String name, Node start, PathNodeWrapper end) {
		this.uniqueID = uniqueID;
		this.name = name;
		this.start = start;
		this.target = end;
	}

	/**
	 * Gets the PathNode this WAY is pointing to - is its end coordinate.
	 * 
	 */
	@Override
	public PathNodeWrapper getTarget() {
		return target;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getWeight() {
		return start.distanceTo(target.getValue());
	}

}
