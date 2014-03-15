package graph;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class PathFinder<T extends Node<T>> {
	Node<T> source;
	Node<T> dest;
	Map<Integer, Node<T>> consideredNodes;

	//	PathFinder(String srcName, String dstName, Resources r) {
	//Should we take two nodes or two names?
	PathFinder(Node<T> source, Node<T> dest) {
		this.source = source;
		this.dest = dest;
		consideredNodes = new HashMap<Integer, Node<T>>();
	}

	public void getPath() {
		//Populates 'nodes' and performs djikstra's algorithm, assigning distances
		//and previous nodes to each one.
		constructGraphToDest(source);
		Node<T> sourceNode = consideredNodes.get(source.hashCode());
		Node<T> destNode = consideredNodes.get(dest.hashCode());
		if (sourceNode == null || destNode == null) {
			System.out.println(String.format("%s -/- %s", source.getName(), dest.getName()));
			System.exit(0);
		}
		List<Node<T>> path = new ArrayList<>();
		//Iterates from the destination node back to the root (whose previous is null)
		//This creates a list of the nodes. We still need to connect them by identifying the
		//correct edges though.
		for (Node<T> node = destNode; node != null; node = node.getPrevious()) {
			path.add(node);
		}
		Collections.reverse(path);

		for(int i = 0; i < path.size() - 1; i++) {
			Node<T> currentActor = path.get(i);
			Node<T> nextActor =  path.get(i + 1);
			String movieTitle = null;
			Edge<? extends Node<T>> e = currentActor.getNeighbor(nextActor.hashCode());
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

	private void constructGraphToDest(Node<T> source) {
		source.setDistance(0.0);
		source.setAStarDistance(source.getDistanceTo(dest) + source.getDistance());
		consideredNodes.put(source.hashCode(), source);
		PriorityQueue<Node<T>> fringe = new PriorityQueue<>();
		fringe.offer(source);
		while (!fringe.isEmpty()) {
			Node<T> currentNode = fringe.poll();
			T nodeValue = currentNode.getValue();
			consideredNodes.put(currentNode.hashCode(), currentNode);
			//TODO define equality relationship here. Do we really want to check this way?
			if (currentNode.getValue().equals(dest.getValue())) {
				break;
			}
			else {
				Map<Integer, Edge<? extends Node<T>>> edges = nodeValue.getNeighbors();
				for(Edge<? extends Node<T>> edge : edges.values()) {
					Node<T> neighbor = edge.getTarget();
					//We don't want any duplicates 
					//TODO this section is confusing.
					if (!neighbor.getValue().equals(nodeValue)) {
						Node<T> edgeTarget = consideredNodes.get(neighbor.hashCode());
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