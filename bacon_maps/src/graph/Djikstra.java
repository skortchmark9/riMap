/*package graph;

//Didn't have time to finish this I'm afraid. 
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import baconGraph.Parser;

public class Djikstra<T extends Node<T>> {
	String sourceName;
	String destName;
	Node<T> baconGraph;
	Map<Integer, Node<T>> nodes;

	Djikstra(String srcName, String dstName) {
		sourceName = srcName;
		destName = dstName;
		nodes = new HashMap<Integer, Node<T>>();
	}

	public void getPath(Node<T> source) {
		constructGraphToDest(source);
		Node<T> sourceNode = nodes.get(sourceName.hashCode());
		Node<T> destNode = nodes.get(destName.hashCode());
		if (sourceNode == null || destNode == null) {
			System.out.println(String.format("%s -/- %s", sourceName, destName));
			System.exit(0);
		}
		List<Node<T>> path = new ArrayList<>();
		for (Node<T> node = destNode; node != null; node = node.getPrevious()) {
			path.add(node);
		}
		Collections.reverse(path);

		for(int i = 0; i < path.size() - 1; i++) {
			Node<T> currentActor = path.get(i);
			Node<T> nextActor =  path.get(i + 1);
			String movieTitle = null;
			for(Edge<T> e : currentActor.getNeighbors()) {
				if (e.getTarget().equals(nextActor)) {
					movieTitle = e.getName();
				}
			}
			if (movieTitle == null) {
				System.out.println("ERROR: No connecting movie between");
			}
			///CHANGE ME
			System.out.println(String.format("%s -> %s : %s",
					currentActor.getValue().getName(),
					nextActor.getValue().getName(),
					movieTitle));
		}
	}

	private void constructGraphToDest(Node<T> source) {
		source.setDistance(0.0);
		nodes.put(source.hashCode(), source);
		Queue<Node<T>> q = new LinkedList<>();
		q.add(source);
		while (!q.isEmpty()) {
			Node<T> currentNode = q.poll();
			T currentActor = currentNode.getValue();
			System.out.println(currentActor);
			nodes.put(currentNode.hashCode(), currentNode);
			if (currentActor.getName().equals(destName)) {
				break;
			} else {
				String lastInitial = Parser.getLastInitial(currentNode.getName());
				List<Edge<T>> movies = currentActor.getNeighbors();
				for(Edge<T> movie : movies) {
					Node<T> mNode = movie.getTarget();
					if (!mNode.getValue().equals(currentActor) && Parser.getFirstInitial(mNode.getName()).equals(lastInitial)) {
						Node<T> newActorNode = nodes.get(mNode.hashCode());
						if (newActorNode == null) {
							newActorNode = mNode;
							q.add(newActorNode);
							nodes.put(newActorNode.hashCode(), newActorNode);
						}
						double alternateDistance = currentNode.getDistance() + (1 / movie.getWeight());
						if (alternateDistance < newActorNode.getDistance()) {
							newActorNode.setDistance(alternateDistance);
							newActorNode.setPrevious(currentNode);
						}
						currentNode.addNeighbor(movie);
					}
				}
			}
		}
	}
}*/