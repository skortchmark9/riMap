package baconGraph;

import graph.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Main engine of the program. Bacon performs djikstra's algorithm to find
 * bacon passes at movie premieres. 
 * @author skortchm
 *
 */
public class Bacon {
	String firstActor; //The source actor.
	String lastActor; //The destination actor.
	Map<Integer, BaconNode> nodes; //A hashmap which contains all of the nodes we have already looked for. 
	Resources r;

	/** Only constructor for bacon. */
	Bacon(String actor1Name, String actor2Name, Resources r) {
		firstActor = actor1Name; 
		lastActor = actor2Name;
		nodes = new HashMap<Integer, BaconNode>();
		this.r = r;
		boolean problem = false;		
		String ID = r.indexFile.getXByY("id", lastActor);
		if (ID == null) {
			System.out.println(String.format("ERROR: Could not find: %s in Index File", lastActor));
			System.exit(1);
		}
	}

	public void getPath() {
		constructGraphToDest(); //Construct the graph and do djikstra concurrently. Easier said than done.
		BaconNode sourceNode = nodes.get(firstActor.hashCode());
		BaconNode destNode = nodes.get(lastActor.hashCode());
		//If either node fails to appear in the graph, there can be no connection between them.
		if (sourceNode == null || destNode == null) {
			System.out.println(String.format("%s -/- %s", firstActor, lastActor));
			System.exit(0);
		}
		//We trace the previous nodes back from the destination node until we've reached the source.
		List<BaconNode> path = new ArrayList<>();
		for (BaconNode node = destNode; node != null; node = node.getPrevious()) {
			path.add(node);
		}
		Collections.reverse(path);

		//We find the correct edge (movie) by consulting the neighbor hashmap.
		for(int i = 0; i < path.size() - 1; i++) {
			BaconNode currentActor = path.get(i);
			BaconNode nextActor =  path.get(i + 1);
			String movieTitle = null;
			Edge<Actor> e = currentActor.getNeighbor(nextActor.hashCode());
			if (e.getTarget().equals(nextActor)) {
				movieTitle = e.getName();
			}
			//If the requisite edge isn't a neighbor, then there is no connection.
			if (movieTitle == null) {
				System.out.println(String.format("%s -/- %s", firstActor, lastActor));
				break;
			}
			System.out.println(String.format("%s -> %s : %s",
					currentActor.getValue().getName(),
					nextActor.getValue().getName(),
					movieTitle));
		}
	}

	/**
	 * Performs djikstra's algorithm and also creates a graph concurrently
	 * for maximum efficiency.
	 */
	private void constructGraphToDest() {
		Actor root = new Actor(firstActor, Actor.ActorType.ROOT, r);
		BaconNode source = new BaconNode(root);
		source.setDistance(0.0);
		nodes.put(source.hashCode(), source);
		PriorityQueue<BaconNode> q = new PriorityQueue<>();
		q.offer(source);
		//Here we create and place the root node in the graph.
		while (!q.isEmpty()) {
			//The current nearest node is polled and consulted.

			BaconNode currentNode = q.poll();
			Actor currentActor = currentNode.getValue();
			//	System.out.println(currentActor + " distance: " + currentNode.getDistance());
			//	System.out.println("Starting loop now: ");
			final long startTime = System.currentTimeMillis();
			//Added to visited set/the graph
			nodes.put(currentNode.hashCode(), currentNode);
			//If we've found the destination, we end the recursion.
			if (currentActor.getName().equals(lastActor)) {
				break;
			} else {
				//If not, we add all actors with subsequent names to the queue.
				String lastInitial = currentActor.getLastInitial();
				List<Movie> movies = currentActor.getMovies();
				int actors = 0;
				int mov = 0;
				for(Movie movie : movies) {
					mov++;
					String mTitle = movie.getTitle();
					List<Actor> movieActors = movie.getActors();
					int weight = movieActors.size();
					for(Actor a : movieActors) {
						actors++;
						if (!a.equals(currentActor) && a.getFirstInitial().equals(lastInitial)) {
							BaconNode newActorNode = nodes.get(a.hashCode());
							boolean existsInGraph = (newActorNode != null);
							//If the node already exists in the graph, we won't add it to the queue.
							//We will however, check if we've found a shorter path.
							if (!existsInGraph) {
								newActorNode = new BaconNode(a);
							}
							double alternateDistance = currentNode.getDistance() + (1.0 / weight);
							//An aggregate distance from the source  node. 
							if (alternateDistance < newActorNode.getDistance()) {
								newActorNode.setDistance(alternateDistance);
								newActorNode.setPrevious(currentNode);
							}
							nodes.put(newActorNode.hashCode(), newActorNode);
							if (!existsInGraph) {
								q.add(newActorNode);
							}
							//Finally, we add the node as an edge of the current node.
							currentNode.addNeighbor(new BaconEdge(mTitle, weight, newActorNode));
						}
					}
				}

			}
		}
	}
}
