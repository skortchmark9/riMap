/**
 * 
 */
package kdtree;

import java.util.Comparator;

/**
 * KDimensionComparator<br>
 * This class represents the comparator used to sort the list of 
 * coordinates that the KDTree uses to build itself. Since the KDTree
 * must be balanced to optimize its runtime, it must always choose
 * the median based on which dimension is used at the given level.
 * This class allows the KDTree to call Collections.sort on the list of
 * incoming KDimensionable objects, as to easily grab the median from
 * the list.
 * 
 * @author emc3
 *
 */
public class KDimensionComparator implements Comparator<KDimensionable> {
	
	private int _axis; //the dimension to compare
	
	/**
	 * Default constructor. Requires that the caller
	 * specifies an axis which the KDimensionable objects
	 * will be compared by. For example, to compare by
	 * x-values, axis would be the number 0.
	 * 
	 * @param axis -	the coordinate to compare the KDimensionable
	 * 					objects by.
	 */
	public KDimensionComparator(int axis) {
		if (axis >= 0)
			_axis = axis;
		else
			System.err.println("ERROR: KDimensionComparator: axis for compare must be non-negative.");
	}
	
	
	/**
	 * Compares the two given objects based on the
	 * axis specified to this object during construction.
	 */
	@Override
	public int compare(KDimensionable o1, KDimensionable o2) {
		return o1.compareAxis(o2, _axis);
	}

}
