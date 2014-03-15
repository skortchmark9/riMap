package graph;

import java.util.Map;
/** 
 * Interface for nodes in graph.
 * @author skortchm
 *
 * @param <T> - the type of value to be stored in the node. 
 */
public interface Node<T> {
	Map<Integer, Edge<? extends Node<T>>> getNeighbors();
	Edge<? extends Node<T>> getNeighbor(int hashcode);
	T getValue();
	double getDistance();
	void setDistance(Double d);
	double getAStarDistance();
	void setAStarDistance(Double d);
	Node<T> getPrevious();
	void setPrevious(Node<T> n);
	@Override
	int hashCode();
	String getName();
	double getDistanceTo(Node<T> n2);
	int compareTo(Node<T> other);
}
