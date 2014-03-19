package skdtree;

import java.util.Arrays;

/** Generic Dimensionable object - here it represents a star.*/
public class DimObject implements Dimensionable {
	
	private String name; //Colloquial name of object. Default is "";
	private int id;   //id number of object
	private double[] domain = new double[3]; //Dimensions of the object //0 = x, 1 = y, 2 = z, etc.
	
	public DimObject(int id,  String name, double ... ds) {
		this.name = name;
		this.id = id;
		domain = ds;
	}
	
	public String getName() {
		return name;
	}
		
	public int getID() {
		return id;
	}
	
	public int numDimensions() {
		return domain.length;
	}
	
	public double getDim(int i) { 
		return domain[i];
	}
	
	
	public double distance(Dimensionable d) {
		// Distance squared. //
		if (d.numDimensions() != numDimensions()) {
			System.out.println("ERROR: objects have different dimensionality: " + d);
			return 0;
		}
		double distance = 0;
		for(int i = 0; i < numDimensions(); i++) {
			distance += Math.pow(getDim(i) - d.getDim(i), 2);
		}
		return distance;
	}
	
	
	@Override
	public boolean equals(Object o) {
		/** Pretty hard equals, a little much for testing probably */
		if (o == this) 
			return true;
		if (!(o instanceof DimObject)) 
			return false;
		DimObject s = (DimObject) o;
		boolean sameDims = true;
		for(int i = 0; i < numDimensions(); i++) {
			sameDims = sameDims && this.getDim(i) == s.getDim(i);
		}
		return  this.getID() == s.getID() && sameDims;
	}
	
	@Override
	public String toString() {
		return"Name:"  + name + 
				"ID: " + id + 
				Arrays.toString(domain);
	}
}
