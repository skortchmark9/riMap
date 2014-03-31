package kdtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Abstraction used to run algorithms such as nearest neighbor 
 * and radial search on a collection of nodes organized into a KDTree.
 * 
 * @author emc3
 *
 * @param <KDType>
 */
public class KDTree<KDType extends KDimensionable> {
	
	private KDNode _root;
	private int _k = 0; //number of dimensions
	private long _size;
	private List<MinMaxPair> bounds;
	

	private NeighborList _neighborList;
	
	/**
	 * Default constructor.<br>
	 * Initializes the root node to null, and sets the 
	 * number of dimensions this KD tree will hold to the
	 * specified integer.
	 * 
	 * @param k -	The number of dimensions that 
	 * 							this KDTree will hold.
	 */
	public KDTree(int k) {
		_root = null;
		_size = 0;
		if (k > 0)
			_k = k;
		else
			System.err.println("ERROR: KDTree constructor: Dimension must be at least 1");
		bounds = new ArrayList<>(_k);
	}
	
	/**
	 * Alternate constructor.<br>
	 * Calls the default constructor, which initializes the root node to null.
	 * Also, the number of dimensions of the FIRST object in the collection
	 * will be used to set the KDTree's number of dimension. Therefore this
	 * constructor assumes that the dimension is the same for each 
	 * KDimensionable object in the list. 
	 * 
	 * @param collection -	A collection of KDimensionable objects that will
	 * 						be used to create the tree.
	 */
	public KDTree(Collection<KDType> collection) {
		this(collection.iterator().next().getNumDimensions()); //call default constructor with dimension of first object in list.
		ArrayList<List<KDType>> superList = new ArrayList<>(_k);
		for (int i = 0; i < _k; i++) {
			ArrayList<KDType> l = new ArrayList<>(collection);
			KDimensionComparator comparator = new KDimensionComparator(i); //compare nodes on axis i
			Collections.sort(l, comparator); //sort the list using comparator : ascending by axis i
			double min = l.get(0).getCoordinates()[i];
			double max = l.get(l.size() - 1).getCoordinates()[i];
			bounds.add(new MinMaxPair(min, max));
			superList.add(l);
		}
		_root = this.recursiveBuildFaster(superList, 0); //build a new KDTree from the collection.
	}
	
	public double getMax(int axis) {
		return bounds.get(axis).getMax();
	}
	
	public double getMin(int axis) {
		return bounds.get(axis).getMin();
	}

	/**
	 * This was the old constructor.
	 * Used recursiveBuild() instead of recursiveBuildFaster() 
	 */
	/*
	public KDTree(Collection<KDType> collection) {
		this(collection.iterator().next().getNumDimensions()); //call default constructor with dimension of first object in list.
		
		_root = this.recursiveBuild(collection, 0); //build a new KDTree from the collection.
	}
	*/
	
	
	/**
	 * @return the number of dimensions held
	 * by the nodes of this tree.
	 */
	public int getNumDimensions() {
		return _k;
	}
	
	
	/**
	 * This method takes a collection of objects that implement
	 * KDimensionable and recursively builds a balanced KD Tree 
	 * from them.
	 * 
	 * If the KDTree's _k field (number of dimensions) has not been set,
	 * 	Or if the depth (for whatever hellish reason) is negative,
	 * 	Or if the list is empty,
	 * 	an error message will print to STDERROR.
	 *  
	 * 
	 * @param list - The list of KDimensionable objects
	 * @param depth - The depth at which to add the nodes
	 */
	public KDNode recursiveBuild(Collection<KDType> collection, int depth) {
		List<KDType> list;
		if(collection instanceof List<?>)
			list = (List<KDType>)collection; //collection is already a List : cast it
		else
			list = new ArrayList<KDType>(collection); //build list from the collection
		int size = list.size();
		
		if (_k < 1) {
			System.err.println("ERROR: KDTree.recursiveBuild(): Number of Dimensions has not been defined.");
			return null;
		}
		if (depth < 0) {
			System.err.println("ERROR: KDTree.recursiveBuild(): Cannot build tree from a negative depth!");
			return null;
		}
		if(size == 0) {
			System.err.println("ERROR: KDTree.recursiveBuild(): Cannot build tree from an empty list.");
			return null;
		}
		
		int axis = depth % _k;	//the 'axis' is the dimension that the nodes should be compared by.
								//it is determined by the current depth
		
		KDimensionComparator comparator = new KDimensionComparator(axis); //new comparator for each depth's axis
		Collections.sort(list, comparator); //sort the list : ascending by axis
		
		//split list by median
		KDType median;
		int mid = size/2; //floor of size/2
		median = list.get(mid); //get median object
		
		//create node, split list around median and recur
			//if there is only one node left in the list, it's our median,
			//so we're done with building the tree and we can return the 
			//last node which we just created from the median.
		KDNode node = new KDNode(median);
		if (depth == 0)
			_root = node;
		
		if (size > 2) {
			node.putLeft(this.recursiveBuild(list.subList(0, mid), depth+1)); //Recur on sublist of everything before midpoint
			node.putRight(this.recursiveBuild(list.subList(mid+1, size), depth+1)); //recur on sublist of everything after midpoint
		} else if (size == 2) { //mid must be 1
			if (list.get(0).compareAxis(list.get(1), axis) >= 0)
				node.putRight(this.recursiveBuild(list.subList(0, 1), depth+1)); //the node before mid
			else
				node.putLeft(this.recursiveBuild(list.subList(0, 1), depth+1)); //node before mid
		}
		_size++;
		return node;
	}
	
	
	/**
	 * This method takes a collection of objects that implement
	 * KDimensionable and recursively builds a balanced KD Tree 
	 * from them.
	 * 
	 * If the KDTree's _k field (number of dimensions) has not been set,
	 * 	Or if the depth (for whatever hellish reason) is negative,
	 * 	Or if the list is empty,
	 * 	an error message will print to STDERROR.
	 *  
	 * 
	 * @param list - The list of KDimensionable objects
	 * @param depth - The depth at which to add the nodes
	 */
	public KDNode recursiveBuildFaster(ArrayList<List<KDType>> superList, int depth) {
		int size = superList.get(0).size();
		
		if (_k < 1) {
			System.err.println("ERROR: KDTree.recursiveBuildFaster(): Number of Dimensions has not been defined.");
			return null;
		}
		if (depth < 0) {
			System.err.println("ERROR: KDTree.recursiveBuildFaster(): Cannot build tree from a negative depth!");
			return null;
		}
		if(size == 0) {
			System.err.println("ERROR: KDTree.recursiveBuildfaster(): Cannot build tree from an empty list.");
			return null;
		}
		
		int axis = depth % _k;	//the 'axis' is the dimension that the nodes should be compared by.
								//it is determined by the current depth
		
		//split list by median
		KDType median;
		int mid = size/2; //floor of size/2
		median = superList.get(axis).get(mid); //get median object from desired axis-sorted list in superList 
		
		//create node, split list around median and recur
			//if there is only one node left in the list, it's our median,
			//so we're done with building the tree and we can return the 
			//last node which we just created from the median.
		KDNode node = new KDNode(median);
		if (depth == 0) _root = node; //set node to root if it's the first one
		
		//create new sets of branches, split around the median object.
		Branches branches = new Branches(superList, mid, axis);
		ArrayList<List<KDType>> less = branches.less; //all object less than or equal to (on current axis)
		ArrayList<List<KDType>> more = branches.more; //all objects greater than (on current axis)
		
		if (!less.get(0).isEmpty())
			node.putLeft(this.recursiveBuildFaster(less, depth+1));
		if (!more.get(0).isEmpty())
			node.putRight(this.recursiveBuildFaster(more, depth+1));
		//XXX to save memory.
		branches = null;
		
		_size++;
		return node;
	}
	
	/**
	 * Searches this tree for at most n nearest neighbors 
	 * to the given point, and returns a list of nearest-neighbor
	 * KDimensionable objects in a list,
	 * sorted nearest to farthest
	 * 
	 * @param n - the number of neighbors to return in the list
	 * @param testPoint - the test point from which neighbors are compared/found
	 * @return a list of (at most) n KDimensional objects, in ascending order
	 * 			by distance to the test point.
	 */
	public List<KDType> getNearestNeighbors(int n, KDimensionable testPoint) {
		if (_root == null) {
			System.err.println("ERROR: KDTree.getNearestNeighbors(): Tree has not been constructed.");
			return null;
		}
		if (n < 0) {
			System.err.println("ERROR: KDTree.getNearestNeighbors(): Cannot search negative number of neighbors.");
			return null;
		} else if (n == 0) {
			return new ArrayList<KDType>(0);
		}
		
		_neighborList = new NeighborList(n, testPoint);
		this.recursiveNeighborSearch(_root, 0);

		List<KDType> l = _neighborList.asList(); //sorts the list for you
		_neighborList.clear(); //dont need it anymore; clear it out to be safe.
		return l; //returns a sorted list
	}
	
	/**
	 * Recursive nearest neighbor search.<br>
	 * Searches for nearest neighbors according to the supplied neighbor list,
	 * inserting objects it finds along the way. The NeighborList class
	 * takes care of deciding whether or not the object should actually be added
	 * to the neighbor list.
	 * 
	 * @param currNode - a node holding a candidate for nearest neighbor, 
	 * 					whose subtrees this algorithm will search.
	 * @param depth - the current depth of this search in the tree. root = depth 0.
	 * @param neighbors - the list of neighbors to pass around, stores the test point.
	 * @return
	 * A list of nearest neighbors, sorted in ascending order by distance to
	 * the test point and  bound to a specific quantity.
	 */
	private void recursiveNeighborSearch(KDNode currNode, int depth) {
		if (currNode == null) return;
		KDimensionable p = _neighborList.getTestPoint();
		KDType curr = currNode.getKDObject();
		if (!curr.equals(p)) {
			_neighborList.insert(curr); //curr will not be inserted if List is at max and curr is worse than the worst	
		}
		
		//recursively search the subtree which contains the test point.
		boolean wentLeft = false;
		int axis = depth % _k;
		if (p.compareAxis(curr, axis) <= 0) {
			this.recursiveNeighborSearch(currNode.getLeft(), depth+1);
			wentLeft = true;
		}	
		else {
			this.recursiveNeighborSearch(currNode.getRight(), depth+1);
		}
		
		//search other subtree if not enough neighbors or distance to test point on axis is less than worst
		//distance of neighborList (axes could be equal)
		double axisDiff = Math.abs(curr.getCoordinates()[axis] - p.getCoordinates()[axis]);
		double worstDist = _neighborList.getWorstDist();
		if (!_neighborList.isFull() || axisDiff < worstDist) {
			//search the subtree we haven't yet searched using the (wentLeft flag)
			if (wentLeft)
				this.recursiveNeighborSearch(currNode.getRight(), depth+1);
			else
				this.recursiveNeighborSearch(currNode.getLeft(), depth+1);
		}
	}
	
	
	/**
	 * Returns all KDimensionable objects in this tree in a sorted list.
	 * The list is sorted in ascending order by distance from the testPoint.
	 * 
	 * @param radiusSquared - the length of the radius to search.
	 * @param testPoint - the point from which to define the radius.
	 * @return
	 * a list of KDimensionable objects in ascending order by distance to the testpoint.
	 */
	public List<KDType> getObjectsWithinRadius(double radiusSquared, KDimensionable testPoint) {
		if (_root == null) {
			System.err.println("ERROR: KDTree.getObjectsWithinRadius(): Tree has not been constructed.");
			return null;
		}
		if (radiusSquared <= 0) {
			System.err.println("ERROR: KDTree.getObjectsWithinRadius(): Cannot search a non-positive radius.");
			return null;
		}
		
		ArrayList<KDType> points = new ArrayList<>();
		points.addAll(this.recursiveRadiusSearch(_root, 0, radiusSquared, testPoint));
		Collections.sort(points, new DistanceToPointComparator(testPoint)); //sort according to distance to the test point
		return points;
	}
	
	/**
	 * Recursive radius search.<br>
	 * Searches the tree starting with currNode and descending 
	 * into the left and/or right subtrees as necessary, 
	 * maintaining a list of KDimensionables within the radius.
	 * 
	 * @param currNode - the current node that the recursive search is looking at.
	 * @param depth - the depth of the current node. root depth = 0.
	 * @param radius - the length of the radius to search
	 * @param testPoint - the point from which the radius is defined.
	 * @return
	 * A collection of KDimensionable object that are contained in 
	 * 	currNode's subtrees (in no particular order).
	 */
	private Collection<KDType> recursiveRadiusSearch(KDNode currNode, int depth, double radius, KDimensionable testPoint) {
		ArrayList<KDType> points = new ArrayList<>();
		if (currNode == null)
			return points;
		
		KDType curr = currNode.getKDObject();
		double currDist = curr.distanceTo(testPoint);
		if (currDist < radius) { 
			//if curr is within radius, add it and search subtrees.
			if (!curr.equals(testPoint)) {
				points.add(curr);	
			}
			points.addAll(this.recursiveRadiusSearch(currNode.getLeft(), depth+1, radius, testPoint));
			points.addAll(this.recursiveRadiusSearch(currNode.getRight(), depth+1, radius, testPoint));
		} 
		
		else { 
			//curr is not within radius.
			int axis = depth % _k;
			double axisDiff = Math.abs(curr.getCoordinates()[axis] - testPoint.getCoordinates()[axis]);
			if (axisDiff < radius) { //if axis Differential is less than radius, search both subtrees for candidates
				points.addAll(this.recursiveRadiusSearch(currNode.getLeft(), depth+1, radius, testPoint));
				points.addAll(this.recursiveRadiusSearch(currNode.getRight(), depth+1, radius, testPoint));
			} else {
				//axis Differential is larger than radius. Move towards test point in search.
				if (curr.getCoordinates()[axis] <= testPoint.getCoordinates()[axis]) {
					//axis of curr is less than or equal to test point : go right
					points.addAll(this.recursiveRadiusSearch(currNode.getRight(), depth+1, radius, testPoint));
				} else {
					//axis of curr is greater than test point, go left.
					points.addAll(this.recursiveRadiusSearch(currNode.getLeft(), depth+1, radius, testPoint));
				}
			}
		}
		
		return points; //return candidates.
	} 
	
	/**
	 * Tests whether or not this tree contains a specific KDType object.
	 * This method is only used for testing.
	 * 
	 * @param kd the KDimensional object to find.
	 * @return
	 * True if the KDimensionable object is in the tree, false otherwise.
	 */
	public boolean contains(KDType kd) {
		if(_root == null || _k < 1){
			System.err.println("ERROR: KDTree.contains(): Tree is not defined.");
			return false;
		}
		KDNode curr = _root;
		int axis, depth = 0;
		while(curr != null) {
			axis = depth % _k;
			KDType o = curr.getKDObject();
			int result = kd.compareAxis(o, axis);
			if (result > 0) {
				curr = curr.getRight();
			} else if (result < 0) {
				curr = curr.getLeft();
			} else {
				if (kd.equals(o))
					return true;
				else
					curr = curr.getLeft();
			}
			depth++;
			
		}
		return false;
	}
	
	
	/**
	 * Private method only used for subtree testing.
	 * Returns the Node at which the specified KDimensionable object an be found,
	 * that is, if the object is found in the tree.
	 * @param kd
	 * @return
	 */
	@SuppressWarnings("unused")
	private KDNode find(KDType kd) {
		if(_root == null || _k < 1){
			System.err.println("ERROR: KDTree.find(): Tree is not defined.");
			return null;
		}
		KDNode curr = _root;
		int axis, depth = 0;
		while(curr != null) {
			axis = depth % _k;
			KDType o = curr.getKDObject();
			int result = kd.compareAxis(o, axis);
			if (result > 0) {
				curr = curr.getRight();
			} else if (result < 0) {
				curr = curr.getLeft();
			} else {
				if (kd.equals(o))
					return curr;
				else
					curr = curr.getRight();
			}
		}
		return null; //object not found
		
	}
	
	/**
	 * This method is only used for testing.
	 * @return the KDimensionable object stored at the root node.
	 */
	public KDType getRootObject() {
		if (_root != null)
			return _root.getKDObject();
		return null;
	}
	
	public long size() {
		return _size;
	}
	
	
	
	/**
	 * A helper class that splits the 'list of lists'
	 * into lists of lesser and greater. Used to build the tree so
	 * that the list of nodes only has to be sorted 3 times,
	 * (in the beginning- in the constructor).
	 * 
	 * @author emc3 / skortchm
	 * 
	 */
	private class Branches {
		
		private ArrayList<List<KDType>> less, more; //each instance of Branches will be split into 
											//lesser & greater based on the object at the median index.

		Branches(ArrayList<List<KDType>> superList, int medianIndex, int axis) {
			KDType medianVal = superList.get(axis).get(medianIndex); //get median object
			
			//The length of sublist: the number of KDTypes.
			int listSize = superList.get(0).size();

			//Initializing less and more superLists.
			less = new ArrayList<>(_k);
			more = new ArrayList<>(_k);

			for(int i = 0; i < _k; i++) {
				//The sublists will be approximately half of the size of the initial, which is ~ the same as the median index.
				List<KDType> tempLessList = new ArrayList<>(medianIndex);
				//Less list will always have fewer elements, because it also has to handle =
				List<KDType> tempMoreList = new ArrayList<>(medianIndex % 2 == 0 ? medianIndex : medianIndex + 1);

				//We iterate through each sorted list, distributing elements to
				//less and more based on comparison to the splitting pane. make sure to not add the median value to to either list.
				//loop through all elements in list
				for(int j = 0; j < listSize; j++) {
					KDType temp = superList.get(i).get(j); //get the object in the list
					int cmp = temp.compareAxis(medianVal, axis); //compare object to median object

					if (cmp > 0)
						tempMoreList.add(temp);
					else if (cmp < 0)
						tempLessList.add(temp);
					else if (cmp == 0 && !temp.equals(medianVal))
						tempLessList.add(temp); //in case of same axis but not same object, add object to less list.
				}
				less.add(tempLessList);
				more.add(tempMoreList);
			}
		}
	}
	
	
	/**
	 * This inner class represents a KDTree node.
	 * It can hold anything that implements KDimensionable,
	 * which is an Interface supplied with this package.
	 * @author emc3
	 *
	 */
	private class KDNode {
		private KDType _kdObject; //the object which implements KDimensionable
		private KDNode _left, _right; //the left and right children of this node
		
		
		/**
		 * Constructor for a KDNode.<br>
		 * Initializes left & right children to null
		 * to signify 'no children'.
		 * @param o -	An object which implements KDimensionable, to be stored as 
		 * 				the value for this node.
		 */
		private KDNode(KDType o) {
			_kdObject = o;
			_left = null; _right = null; 
		}
		
		
		/**
		 * Returns the object stored at this node.<br>
		 * The object can be anything that implements KDimensionable.
		 * 
		 * @return The object stored at this node.
		 */
		private KDType getKDObject() {
			return _kdObject;
		}
		
		
		/**
		 * @return the left child of this node if one exists.
		 * 	If no left child exists, this method will return null.
		 */
		private KDNode getLeft() {
			return _left;
		}
		
		
		/**
		 * Give this node a left child.
		 * 
		 * @param node -	the node to set as this node's left child.
		 */
		private void putLeft(KDNode node) {
			_left = node;
		}
		
		
		/**
		 * @return the right child of this node if one exists.
		 * 	If no left child exists, this method will return null.
		 */
		private KDNode getRight() {
			return _right;
		}
		
		
		/**
		 * Give this node a right child.
		 * 
		 * @param node -	the node to set as this node's right child.
		 */
		private void putRight(KDNode node) {
			_right = node;
		}	
	}
	
	private class MinMaxPair {
		double minValue;
		double maxValue;
		
		MinMaxPair(double min, double max) {
			minValue = min;
			maxValue = max;
		}
		
		double getMin() {
			return minValue;
		}
		double getMax() {
			return maxValue;
		}
	}
	
	
	/**
	 * This inner class represents a
	 * Bounded-priority queue which holds KDimensionable
	 * objects. The objects are sorted by their distance 
	 * to a fixed test point, supplied on construction of
	 * the list (the distance of the object to the test
	 * point is its priority in the queue).
	 * 
	 * @author emc3
	 */
	private class NeighborList implements Comparator<KDType> {
		
		private final int _n; //the number of neighbors to keep in the list.
		private final KDimensionable _testPoint; //the fixed point to compare all neighbors to
		private ArrayList<KDType> _neighbors; //the list of neighbors
		private KDType _worst;	//the object with the worst distance to the test point
		private double _worstDist;
		
		/**
		 * Default constructor<br>
		 * Creates a new Nearest-Neighbor, which ranks nodes
		 * in ascending order by distance to the test point.
		 * <p>
		 * The test point is specified by the KDimensionable object.
		 * The list is bound to n members.
		 * 
		 * @param n		- The number of neighbors that this list holds. The list will never outgrow n.
		 * @param point	- The test point; neighbors will be compared by their distance to this point.
		 */
		private NeighborList(int n, KDimensionable point) {
			_n = n;
			_neighbors = new ArrayList<>(_n);
			_testPoint = point;
			_worst = null;
			_worstDist = Double.POSITIVE_INFINITY;
		}
		
		/**
		 * Inserts a KDimensionable object to the nearest neighbor list,
		 * and sorts the list by distance to the test point.
		 * <p>
		 * If this list is at capacity (size = n),
		 * the incoming KDimensional object is compared to the 'worst' object in the list,
		 * i.e. the item in the list which is farthest from the test point.
		 * If the incoming object is better than the worst, the incoming object is
		 * added to the list and the list is re-sorted.<br>
		 * If the incoming object is worse than the worst, it is thrown out.
		 * 
		 * @param kd - the incoming object to insert into the list, provided that it
		 * 				is better than current worst item in the list.
		 */
		private void insert(KDType kd) {
			double dist = kd.distanceTo(_testPoint);
			if (_neighbors.size() < _n) { //if the list is not at capacity, add the neighbor.
				if(dist > _worstDist) { //if it's the new worst neighbor, grab it
					_worst = kd;
					_worstDist = dist; 
				}
				_neighbors.add(kd); //add the new nieghbor to the list.
			} else if (dist < _worstDist) { //if the list is at capacity, remove worst, add new, determine new worst.
					_neighbors.add(kd); //add new neighbor
					Collections.sort(_neighbors, this); // sort the list by distance to test point
					_neighbors.remove(_neighbors.size() -1); //never going to remove kd because we already determined its closer than the worst.
					_worst = _neighbors.get(_neighbors.size() - 1); //new worst using new size
					_worstDist = _worst.distanceTo(_testPoint);
			}
		}
		
		/**
		 * returns true if this List is at capacity n
		 * @return true if List at capacity, false otherwise
		 */
		private boolean isFull() {
			if (_neighbors.size() == _n) return true;
			return false;
		}
		
		/**
		 * @return
		 * the 'worst' KDimensionable object in the list.<br>
		 * i.e. the coordinates with the greatest distance to
		 * the test point.
		 */
		private double getWorstDist() {
			return _worstDist;
		}
		
		
		/**
		 * @return
		 * The list of nearest neighbors.
		 * Size of list is bound by n, the integer
		 * which was specified to this list on construction.
		 */
		private List<KDType> asList() {
			Collections.sort(_neighbors, this);
			return _neighbors;
		}
		
		/**
		 * @return the number of neighbors currently in the list
		 */
		@SuppressWarnings("unused")
		private int size() {
			return _neighbors.size();
		}
		
		
		/**
		 * @return
		 * The test point which is used to sort the list by
		 * objects' distances.
		 */
		private KDimensionable getTestPoint() {
			return _testPoint;
		}
		
		private void clear() {
			_neighbors = null;
			_worst = null;
			_worstDist = Double.POSITIVE_INFINITY;
		}
		
		/**
		 * Compares KDimensionable objects based on their distance to the input point.
		 * This will order the list so that objects closer to the input point
		 * will come first in the list and objects that are farther away will
		 * be at the end of the list.
		 * 
		 * @return	-1 if the first object is closer to the input point
		 * 			0 if the objects are the same distance to the input point
		 * 			1 if the first object is farther away form the input point.
		 */
		@Override
		public int compare(KDType o1, KDType o2) {
			double d1 = o1.distanceTo(_testPoint);
			double d2 = o2.distanceTo(_testPoint);
			if (d1 < d2) {
				return -1;
			} else if (d1 > d2) {
				return 1;
			}
			return 0;
		}
		
	}
	
	/**
	 * This inner class represents a comparator used to sort
	 * KDimensionable objects in a list according to a given test point.
	 * <p>
	 * This class is employed by the radius search, in order to
	 * return a list of KDimensionable objects within a radius 
	 * to a given point, p, where the list is sorted by the objects'
	 * distance to that point, in ascending order.
	 * 
	 * @author emc3
	 *
	 */
	private class DistanceToPointComparator implements Comparator<KDType> {
		
		private final KDimensionable _p; //the test point
		
		/**
		 * Default constructor. Initializes the Test point
		 * to use for this comparator.
		 * 
		 * @param p the test point to use for this comparator.
		 */
		private DistanceToPointComparator(KDimensionable p) {
			_p = p;
		}
		
		
		/**
		 * Compares 2 points based on their distance to the test point, _p, 
		 * specified in the constructor.
		 * 
		 * @return
		 * -1 if the first object is closer to the test point than the second<br>
		 * 0 if the two objects are equal distance from the test point<br>
		 * 1 if the first object is farther from the test point than the second object.
		 */
		@Override
		public int compare(KDType o1, KDType o2) {
			double d1 = o1.distanceTo(_p);
			double d2 = o2.distanceTo(_p);
			
			if (d1 < d2)
				return -1;
			else if (d1 > d2)
				return 1;
			return 0;
		}
		
	}
}
