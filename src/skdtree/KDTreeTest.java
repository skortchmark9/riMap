package skdtree;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class KDTreeTest {

	DimObject sun =    new DimObject(0, "Sun", 0, 0, 0);
	DimObject star10 = new DimObject(10, "", 58.65441, 0.03711, -72.08957);
	DimObject star11 = new DimObject(11, "", 159.15237, 0.1036, 170.31215);


	@Test
	public void constructor() {
		//Does our KDTree constructor work correctly?
		List<DimObject> l = new LinkedList<>();
		l.add(sun);
		l.add(star10);
		l.add(star11);
		KDTree<DimObject> kd = new KDTree<>(l);
		assertTrue(kd.getNumDims() == 3);
	}

	@Test
	public void testSearchPoint() {
		//Does our distance formula work with searchpoints?
		assertTrue(sun.distance(new SearchPoint(1, 0, 0)) == 1);
	}

	@Test
	public void testNNSearch() {
		//Does our nearest neighbor search correctly return some neighbors?
		List<DimObject> l = new LinkedList<>();
		l.add(sun);
		l.add(star10);
		l.add(star11);
		KDTree<DimObject> kd = new KDTree<>(l);
		List<DimObject> results = kd.nearestNeighbors(3, 3.0, 3.0, -70.0);
		assertTrue(results.contains(sun) && results.contains(star10) && results.contains(star11));
	}

	@Test
	public void testRadius1() {
		//Tests radius neighbors againstParser.ParseCSV(args[0]) the exhaustive radius search
		List<Dimensionable> stars = null;
		//FIXME: TODO
		KDTree<Dimensionable> kd = new KDTree<>(stars);
		double radius = 3;
		List<Dimensionable> results = kd.radiusNeighbors(radius, 0, 0, 0);
		List<Dimensionable> exhaustiveResults = kd.exhaustiveRadiusNeighbors(radius, 0, 0, 0);
		SearchPoint s = new SearchPoint(0, 0, 0);
		Collections.sort(exhaustiveResults, new DimensionComparator(s));
		Collections.sort(results, new DimensionComparator(s));
		for(int i = 0; i < results.size(); i++) {
			assertTrue(results.contains(exhaustiveResults.get(i)));
		}
		assertTrue(kd.isClean()); //makes sure the tree has no marked nodes when we're done.
	}

	@Test
	public void testNN2() {
		List<Dimensionable> stars = null;
		//TODO: Fixme
		KDTree<Dimensionable> kd = new KDTree<>(stars);
		int neighbors = 3;
		List<Dimensionable> results = kd.nearestNeighbors(neighbors, 0.0, 0.0, 0.0);
		List<Dimensionable> exhaustiveResults = kd.nearestNeighbors(neighbors, 0.0, 0.0, 0.0);
		SearchPoint s = new SearchPoint(0, 0, 0);
		Collections.sort(exhaustiveResults, new DimensionComparator(s));
		Collections.sort(results, new DimensionComparator(s));
		for(int i = 0; i < results.size(); i++) {
			assertTrue(results.contains(exhaustiveResults.get(i)));
		}
		assertTrue(kd.isClean());
	}
}

