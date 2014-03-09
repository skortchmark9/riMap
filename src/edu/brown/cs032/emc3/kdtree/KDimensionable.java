package edu.brown.cs032.emc3.kdtree;

public interface KDimensionable {
	
	
	/**
	 * @return an array of the coordinates of a point 
	 * in a k-dimensional space.<br>
	 * For example, if the point is specified by 3-Dimensions,
	 * the array will be of size 3, and ordered as {x, y, z}. 
	 */
	public double[] getCoordinates();
	
	
	
	/**
	 * Compares two KDimensionable objects based on a specific axis.
	 * This method facilitates the comparing done when building 
	 * the KDTree. 
	 * @param kd -	the KDimensionable object to compare with
	 * @param axis-	the axis to compare on
	 * @return
	 * <ul>
	 * <li>	1 if this object has a larger coordinate compared to the parameter object
	 * 		on the specified axis.</li>
	 * <li> 0 if the coordinates are equal on the specified axis</li>
	 * <li> -1 if this object's coordinate is lesser than the specified object's coordinate</li>
	 * </ul>
	 */
	public int compareAxis(KDimensionable kd, int axis);
	
	
	
	/**
	 * @return
	 *  the number of dimensions that this
	 * 	KDimensional object represents.<br>
	 * 	e.g. for 2D, this method should return 2.
	 * 	for 3D, this method should return 3, and so on.
	 */
	public int getNumDimensions();
	
	
	
	/**
	 * Calculates the distance between this object and the parameter object.
	 * You should check to make sure the two objects have the same number of
	 * dimensions, or normalize the missing dimensions to 0.
	 *  
	 * @param kd - a KDimensional object to measure the distance to
	 * @return the distance between this object and the specified KDimensionable object
	 */
	public double distanceTo(KDimensionable kd);

}
