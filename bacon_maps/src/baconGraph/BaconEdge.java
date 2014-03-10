package baconGraph;

import graph.Edge;
import graph.Node;

class BaconEdge implements Edge<Actor>{

		String name;
		double weight;
		Node<Actor> n;

		BaconEdge(String name, double weight, Node<Actor> n) {
			this.name = name;
			this.weight = weight;
			this.n = n;
		}

		public Node<Actor> getTarget() {
			return n;
		}

		public String getName() {
			return name;
		}
		public double getWeight() {
			return weight;
		}
}