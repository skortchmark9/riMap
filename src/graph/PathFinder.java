package graph;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import kdtree.KDimensionable;
import maps.PathNodeWrapper;


public class PathFinder<K extends PathNode<T>, T extends KDimensionable> {
	K source;
	K dest;
	HashMap<String, K> consideredNodes;
	public PathFinder(K source, K dest) {
		this.source = source;
		this.dest = dest;
		consideredNodes = new HashMap<String, K>();
	}
	
	@SuppressWarnings("unchecked")
	public List<Edge<T>> getPath() {
		List<Edge<T>> pathWays = new LinkedList<>();
		constructGraphToDest(source);
		K sourceNode = consideredNodes.get(source.getName());
		K destNode = consideredNodes.get(dest.getName());
		if (sourceNode == null || destNode == null) {
			return pathWays;
		}
		List<K> path = new ArrayList<>();
		for (K node = destNode; node != null; node = (K) node.getPrevious()) {
			path.add(node);
		}
		Collections.reverse(path);

		for(int i = 0; i < path.size() - 1; i++) {
			K currentActor = path.get(i);
			K nextActor =  path.get(i + 1);
			String movieTitle = null;
			Edge<T> e = currentActor.getNeighbor(nextActor.getName());
			if (e.getTarget().equals(nextActor.getValue())) {
				movieTitle = e.getUniqueID();
			}
			if (movieTitle == null) {
				return new LinkedList<Edge<T>>();
			} else {
				pathWays.add(e);
			}
		}
		consideredNodes = null;
		return pathWays;
	}

	@SuppressWarnings("unchecked")
	public void printPath() {
		//Populates 'nodes' and performs djikstra's algorithm, assigning distances
		//and previous nodes to each one.
		constructGraphToDest(source);
		K sourceNode = consideredNodes.get(source.getName());
		K destNode = consideredNodes.get(dest.getName());
		if (sourceNode == null || destNode == null) {
			System.out.println(String.format("%s -/- %s", source.getName(), dest.getName()));
			System.exit(0);
		}
		List<K> path = new ArrayList<>();
		//Iterates from the destination node back to the root (whose previous is null)
		//This creates a list of the nodes. We still need to connect them by identifying the
		//correct edges though.
		//FIXME - HOW CAN WE AVOID CASTING HERE
		for (K node = destNode; node != null; node = (K) node.getPrevious()) {
			path.add(node);
		}
		Collections.reverse(path);

		for(int i = 0; i < path.size() - 1; i++) {
			K currentActor = path.get(i);
			K nextActor =  path.get(i + 1);
			String movieTitle = null;
			//FIXME: Does this work? HASHING vs Strings as keys.
			Edge<?> e = currentActor.getNeighbor(nextActor.getName());
			if (e.getTarget().equals(nextActor)) {
				movieTitle = e.getUniqueID();
			}
			//If the requisite edge isn't a neighbor, then there is no connection.
			if (movieTitle == null) {
				System.out.println(String.format("%s -/- %s", source.getName(), dest.getName()));
				break;
			}
			System.out.println(String.format("%s -> %s : %s",
					currentActor.getName(),
					nextActor.getName(),
					movieTitle));
		}
	}

	private void constructGraphToDest(K source) {
		source.setDistance(0.0);
		source.setAStarDistance(source.getDistanceTo(dest) + source.getDistance());
		PriorityQueue<K> fringe = new PriorityQueue<>();
		fringe.offer(source);
		while (!fringe.isEmpty()) {
			K currentNode = fringe.poll();
			T nodeValue = currentNode.getValue();
			consideredNodes.put(currentNode.getName(), currentNode);
			//TODO define equality relationship here. Do we really want to check this way?
			if (nodeValue.equals(dest.getValue())) {
				break;
			}
			else {
				Map<String, Edge<T>> edges = currentNode.getNeighbors();
				for(Edge<T> edge : edges.values()) {
					//FIXME Again, this is a weird cast.
					@SuppressWarnings("unchecked")
					K neighbor =  (K) new PathNodeWrapper(edge.getTarget());
					//We don't want any duplicates 
					//TODO this section is confusing.
					if (!neighbor.getValue().equals(nodeValue)) {
						K edgeTarget = consideredNodes.get(neighbor.getName());
						boolean existsInGraph = (edgeTarget != null);
						//It might be a shorter path than one we've already considered
						if (!existsInGraph) { //If it's not there, then we just find the distance and add it.
							edgeTarget = neighbor;
						}
						//an aggregate distance from the source node.
						double alternateDistance = currentNode.getDistance() + edge.getWeight();
						//An aggregate distance from the source  node.
						if (alternateDistance < edgeTarget.getDistance()) {
							edgeTarget.setDistance(alternateDistance);
							edgeTarget.setPrevious(currentNode);
							edgeTarget.setAStarDistance(edgeTarget.getDistance() + edgeTarget.getDistanceTo(dest));
						}
						consideredNodes.put(edgeTarget.getName(), edgeTarget);
						if (!existsInGraph) {
							fringe.add(edgeTarget);
						}
					}
				}
			}
		}
	}
}