/**
 * 
 */
package edu.brown.cs032.emc3.kdtree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author emc3
 *
 */
public class KDTreeTest {
	
	/**
	 * Test that the new constructing method actually constructs a tree.
	 * Disregards order, at this point.
	 */
	@Test
	public void simpleTest() {
		KDStub l1 = new KDStub(0,0);
		KDStub l2 = new KDStub(1,2);
		KDStub l3 = new KDStub(2,3);
		KDStub l4 = new KDStub(3,4);
		KDStub[] locations = new KDStub[] {
			l1,
			l2,
			l3,
			l4
		};
		List<KDStub> list = Arrays.asList(locations);
		KDTree<KDStub> tree = new KDTree<>(list);
		assertTrue(tree.contains(l1));
		assertTrue(tree.contains(l2));
		assertTrue(tree.contains(l3));
		assertTrue(tree.contains(l4));
	}
	
	
	/**
	 * A very simple test to ensure that the neighbor test is working.
	 */
	@Test
	public void simpleNeighborTest() {
		KDStub l1 = new KDStub(0,0);
		KDStub l2 = new KDStub(1,2);
		KDStub l3 = new KDStub(2,3);
		KDStub l4 = new KDStub(3,4);
		ArrayList<KDStub> l = new ArrayList<>();
		l.add(l1);
		l.add(l2);
		l.add(l3);
		l.add(l4);
		
		KDTree<KDStub> tree = new KDTree<>(l);
		List<KDStub> results = tree.getNearestNeighbors(2, l1);
		
		assertTrue(results.size() == 2);
		assertFalse(results.contains(l1));
		assertTrue(results.contains(l2));
		assertTrue(results.contains(l3));
		assertFalse(results.contains(l4));
	}
	
	
	/**
	 * A very simple test to ensure that the radius test is working.
	 */
	@Test
	public void simpleRadiusTest() {
		KDStub l1 = new KDStub(0,0);
		KDStub l2 = new KDStub(1,2);
		KDStub l3 = new KDStub(2,3);
		KDStub l4 = new KDStub(3,4);
		ArrayList<KDStub> l = new ArrayList<>();
		l.add(l1);
		l.add(l2);
		l.add(l3);
		l.add(l4);
		KDTree<KDStub> tree = new KDTree<>(l);
		int radius = 3;
		List<KDStub> results = tree.getObjectsWithinRadius(Math.pow(radius, 2), l1);
		assertTrue(results.size() == 1);
		assertFalse(results.contains(l1));
		assertTrue(results.contains(l2));
		assertFalse(results.contains(l3));
		assertFalse(results.contains(l4));
	}
	
	
	
}
