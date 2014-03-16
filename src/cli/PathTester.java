package cli;

import maps.Node;
import maps.PathNodeWrapper;
import backend.Backend;
import edu.brown.cs032.emc3.kdtree.KDStub;
import edu.brown.cs032.emc3.kdtree.KDimensionable;
import graph.PathFinder;

public class PathTester {

	PathTester(String s, Backend b) {
		String[] args = s.split(" ");
		KDimensionable source = new KDStub(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
		KDimensionable dest = new KDStub(Double.parseDouble(args[2]), Double.parseDouble(args[3]));
		Node sourceNode = b.getNearestNeighbors(1, source).get(0);
		Node destNode = b.getNearestNeighbors(1, dest).get(0);
		PathNodeWrapper wrappedSource = new PathNodeWrapper(sourceNode);
		PathNodeWrapper wrappedDest = new PathNodeWrapper(destNode);
		PathFinder<PathNodeWrapper, Node> p = new PathFinder<PathNodeWrapper, Node>(wrappedSource, wrappedDest);
		p.getPath();
	}
}