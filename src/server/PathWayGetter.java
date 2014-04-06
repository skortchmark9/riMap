package server;

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
import shared.PathResponse;

/**
 * Class 
 * @author samkortchmar
 *Class for
 */
public class PathWayGetter {

	ExecutorService _executor;
	ClientHandler _owner;
	
	public PathWayGetter(ClientHandler owner) {
		_owner = owner;
		_executor = Executors.newFixedThreadPool(5);
	}

	public void findPath(Node start, Node end, int seconds) {
		Runnable worker = new FinderThread(start,end, seconds);
		_executor.execute(worker);
	}

	private class FinderThread implements Runnable {

		private int _timeout;
		private Node _start, _end;

		FinderThread(Node start, Node end, int timeout) {
			_timeout = timeout;
			_start = start;
			_end = end;
		}

		@Override
		public void run() {
			Future<List<Way>> future = _executor.submit(new CallableWays(_start, _end));
			List<Way> wayList = new LinkedList<>();
			PathResponse pr;
			try {
				wayList = future.get(_timeout, TimeUnit.SECONDS);
			} catch (InterruptedException e1) {
				return;
			} catch (ExecutionException e1) {
			} catch (TimeoutException e1) {
				pr = new PathResponse("The search timed out"));
			}
			if (wayList.isEmpty()) {
				pr = new PathResponse("Sorry, could not find a path");
			}
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
			return _owner.getBackend().getPath(start, end);
		}
	}
}