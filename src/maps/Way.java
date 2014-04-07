package maps;

import java.io.Serializable;

import graph.Edge;

public class Way implements Edge<Node>, Serializable {
	
	private static final long serialVersionUID = 1L;
	Node target;
	String name;
	Node start;
	String uniqueID;
	
	/** 
	 * We probably shouldn't use this too often.
	 * @param uniqueID
	 * @param r
	 */
	
	Way(String uniqueID, String name, Node start, Node end) {
		this.uniqueID = uniqueID;
		this.name = name;
		this.start = start;
		this.target = end;
	}
	
	/**
	 * @return the node representing the start point of this way
	 * (opposite the target)
	 */
	public Node getStart() {
		return start;
	}
	
	/**
	 * Gets the PathNode this WAY is pointing to - is its end coordinate.
	 * 
	 */
//	@Override
	@Override
	public Node getTarget() {
		return target;
	}

//	@Override
	@Override
	public String getName() {
		return uniqueID;
	}	

//	@Override
	@Override
	public double getWeight() {
		return start.distanceTo(target) * MapFactory.getTrafficValue(uniqueID);
	}
	
	@Override
	public String toString() {
		return uniqueID;
	}

}
