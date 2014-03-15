package graph;
/**
 * The interface for edges to be used in the graph.
 * @author skortchm
 *
 * @param <T> the type of value to be stored in nodes of the graph
 */
public interface Edge<K extends PathNode<?>> {
	K getTarget();
	String getName();
	double getWeight();
}