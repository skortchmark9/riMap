/**
 * 
 */
package maps;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author emc3
 *
 */
public class BoundaryFinder {
	double minLat, maxLat, minLon, maxLon;
	
	/**
	 * @throws IOException 
	 * 
	 */
	public BoundaryFinder() throws IOException {
		minLat = Double.MAX_VALUE;
		maxLat = Double.MIN_VALUE;
		minLon = Double.MAX_VALUE;
		maxLon = Double.MIN_VALUE;
		
		BufferedReader reader = new BufferedReader(new FileReader("./data/mapsfiles/nodes.tsv"));
		
		String line = reader.readLine();
		line = reader.readLine();
		while(line != null) {
			String[] fields = line.split("\t");
			Double testLat = Double.parseDouble(fields[1]);
			Double testLon = Double.parseDouble(fields[2]);
			
			if (testLat < minLat) {
				minLat = testLat;
			}
			
			if (testLat > maxLat) {
				maxLat = testLat;
			}
			
			if (testLon < minLon) {
				minLon = testLon;
			}
			
			if (testLon > minLon) {
				maxLon = testLon;
			}
			
			line = reader.readLine();
		}
	}
	
	public double[] getBoundaries() {
		return new double[]{minLat, maxLat, minLon, maxLon};
	}

	public static void main(String[] args) {
		try {
			BoundaryFinder b = new BoundaryFinder();
			double[] bounds = b.getBoundaries();
			System.out.println("Min Lat: " + bounds[0]);
			System.out.println("Max Lat: " + bounds[1]);
			System.out.println("Min Lon: " + bounds[2]);
			System.out.println("Max Lon: " + bounds[3]);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
