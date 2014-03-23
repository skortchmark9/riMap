package maps;

import java.io.IOException;

import org.junit.Test;

import backend.Constants;
import backend.Resources;

public class MapFactoryTest {

	@Test
	public void test() throws IOException {
		String arg1 = "./data/mapsfiles/ways.tsv";
		String arg2	= "./data/mapsfiles/nodes.tsv";
		String arg3 = "./data/mapsfiles/index.tsv";
		new Resources(arg1, arg2, arg3);
		double minLat = Constants.INITIAL_LAT;
		double minLon = Constants.INITIAL_LON;
		double maxLat = minLat + Constants.GEO_DIMENSION_FACTOR;
		double maxLon = minLon + Constants.GEO_DIMENSION_FACTOR;
		System.out.println(MapFactory.getWaysInRange(minLat, maxLat, minLon, maxLon));
	}
}
