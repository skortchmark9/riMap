package skdtree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.MinMaxPriorityQueue;

public class DimensionComparatorTest {
	
	DimObject sun =    new DimObject(0, "Sun", 0, 0, 0);
	DimObject star10 = new DimObject(10, "", 58.65441, 0.03711, -72.08957);
	DimObject star11 = new DimObject(11, "", 159.15237, 0.1036, 170.31215);

	
	/** Does the dimension comparator correctly sort across single dimensions?*/
	@Test
	public void SortTest1() {
		DimObject[] a = {sun, star10, star11};
		DimObject[] results = {sun, star10, star11};
		Arrays.sort(a, new DimensionComparator(1)); //Sorts by y coordinate
		assertTrue(Arrays.equals(a, results));
	}
	
	/** Does the dimension comparator correctly sort across single dimensions?*/
	@Test
	public void SortTest2() {
		DimObject[] a = {sun, star10, star11};
		DimObject[] results = {star10, sun, star11};
		Arrays.sort(a, new DimensionComparator(2)); //Sorts by z coordinate
		assertTrue(Arrays.equals(a, results));
	}
	
	/** Makes sure we can also sort by distance to a given point. */
	@Test
	public void SortTest3() { 
		DimObject[] a = {sun, star10, star11};
		DimObject[] results = {star10, sun, star11};
		Arrays.sort(a, new DimensionComparator(new DimObject(1, "", 23.0, 23.0, -70.0))); //Sorts by distance to new star.
		assertTrue(Arrays.equals(a, results));
	}
	
	/** Creates and checks the functionality of a bounded min heap from the guava libraries. */
	@Test
	public void boundedMinHeapTest() {
		//Creates a bounded priority queue
		MinMaxPriorityQueue.Builder<Dimensionable> b = 
				MinMaxPriorityQueue.orderedBy(new DimensionComparator(star10)).maximumSize(2);
		//based on distance between star and star10. Maximum number of elements is 2.
		MinMaxPriorityQueue<Dimensionable>  mpq = b.create(); 
		assertTrue(mpq.isEmpty());
		mpq.add(star11);
		mpq.add(sun);
		assertFalse(mpq.isEmpty());
		assertTrue(mpq.contains(star11));
		assertTrue(mpq.contains(sun));
		//Adds star 10 and therefore removes star11.
		mpq.add(star10);
		assertTrue(mpq.contains(sun));
		assertFalse(mpq.contains(star11));
	}

}
