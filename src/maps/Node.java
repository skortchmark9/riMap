/**
 * 
 */
package maps;

import java.util.LinkedList;
import java.util.List;

import edu.brown.cs032.emc3.kdtree.KDimensionable;

/**
 * @author emc3 / skortchm
 */
public class Node implements KDimensionable {
	final String id;
	final List<String> wayIDs;
	final double[] coords;

	/**
	 * Default constructor<br>
	 * Checks the latitude and longitude to make sure they are 
	 * in range of acceptable values.
	 * 
	 */
	public Node(String id, double latitude, double longitude, List<String> ways) {
		Double lat = checkLatitude(latitude);
		Double lon = checkLongitude(longitude);
		coords = new double[]{lat, lon};
		//TODO make me work!
		this.id = id;
		this.wayIDs = ways;
	}
	
	public double getLat() {
		return coords[0];
	}
	
	public double getLon() {
		return coords[1];
	}
	
	public List<String> getWayIDs() {
		return wayIDs;
	}
	
	public String getID() {
		return id;
	}

	/**
	 * @return an array of doubles containing the coordinates of
	 * this location object in the format<br> 
	 * <strong>{latitude, longitude}</strong>
	 */
	@Override
	public double[] getCoordinates() {
		return coords;
	}

	
	@Override
	public int compareAxis(KDimensionable kd, int axis) {
		if (axis > 1 || axis < 0) {
			throw new IllegalArgumentException(String.format("Axis %d out of range: must be 1 or 0", axis));
		}
		double mine = coords[axis];
		double other = kd.getCoordinates()[axis];
		if (mine > other) {
			return 1;
		} else if (mine < other) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * @return 2
	 */
	@Override
	public int getNumDimensions() {
		return 2;
	}
	
	public double manhattanDistanceTo(KDimensionable kd) {
		double[] otherCoords = kd.getCoordinates();
		double latDiff = Math.abs(coords[0] - otherCoords[0]);
		double lonDiff = Math.abs(coords[1] - otherCoords[1]);
		
		return latDiff + lonDiff;
	}


	/**
	 * @return the Haversine Distance between two Locations 
	 */
	@Override
	public double distanceTo(KDimensionable kd) {
		// TODO: perhaps this should calculate the haversine Distance?
		//right now we are just returning Manhattan distance:
		return this.manhattanDistanceTo(kd);
		}
	
	
	/**
	 * Checks the given longitude to see if it is within range.
	 * between +/- 180
	 * @param lon - the longitude to check
	 * @return longitude if the longitude is within range.
	 * @throws IllegalArgumentException if lon is out of range
	 */
	private double checkLongitude(double lon) {
		if (lon < -180.0 || lon > 180.0) {
			throw new IllegalArgumentException(String.format("%f is out of range of earthly longitudes.", lon));
		}
		return lon;
	}
	
	/**
	 * Checks the given latitude to see if it is within range,
	 * between +/- 90
	 * @param lat -the latitude to check
	 * @return latitude if the latitude is within range.
	 * @throws IllegalArgumentException if lat is out of range
	 */
	private double checkLatitude(double lat) {
		if (lat < -90.0 || lat > 90.0) {
			throw new IllegalArgumentException(String.format("%f is out of range of earthly latitudes.", lat));
		}
		return lat;
	}
}
