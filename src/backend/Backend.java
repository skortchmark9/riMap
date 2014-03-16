package backend;

import java.io.IOException;
import java.util.List;

import maps.MapFactory;
import maps.Node;
import edu.brown.cs032.emc3.kdtree.KDTree;
import edu.brown.cs032.emc3.kdtree.KDimensionable;

public class Backend {
	
	KDTree<Node> kd = null;
	
	public Backend() throws IOException {
		new Resources();
		initKDTree();
	}
	
	public Backend(String[] args) throws IOException {
		try {
			new Resources(args[0], args[1], args[2]);
		} catch (IOException e) {
			System.out.println("ERROR: Could not generate Resources");
			throw e;
		}
		initKDTree();
	}
	
	private void initKDTree() {
		kd = MapFactory.createKDTree();
	}
	
	public List<Node> getNearestNeighbors(int num, KDimensionable testPoint) {
		if (kd != null) {
			return kd.getNearestNeighbors(num, testPoint);
		} else {
			System.err.println("ERROR: KD TREE NOT INITIALIZED");
			return null;
		}
	}
}
