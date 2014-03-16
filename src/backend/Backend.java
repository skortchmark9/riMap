package backend;

import java.io.IOException;
import java.util.List;

import autocorrect.Engine;
import autocorrect.RadixTree;
import maps.MapFactory;
import maps.Node;
import edu.brown.cs032.emc3.kdtree.KDTree;
import edu.brown.cs032.emc3.kdtree.KDimensionable;

public class Backend {
	
	KDTree<Node> kd = null;
	Engine autoCorrectEngine = null;
	
	public Backend(String[] args) throws IOException {
		try {
			new Resources(args[0], args[1], args[2]);
		} catch (IOException e) {
			System.out.println("ERROR: Could not generate Resources");
			throw e;
		}
		initKDTree();
		initAutoCorrect();
	}
	
	private void initKDTree() {
		kd = MapFactory.createKDTree();
	}
	
	private void initAutoCorrect() {
		RadixTree rt = MapFactory.createRadixTree();
		if (rt.isEmpty()) {
			System.out.println("ERROR: Could not instantiate AutoCorrectEngine");
			return;
		} else {
			autoCorrectEngine = new Engine(Constants.defaultGenerator, Constants.defaultRanker, rt);
		}
	}
	
	public List<String> getAutoCorrections(String name) {
		if (autoCorrectEngine != null) {
			return autoCorrectEngine.suggest(name);
		} else {
			System.err.println("ERROR: AutoCorrectEngine is not initialized");
			return null;
		}
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
