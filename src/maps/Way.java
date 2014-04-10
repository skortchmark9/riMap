package maps;

import java.io.Serializable;

import graph.Edge;

public class Way implements Edge<Node>, Serializable {
	
	private static final long serialVersionUID = 1L;
	Node target;
	String name = "";
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
	@Override
	public Node getTarget() {
		return target;
	}

	@Override
	public String getUniqueID() {
		return uniqueID;
	}
	
	public String getName() {
		return name;
	}
	
	public double getDistanceInKM() {
		  double theDistance = (Math.sin(Math.toRadians(start.getCoordinates()[0])) *
		            Math.sin(Math.toRadians(target.getCoordinates()[0])) +
		            Math.cos(Math.toRadians(start.getCoordinates()[0])) *
		            Math.cos(Math.toRadians(target.getCoordinates()[0])) *
		            Math.cos(Math.toRadians(start.getCoordinates()[1] - target.getCoordinates()[1])));
		    return new Double((Math.toDegrees(Math.acos(theDistance))) * 69.09 * 1.6093);
	}
	
	public double getTime(int speed) {
		return getDistanceInKM() * getTraffic() / speed;
	}

	@Override
	public double getWeight() {
		return start.distanceTo(target) * getTraffic();
	}
	
	public double getTraffic() {
		return MapFactory.getTrafficValue(uniqueID);
	}
	
	@Override
	public String toString() {
		return uniqueID;
	}

}
