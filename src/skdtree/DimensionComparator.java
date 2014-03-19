package skdtree;

import java.util.Comparator;


/** Comparator for dimensionable objects. Supports two kinds of comparison:
 * 	1.) Comparison of distance across a single dimension
 *  2.) Comparison of distance to a given point.*/
public class DimensionComparator implements Comparator<Dimensionable> {
	
	//Integer because we check for null and we don't want random int values floating around.
	private static Integer comparedDim = null; 
	private Dimensionable compared = null;

	
	/**for comparison along a dimension.*/	
	DimensionComparator(int dim) {
		comparedDim = dim;
		compared = null;
	}
	
	/** for comparison of distance of two points to another dimensionable point.*/
	public DimensionComparator(Dimensionable compared) {
		comparedDim = null;
		this.compared = compared;
	}
	
	/** Switches between the two types of comparisons based on null values.*/
	@Override
	public int compare(Dimensionable o1, Dimensionable o2) {
		if (compared != null) {
			double cmp = o1.distance(compared) - o2.distance(compared);
			if (cmp < 0) {
				return -1;
			}
			else if (cmp > 0) {
				return 1;
			}
			else return 0;
		}
		else if (comparedDim != null) {
			double cmp = (o1.getDim(comparedDim) - o2.getDim(comparedDim));
			if (cmp < 0) {
				return -1;
			}
			else if (cmp > 0) {
				return 1;
			}
			else return 0;
		}
		else {
			System.out.println("Compare in DimensionComparator: Error, we shouldn't have gotten here.");
			return 0;
		}
	}
}
