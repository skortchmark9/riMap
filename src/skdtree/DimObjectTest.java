package skdtree;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests on the DimObject*/
public class DimObjectTest {
	
	DimObject sun = new DimObject(0, "Sun", 0, 0, 0);
	DimObject star10 = new DimObject(10, "", 58.65441, 0.03711, -72.08957);
	DimObject star11 = new DimObject(11, "", 159.15237, 0.1036, 170.31215);
	String starCSV = "./data/stardata.csv";
	String smallerStarCSV = "./data/smallerstardata.csv";
	String tinyStarCSV = "./data/tinydata.csv";
	String tinyStarCSV2 = "./data/tinydata2.csv";

	@Test
	public void distance1() {
		//Does our distance work correctly?
		assertTrue(Math.floor(sun.distance(star10)) == 8637);
	}
	@Test
	public void distance2() {
		//Does our distance work correctly?
		assertTrue(Math.floor(star11.distance(star10)) == 68858);
	}
	
	@Test
	public void equals1() {
		//Does our equals work correctly?
		DimObject equalStar = new DimObject(10, "", 58.65441, 0.03711, -72.08957);
		assertTrue(star10.equals(equalStar));
	}
}
