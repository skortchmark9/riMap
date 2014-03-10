package graph;

import java.util.Map;
/** 
 * Interface for nodes in graph.
 * @author skortchm
 *
 * @param <T> - the type of value to be stored in the node. 
 */
public interface Node<T> {
	void addNeighbor(Edge<T> e);
	Map<Integer, Edge<T>> getNeighbors();
	T getValue();
	double getDistance();
	void setDistance(Double d);
	Node<T> getPrevious();
	void setPrevious(Node<T> n);
	int hashCode();
	String getName();
}
