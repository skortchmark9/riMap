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

import maps.Node;
import maps.Way;

public class PathWayRequester {

	ExecutorService executor = Executors.newSingleThreadExecutor();
	Future<List<Way>> future;
	Backend b;
	
	public PathWayRequester(Backend b) {
		this.b = b;
	}

	public List<Way> getWays(Node start, Node end, int seconds) throws TimeoutException {
		future = executor.submit(new CallableWays(start, end));
		List<Way> way = new LinkedList<>();
		try {
			way = future.get(seconds, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
		} catch (ExecutionException e1) {
		} catch (TimeoutException e1) {
			throw e1;
		}
		return way;
	}


	class CallableWays implements Callable<List<Way>> {

		Node start, end;

		public CallableWays(Node start, Node end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public List<Way> call() {
			return b.getPath(start, end);
		}
	}
}
