package graph;

import java.util.Map;
/** 
 * Interface for nodes in graph.
 * @author skortchm
 *
 * @param <T> - the type of value to be stored in the node. 
 */
public interface PathNode<T> {
	Map<String, Edge<? extends PathNode<T>>> getNeighbors();
	Edge<? extends PathNode<T>> getNeighbor(String key);
	T getValue();
	double getDistance();
	void setDistance(Double d);
	double getAStarDistance();
	void setAStarDistance(Double d);
	PathNode<T> getPrevious();
	void setPrevious(PathNode<T> n);
	@Override
	int hashCode();
	String getName();
	double getDistanceTo(PathNode<T> n2);
	int compareTo(PathNode<T> other);
}
