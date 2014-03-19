package skdtree;

/** Interface for objects in a KD tree. */
public interface Dimensionable {

	int numDimensions(); //Returns the number of dimensions in the Dimensionable object
	double getDim(int i); //Returns the specific component of the object
	double distance(Dimensionable d); //Calculates the distance (squared) to d
	String getName();
	int getID();
}
