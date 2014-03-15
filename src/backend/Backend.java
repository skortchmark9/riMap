package backend;

import maps.MapFactory;
import maps.Node;
import edu.brown.cs032.emc3.kdtree.KDTree;

public class Backend {
	
	KDTree<Node> kd = null;
	
	Backend() {
		initKDTree();
	}
	
	private void initKDTree() {
		kd = MapFactory.createKDTree();
	}

}
