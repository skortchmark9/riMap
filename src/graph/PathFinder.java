package graph;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class PathFinder<T extends PathNode<T>> {
	PathNode<T> source;
	PathNode<T> dest;
	Map<Integer, PathNode<T>> consideredNodes;

	//	PathFinder(String srcName, String dstName, Resources r) {
	//Should we take two nodes or two names?
	PathFinder(PathNode<T> source, PathNode<T> dest) {
		this.source = source;
		this.dest = dest;
		consideredNodes = new HashMap<Integer, PathNode<T>>();
	}

	public void getPath() {
		//Populates 'nodes' and performs djikstra's algorithm, assigning distances
		//and previous nodes to each one.
		constructGraphToDest(source);
		PathNode<T> sourceNode = consideredNodes.get(source.hashCode());
		PathNode<T> destNode = consideredNodes.get(dest.hashCode());
		if (sourceNode == null || destNode == null) {
			System.out.println(String.format("%s -/- %s", source.getName(), dest.getName()));
			System.exit(0);
		}
		List<PathNode<T>> path = new ArrayList<>();
		//Iterates from the destination node back to the root (whose previous is null)
		//This creates a list of the nodes. We still need to connect them by identifying the
		//correct edges though.
		for (PathNode<T> node = destNode; node != null; node = node.getPrevious()) {
			path.add(node);
		}
		Collections.reverse(path);

		for(int i = 0; i < path.size() - 1; i++) {
			PathNode<T> currentActor = path.get(i);
			PathNode<T> nextActor =  path.get(i + 1);
			String movieTitle = null;
			//FIXME: Does this work? HASHING vs Strings as keys.
			Edge<? extends PathNode<T>> e = currentActor.getNeighbor(nextActor.getValue().getName());
			if (e.getTarget().equals(nextActor)) {
				movieTitle = e.getName();
			}
			//If the requisite edge isn't a neighbor, then there is no connection.
			if (movieTitle == null) {
				System.out.println(String.format("%s -/- %s", source.getName(), dest.getName()));
				break;
			}
			System.out.println(String.format("%s -> %s : %s",
					currentActor.getValue().getName(),
					nextActor.getValue().getName(),
					movieTitle));
		}
	}

	private void constructGraphToDest(PathNode<T> source) {
		source.setDistance(0.0);
		source.setAStarDistance(source.getDistanceTo(dest) + source.getDistance());
		consideredNodes.put(source.hashCode(), source);
		PriorityQueue<PathNode<T>> fringe = new PriorityQueue<>();
		fringe.offer(source);
		while (!fringe.isEmpty()) {
			PathNode<T> currentNode = fringe.poll();
			T nodeValue = currentNode.getValue();
			consideredNodes.put(currentNode.hashCode(), currentNode);
			//TODO define equality relationship here. Do we really want to check this way?
			if (currentNode.getValue().equals(dest.getValue())) {
				break;
			}
			else {
				Map<String, Edge<? extends PathNode<T>>> edges = nodeValue.getNeighbors();
				for(Edge<? extends PathNode<T>> edge : edges.values()) {
					PathNode<T> neighbor = edge.getTarget();
					//We don't want any duplicates 
					//TODO this section is confusing.
					if (!neighbor.getValue().equals(nodeValue)) {
						PathNode<T> edgeTarget = consideredNodes.get(neighbor.hashCode());
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
						consideredNodes.put(edgeTarget.hashCode(), edgeTarget);
						if (!existsInGraph) {
							fringe.add(edgeTarget);
						}
					}
				}
			}
		}
	}
}