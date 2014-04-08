package graph;

import kdtree.KDimensionable;

/**
 * The interface for edges to be used in the graph.
 * @author skortchm
 *
 * @param <T> the type of value to be stored in nodes of the graph
 */
public interface Edge<T extends KDimensionable> {
	T getTarget();
	String getUniqueID();
	double getWeight();
}