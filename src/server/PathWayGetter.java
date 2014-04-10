package server;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import maps.Node;
import maps.Way;
import shared.PathResponse;
import backend.Util;

/**
 * @author samkortchmar
 * This class is responsible for getting paths from a client.
 * It implements a timer so that no exhaustive searches take place.
 * It also uses futurees 
 */
public class PathWayGetter extends Thread {

	ClientHandler _owner; //The owning client handler - used to serve messages to the client.
	CallableWays _worker; //The current worker finding a path.
	volatile boolean _running; //Whether not this thread should continue running
	ThreadPoolExecutor _executor; //A small executor for handling workers. 
	Future<PathResponse> waysFuture; //The current future we are waiting on.
	int _timeout; //The timeout that we are waiting for.

	public PathWayGetter(ClientHandler owner) {
		super("PathWayGetter");
		_owner = owner;
		_running = true;
		_executor = Util.defaultThreadPool("PathWayGetter", 2, 2);
	}

	/**
	 * Cancel any previous pathfinding attempts and start a new one.
	 * @param start - the node to start at
	 * @param end - the node to end at
	 * @param seconds - the number of seconds to wait.
	 */
	public void findPath(Node start, Node end, int seconds) {
		if (waysFuture != null) {
			waysFuture.cancel(true);
			_executor.purge();
		}
		_timeout = seconds;
		waysFuture = _executor.submit(new CallableWays(start, end));
	}

	public void close() {
		_executor.shutdown();
		_running = false;
	}

	@Override
	public void run() {
		while(_running) {
			if (waysFuture != null) {
				try {
					PathResponse result = waysFuture.get(_timeout, TimeUnit.SECONDS);
					_owner._responseQueue.add(result);
				} catch (InterruptedException | CancellationException e) {
					continue;
				} catch (ExecutionException e) {
					Util.err("ERROR: Execution Exception in PathWayGetter...usually caused by null pointer.");
				} catch (TimeoutException e) {
					waysFuture.cancel(true);
					waysFuture = null;
					_owner._responseQueue.add(new PathResponse("Timed out after: " + _timeout + " seconds"));
				}
				waysFuture = null;
			}
		}
	}

	class CallableWays implements Callable<PathResponse> {
		Node start, end;

		public CallableWays(Node start, Node end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public PathResponse call() {
			if (start == null && end == null)
				return new PathResponse("Could not find either location");
			else if (start == null) {
				return new PathResponse("Could not find start location");
			} else if (end == null) {
				return new PathResponse("Could not find end location");
			} else {
			List<Way> path = _owner._b.getPath(start, end);
			return path.isEmpty() ? new PathResponse("Could not find a path") :
									new PathResponse(path, start, end);
			}
		}
	}
}