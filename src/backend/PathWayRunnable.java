package backend;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import client.Client;
import frontend.MapPane;
import maps.Node;
import maps.Way;

/**
 * @author samkortchmar
 *Class for
 */
public class PathWayRunnable implements Runnable {

	Client client;
	int timeout;
	List<Way> wayList = new LinkedList<>();

	public PathWayRunnable(Client client, int timout) {
		this.client = client;
		this.timeout = timeout;
	}

	@Override
	public void run() {
		Future<List<Way>> future = executor.submit(new CallableWays(map.getStart(), map.getEnd()));
		List<Way> wayList = new LinkedList<>();
		try {
			wayList = future.get(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			return;
		} catch (ExecutionException e1) {
		} catch (TimeoutException e1) {
			Util.guiMessage("ERROR: the search timed out - try again with more time.");
		}
		if (wayList.isEmpty()) {
			Util.guiMessage("Could not find a path between these points");
		}
		map.clearRoute();
		if (Constants.DEBUG_MODE) {
			Util.out("WAYS FOUND:", wayList);
		}
		map.setCalculatedRoute(wayList);
		map.repaint();
	}
}

class CallableWays implements Callable<List<Way>> {

	Node start, end;

	public CallableWays(Node start, Node end) {
		this.start = start;
		this.end = end;
	}

	@Override
	public List<Way> call() {
		return client.getPath(start, end);
	}
}

@Override
public void run() {
	// TODO Auto-generated method stub

}

}