package server;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import backend.Util;
import shared.PathResponse;
import maps.Node;
import maps.Way;

/**
 * @author samkortchmar
 * Should be changed probably
 */
public class PathWayGetter implements Runnable {

	ClientHandler _owner;
	CallableWays _worker;
	volatile boolean _running;
	ExecutorService _executor;
	Future<List<Way>> waysFuture;
	int _timeout;

	public PathWayGetter(ClientHandler owner) {
		_owner = owner;
		_running = true;
		_executor = Executors.newSingleThreadExecutor();
		run();
	}

	public void findPath(Node start, Node end, int seconds) {
		if (waysFuture != null) {
			waysFuture.cancel(true);
		}
		_timeout = seconds;
		waysFuture = _executor.submit(new CallableWays(start, end));
	}

	public void close() {
		_running = false;
	}

	@Override
	public void run() {
		while(_running) {
			if (waysFuture != null) {
				try {
					List<Way> ways = waysFuture.get(_timeout, TimeUnit.SECONDS);
					if (ways.isEmpty()) {
						_owner._responseQueue.add(new PathResponse("Could not find a path."));
					} else {
						_owner._responseQueue.add(new PathResponse(ways));
					}
				} catch (InterruptedException e) {
					continue;
				} catch (ExecutionException e) {
					//FIXME
					Util.err("ERROR: Execution Exception in PathWayGetter...not sure why");
					e.printStackTrace();
				} catch (TimeoutException e) {
					_owner._responseQueue.add(new PathResponse("Timed out after: " + _timeout + " seconds"));
				}
				 waysFuture = null;
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
			return _owner._b.getPath(start, end);
		}
	}
}