/**
 * 
 */
package maps;

import edu.brown.cs032.emc3.kdtree.KDimensionable;

/**
 * @author emc3 / skortchm
 */
public class Location implements KDimensionable {
	double lat, lon;

	/**
	 * Default constructor
	 * 
	 */
	public Location(double lat, double lon) {
		this.lat = checkLatitude(lat);
		this.lon = checkLongitude(lon);
		
	}


	@Override
	public double[] getCoordinates() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.brown.cs032.emc3.kdtree.KDimensionable#compareAxis(edu.brown.cs032.emc3.kdtree.KDimensionable, int)
	 */
	@Override
	public int compareAxis(KDimensionable kd, int axis) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.brown.cs032.emc3.kdtree.KDimensionable#getNumDimensions()
	 */
	@Override
	public int getNumDimensions() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.brown.cs032.emc3.kdtree.KDimensionable#distanceTo(edu.brown.cs032.emc3.kdtree.KDimensionable)
	 */
	@Override
	public double distanceTo(KDimensionable kd) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	/**
	 * 
	 * @param lon
	 * @return
	 */
	private double checkLongitude(double lon) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * 
	 * @param lat
	 * @return
	 */
	private double checkLatitude(double lat) {
		// TODO Auto-generated method stub
		return 0;
	}
}
