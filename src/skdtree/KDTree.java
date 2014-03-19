package skdtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.MinMaxPriorityQueue;


/** Generic KD Tree which can take any object(s) implementing the Dimensionable
 * 	interface. T values are stored in nodes and leaves are represented by null values.
 * 	We split along the median value of the set of <T>s. The tree is constructed by
 * 	maintaining K arrays that are pre-sorted, and then maintaining the ordering of those
 * 	arrays across all child nodes. This means we only have to do k sorts, and then
 * 	the rest of the construction process is linear.
 * 
 * @author samkortchmar
 * @param <T>
 */
public class KDTree<T extends Dimensionable> {

	private T value;
	private KDTree<T> parent, lessChild, moreChild; //lessChild holds equivalent values also.
	private int depth;
	private static int numDims;
	private boolean checked = false;

	/////////////////////////////KD TREE CONSTRUCTION//////////////////////////////

	/** 
	 * Public constructor for KDTree, which creates a tree from a list of Ts
	 * It copies the list K times and then sorts each one independently, in
	 * order to save time sorting later. */
	public KDTree(List<T> stuff) {
		T exemplar = stuff.get(0);
		numDims = exemplar.numDimensions();
		//Here we're making a 2d list, to hold a sorted list for each dimension.
		List<List<T>> dimSortedLists = new ArrayList<List<T>>(numDims);
		for (int i = 0; i < numDims; i++) {
			List<T> temp = new ArrayList<T>(stuff); //copies an arraylist and
			//sorts it according to the current dimension.
			Collections.sort(temp, new DimensionComparator(i));
			dimSortedLists.add(temp);
		}
		parent = null;
		depth = 0;
		int currentDim = depth % numDims;
		
		
		//We find the median element of the list for the current dimension..
		int medianIndex = medianIndex(dimSortedLists.get(currentDim));
		value = medianVal(dimSortedLists.get(currentDim), medianIndex);
		//We split each of the presorted lists into less/equal and greater
		//branches, taking the medianVal as the splittingpane.
		Branches b = new Branches(dimSortedLists, medianIndex, currentDim);
		List<List<T>> lesserBranch = b.getLesserBranch();
		List<List<T>> greaterBranch = b.getGreaterBranch();
		//We count on the lists all containing the same number of elements.
		if (lesserBranch.get(0).isEmpty()) {
			lessChild = null; //our equivalent of a leaf node.
		}
		else {
			//here the recursive structure becomes obvious.
			lessChild = new KDTree<>(lesserBranch, this);
		}
		if (greaterBranch.get(0).isEmpty()) {
			moreChild = null;
		}
		else {
			moreChild = new KDTree<>(greaterBranch, this);
		}
	}

	/** This constructor is used exclusively by KDTree. This constructor
	 * 	relies on the dimension sorted lists that come from branches.
	 *  */
	private KDTree(List<List<T>> dimSortedLists, KDTree<T> parent) {
		this.parent = parent;
		this.depth = parent.depth + 1;
		int currentDim = depth % numDims;
		int medianIndex = medianIndex(dimSortedLists.get(currentDim));
		value = medianVal(dimSortedLists.get(currentDim), medianIndex);
		Branches b = new Branches(dimSortedLists, medianIndex, currentDim);
		List<List<T>> lesserBranch = b.getLesserBranch();
		List<List<T>> greaterBranch = b.getGreaterBranch();
		//We count on the lists all containing the same number of elements.
		if (lesserBranch.get(0).isEmpty()) {
			lessChild = null; //our equivalent of a leaf node.
		}
		else {
			//here the recursive structure becomes obvious.
			lessChild = new KDTree<>(lesserBranch, this);
		}
		if (greaterBranch.get(0).isEmpty()) {
			moreChild = null;
		}
		else {
			moreChild = new KDTree<>(greaterBranch, this);
		}
	}

	/** Finds the median element in a list. Used in the constructors.*/
	private T medianVal(List<T> list, int medianIndex) {
		return list.get(medianIndex);
	}

	/** Finds the median index of a list. Used in the constructors.*/
	private int medianIndex(List<T> list) {
		return (int) Math.ceil((list.size() / 2.0) - 1);
	}

	/** Data structure for partitioning dimension-sorted lists into less and more
	 * branches based on a splitting pane.
	 */
	private class Branches {
		List<List<T>> less, more;

		Branches(List<List<T>> dimSortedLists, int medianIndex, int currentDim) {
			T value = dimSortedLists.get(currentDim).get(medianIndex);
			double splittingPane = value.getDim(currentDim);
			//The length of all sublists: the number of Ts.
			int length2d = dimSortedLists.get(0).size();

			//Initializing less and more. Each branch will have the same dimensions.
			less = new ArrayList<List<T>>(numDims);
			more = new ArrayList<List<T>>(numDims);
			for(int i = 0; i < numDims; i++) { //could we save time by splitting the currentDim at the median? It's already sorted into less + med + more
				//The sublists will be approx half of the size of the initial:
				//Which is ~ the same as the median index.
				List<T> tempLessList = new ArrayList<T>(medianIndex);
				//Less list will always have fewer elements, because it also has to handle =
				List<T> tempMoreList = new ArrayList<T>(medianIndex % 2 == 0 ? medianIndex : medianIndex + 1);
				//We iterate through each sorted list, distributing elements to
				//less and more based on comparison to the splitting pane. We
				//make sure we don't add the median value to to either list.
				for(int j = 0; j < length2d; j++) {
					T temp = dimSortedLists.get(i).get(j);
					if (temp.getDim(currentDim) > splittingPane) {
						tempMoreList.add(temp);
					}
					else if (temp.getDim(currentDim) < splittingPane) {
						tempLessList.add(temp);
					}
					else if (temp.getDim(currentDim) == splittingPane && !temp.equals(value)) {
						tempLessList.add(temp);
						//in case of same axis but not same star					
					}
				}
				less.add(tempLessList);
				more.add(tempMoreList);
			}
		}
		List<List<T>> getLesserBranch() {
			return less;
		}
		List<List<T>> getGreaterBranch() {
			return more;
		}
	}

	////////////////////    KD SEARCHING //////////////////////////////////////////

	/** 
	 * Traverses the tree until it encounters a leaf node, and then returns
	 * the current node. Used exclusively at the beginning of neighbors searches.
	 * @param s - the point we are searching for nearest neighbors of.
	 * @return - the bottommost node close to the SearchPoint.
	 */
	private KDTree<T> down(SearchPoint s) {
		int currentDim = depth % numDims;
		double splittingPane = value.getDim(currentDim);
		if (s.getDim(currentDim) > splittingPane) {
			if (moreChild == null) {
				return this;
			}
			else {
				return moreChild.down(s);
			}
		}
		else {
			if (lessChild == null) {
				return this;
			}
			else {
				return lessChild.down(s);
			}
		}
	}

	/** Used to check off nodes.*/
	private void setChecked() {
		if (this != null) {
			checked = true;
		}
	}

	/** Once we've checked off both child nodes of a node, we want to reset
	 * 	them so they don't pollute any future searches. We have to be very
	 *  cautious - making sure we won't ever come back to them during the 
	 *  current search. */
	private void resetChildren() {
		if (lessChild != null)
			lessChild.checked = false;
		if (moreChild != null) 
			moreChild.checked = false;
	}

	private boolean isChecked() {
		return checked;
	}

	public int getNumDims() {
		return numDims;
	}

	/////////////////////////NEAREST NEIGHBOR SEARCHING////////////////////////////


	/** Function for testing: searching through the tree exhaustively for
	 * 	nearest neighbors. Initializes a SearchPoint for recursive usage. */
	@SuppressWarnings("unchecked")
	public List<T> exhaustiveNearestNeighbors(int neighbors, double ...ds) {
		SearchPoint s = new SearchPoint(ds);
		s.initNearestNeighbors(neighbors);
		s = exhaustiveNearestNeighbors(s);
		return (List<T>) s.getNeighbors();
	}

	/** Recursive engine of exhaustive search.*/
	private SearchPoint exhaustiveNearestNeighbors(SearchPoint s) {
		SearchPoint currentPointer = s;
		currentPointer.addNeighbor(value);
		if (lessChild != null)
			currentPointer = lessChild.exhaustiveNearestNeighbors(currentPointer);
		if (moreChild != null)
			currentPointer = moreChild.exhaustiveNearestNeighbors(currentPointer);
		return currentPointer;
	}

	/** Fast nearest neighbors search which relies on the properties of the
	 * 	KD-Tree to eliminate whole branches of the tree. */
	@SuppressWarnings("unchecked")
	public List<T> nearestNeighbors(int neighbors, double...ds) {
		SearchPoint s = new SearchPoint(ds);
		s.initNearestNeighbors(neighbors);
		s = down(s).nearestNeighbors(s);
		return (List<T>) s.getNeighbors();
	}

	/** Recursive engine of nearest neighbors search.*/
	private SearchPoint nearestNeighbors(SearchPoint s) {
		int currentDim = depth % numDims;
		SearchPoint currentPointer = s;

		if (!currentPointer.isFull() || value.distance(currentPointer) < currentPointer.getStoredDistance()) {
			currentPointer.addNeighbor(value);
		}
		setChecked();
		//Defines the current optimal hypersphere around the current node.
		double hypersphere = value.getDim(currentDim) - currentPointer.getDim(currentDim);
		//Checks if the current hypersphere is closer than the current worst
		//nearest neighbor.
		boolean hypersphereWithinRange = Math.pow(hypersphere, 2) <= currentPointer.getStoredDistance();

		//If the current node is neither checked, nor null, we will consider it.
		if (!(lessChild == null || lessChild.isChecked())) {
			//If the currentPointer isn't full, then we don't care about distance.
			//If the hypersphere is within range, then we'll definitely want check the point.
			//If the hypersphere is NOT within range, but some points in the subtree
			//might have a hypersphere within range, then we have to check the node.
			if  (!currentPointer.isFull() || hypersphereWithinRange || hypersphere >= 0) {
				currentPointer = lessChild.nearestNeighbors(currentPointer);
			}
			else {
				//If the currentPointer is full, the hypersphere is out of range and
				//will not improve, then we don't haveto consider this branch. 
				lessChild.setChecked();
			}
		}
		if (!(moreChild == null || moreChild.isChecked())) {
			if  (!currentPointer.isFull() || hypersphereWithinRange || hypersphere < 0) {
				currentPointer = moreChild.nearestNeighbors(currentPointer);
			}
			else {
				moreChild.setChecked();
			}
		}
		//At this point, both children will definitely be checked. We can seal
		//up this branch and continue moving up the tree.
		resetChildren();
		if (parent == null) { //We're at the root! We're done!
			this.checked = false;
			return currentPointer;
		}
		else if (!parent.isChecked()) { //We need to walk up the tree
			return parent.nearestNeighbors(currentPointer);
		}
		else { //The parent has been checked - this search must be for
			//a subtree of the parent node. The parent node will handle further
			//tree traversal.
			return currentPointer;
		}
	}

	/////////////////////////RADIUS NEIGHBOR SEARCHING ////////////////////////////

	/** Calculates the distance to all stars and then picks the ones that lie
	 * within the given radius.
	 * @param radius - the radius (squared) between the stars.
	 * @param ds
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> exhaustiveRadiusNeighbors(double radius, double...ds) {
		SearchPoint s = new SearchPoint(ds);
		s.initRadius(radius);
		s = exhaustiveRadiusNeighbors(s);
		return (List<T>) s.getNeighbors();
	}

	private SearchPoint exhaustiveRadiusNeighbors(SearchPoint s) {
		SearchPoint currentPointer = s;
		if (s.distance(value) < s.getStoredDistance()) {
			currentPointer.addNeighbor(value);
		}
		if (lessChild != null)
			currentPointer = lessChild.exhaustiveNearestNeighbors(currentPointer);
		if (moreChild != null)
			currentPointer = moreChild.exhaustiveNearestNeighbors(currentPointer);
		return currentPointer;		
	}

	@SuppressWarnings("unchecked")
	public List<T> radiusNeighbors(double radius, double...ds) {
		SearchPoint s = new SearchPoint(ds);
		s.initRadius(radius);
		s = down(s).radiusNeighbors(s);
		return (List<T>) s.getNeighbors();
	}

	/** Conceptually similar to nearestNeighbors, except that we don't need
	 * 	to maintain a bounded priority queue - we no longer check if the queue
	 *  is empty. Instead, a simple list will hold the dimensionable objects.
	 * 	At each node we check if it is within the given radius, and then if its
	 *  children might be.
	 * @param s
	 * @return
	 */
	private SearchPoint radiusNeighbors(SearchPoint s) {
		int currentDim = depth % numDims;
		SearchPoint currentPointer = s;
		if (value.distance(currentPointer) < currentPointer.getStoredDistance()) {
			currentPointer.addNeighbor(value);
		}
		setChecked();
		//Defines the current optimal hypersphere around the current node.
		double hypersphere = value.getDim(currentDim) - currentPointer.getDim(currentDim);
		//Checks if the current hypersphere is closer than the current radius
		boolean hypersphereWithinRange = Math.pow(hypersphere, 2) <= currentPointer.getStoredDistance();

		//If the current node is neither checked, nor null, we will consider it.
		if (!(lessChild == null || lessChild.isChecked())) {
			//If the hypersphere is within range, then we'll definitely want check the point.
			//If the hypersphere is NOT within range, but some points in the subtree
			//might have a hypersphere within range, then we have to check the node.
			if  (hypersphereWithinRange || hypersphere >= 0) {
				currentPointer = lessChild.radiusNeighbors(currentPointer);
			}
			else {
				lessChild.setChecked();
			}
		}
		if (!(moreChild == null || moreChild.isChecked())) {
			if  (hypersphereWithinRange || hypersphere < 0) {
				currentPointer = moreChild.radiusNeighbors(currentPointer);
			}
			else {
				moreChild.setChecked();
			}
		}
		//At this point, both children will definitely be checked. We can seal
		//up this branch and continue moving up the tree.
		resetChildren();
		if (parent == null) { //We're at the root! We're done!
			this.checked = false;
			return currentPointer;
		}
		else if (!parent.isChecked()) { //We need to walk up the tree
			return parent.radiusNeighbors(currentPointer);
		}
		else { //The parent has been checked - this search must be for
			//a subtree of the parent node. The parent node will handle further
			//tree traversal.
			return currentPointer;
		}
	}



	/** Testing function for making sure no nodes of the tree are: "checked"
	 *  after a search.
	 * @return
	 */
	public boolean isClean() {
		if (this.checked) {
			System.out.println("Dirty node at: " + this.depth);
		}
		return !this.checked && (lessChild == null || (lessChild != null && lessChild.isClean())) && 
				(moreChild == null || (moreChild != null && moreChild.isClean()));
	}

	@Override
	public String toString() {
		return value.toString(); 
	}

	/** Testing function which prints a binary tree! So useful! */
	public void printBinaryTree(KDTree<T> root, int level){
		if(root==null)
			return;
		printBinaryTree(root.moreChild, level+1);
		if(level!=0){
			for(int i=0;i<level-1;i++)
				System.out.print("|\t");
			System.out.println("|-------"+root.value);
		}
		else
			System.out.println(root.value);
		printBinaryTree(root.lessChild, level+1);
	}    
}

class SearchPoint implements Dimensionable {

	double [] domain;
	double storedDistance;
	int numNearestNeighbors;
	MinMaxPriorityQueue<Dimensionable> nearestNeighbors;
	List<Dimensionable> radiusNeighbors;

	/** Creates a "SearchPoint," a generic Dimensionable object
	 * with the same number of dimensions as the other objects in
	 * the KD tree. A searchPoint can be used in many ways, one of
	 * which is the location of nearestNeighbors;
	 * @param dimensions -  variable number of doubles to represent
	 * the dimensions of the SearchPoint in space.
	 */
	public SearchPoint(double ...dimensions) {
		domain = dimensions;
		storedDistance = Double.POSITIVE_INFINITY;
	}

	
	/** Initializes the SearchPoint to do a nearestNeighbors search*/
	void initNearestNeighbors(int neighbors) {
		numNearestNeighbors = neighbors;
		MinMaxPriorityQueue.Builder<Dimensionable> b = 
				MinMaxPriorityQueue.orderedBy(new DimensionComparator(this)).maximumSize(numNearestNeighbors);
		nearestNeighbors = b.create();
	}
	
	/** Initializes the SearchPoint to do a radius search*/
	void initRadius(double radius) {
		nearestNeighbors = null;
		storedDistance = Math.pow(radius, 2);
		radiusNeighbors = new LinkedList<>();
	}

	boolean isFull() {
		return (nearestNeighbors.size() == numNearestNeighbors);
	}

	/** Adds a neighbors to the current SearchPoint.*/
	void addNeighbor(Dimensionable T) {
		if (nearestNeighbors == null && radiusNeighbors == null) {
			System.out.println("ERROR: SearchPoint not initialized for search.");
		}
		else if (nearestNeighbors != null) {
			nearestNeighbors.add(T); //We add it and the queue handles whether or not we keep it
			storedDistance = nearestNeighbors.peekLast().distance(this); //the new worst distance
		}
		else if (radiusNeighbors != null) {
			radiusNeighbors.add(T);
		}
	}

	/** Returns the neighbors list, sorted by distance to the pointer.*/
	List<Dimensionable> getNeighbors() {
		List<Dimensionable> results =  new LinkedList<Dimensionable>();

		if (nearestNeighbors == null && radiusNeighbors == null) {
			System.out.println("ERROR: SearchPoint not initialized for search.");
		}
		else if (nearestNeighbors != null) {
			int originalSize = nearestNeighbors.size();
			for(int i = 0; i < originalSize; i++) {
				results.add(nearestNeighbors.removeFirst()); //Already ordered.
			}
		}
		else if (radiusNeighbors != null) {
			Collections.sort(radiusNeighbors, new DimensionComparator(this));
			results = radiusNeighbors;
		}
		return results;
	}

	double getStoredDistance() {
		return storedDistance;
	}

	@Override
	public int numDimensions() {
		return domain.length;
	}

	@Override
	public double getDim(int i) {
		return domain[i];
	}

	/** Calculates the distance from the SearchPoint to a given dimensionable
	 * object with the same dimensionality.
	 */
	public double distance(Dimensionable d) {
		if (d.numDimensions() != numDimensions()) {
			System.out.println("ERROR: (SearchPoint) objects have different dimensionality: " + d);
			return 0;
		}
		double distance = 0;
		for(int i = 0; i < numDimensions(); i++) {
			distance += Math.pow(getDim(i) - d.getDim(i), 2);
		}
		return distance;
	}
	
	public String getName() {
		return this.toString();
	}
	
	public int getID() {
		return 0;
	}

	@Override
	public String toString() {
		return "SP: " + Arrays.toString(domain);
	}
}