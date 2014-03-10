/**
 * 
 */
package edu.brown.cs032.emc3.kdtree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import maps.Location;

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
		Location l1 = new Location(0,0);
		Location l2 = new Location(1,2);
		Location l3 = new Location(2,3);
		Location l4 = new Location(3,4);
		Location[] locations = new Location[] {
			l1,
			l2,
			l3,
			l4
		};
		List<Location> list = Arrays.asList(locations);
		KDTree<Location> tree = new KDTree<>(list);
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
		Location l1 = new Location(0,0);
		Location l2 = new Location(1,2);
		Location l3 = new Location(2,3);
		Location l4 = new Location(3,4);
		ArrayList<Location> l = new ArrayList<>();
		l.add(l1);
		l.add(l2);
		l.add(l3);
		l.add(l4);
		
		KDTree<Location> tree = new KDTree<>(l);
		List<Location> results = tree.getNearestNeighbors(2, l1);
		
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
		Location l1 = new Location(0,0);
		Location l2 = new Location(1,2);
		Location l3 = new Location(2,3);
		Location l4 = new Location(3,4);
		ArrayList<Location> l = new ArrayList<>();
		l.add(l1);
		l.add(l2);
		l.add(l3);
		l.add(l4);
		KDTree<Location> tree = new KDTree<>(l);
		List<Location> results = tree.getObjectsWithinRadius(4, l1);
		
		assertTrue(results.size() == 1);
		assertFalse(results.contains(l1));
		assertTrue(results.contains(l2));
		assertFalse(results.contains(l3));
		assertFalse(results.contains(l4));
	}
	
	
	
}
