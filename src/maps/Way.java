package maps;

import graph.Edge;

public class Way implements Edge<PathNode> {
	
	PathNode target;
	Way(PathNode p) {
		target = p;
	}

	@Override
	public PathNode getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getWeight() {
		// TODO Auto-generated method stub
		return 0;
	}

}
