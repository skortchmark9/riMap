package server;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import maps.Way;
import shared.WayResponse;
import backend.Util;

/**
 * 
 * @author skortchm
 *
 */
class CopyOfWayGetter extends Thread {
	private final ClientHandler _owner;
	private ExecutorService _exec;
	private Future<List<Way>> futureWays;
	private volatile boolean _running;
	List<Way> ways;

	/**
	 * @param owner
	 */
	public CopyOfWayGetter(ClientHandler owner) {
		super("CopyOfWayGetter");
		_owner = owner;
		_exec = Util.defaultThreadPool("CopyOfWayGetter", 2, 2);
	}

	public void run() {
		_running = true;
		while(_running) {
			if (futureWays != null && !futureWays.isDone()) {
				try {
					ways = futureWays.get();
					if (ways != null)
						_owner._responseQueue.add(new WayResponse(ways));
				} catch (InterruptedException | CancellationException | ExecutionException e) {
					e.printStackTrace();
					continue; //Standard operating behavior
				}
			}
		}
	}

	public void close() {
		_running = false;
		_exec.shutdownNow();
		try {
			_exec.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param minLat
	 * @param maxLat
	 * @param minLon
	 * @param maxLon
	 */
	void getWays(double minLat, double maxLat, double minLon, double maxLon) {
		if (futureWays != null)
			futureWays.cancel(true);
		if (_running)
			futureWays = _exec.submit(new WayWorker(minLat,maxLat, minLon, maxLon));
	}

	class WayWorker implements Callable<List<Way>> {
		double _minLat, _maxLat, _minLon, _maxLon;

		/**
		 * 
		 * @param minLat
		 * @param maxLat
		 * @param minLon
		 * @param maxLon
		 */
		public WayWorker(double minLat, double maxLat, double minLon, double maxLon) {
			_minLat = minLat;
			_maxLat = maxLat;
			_minLon = minLon;
			_maxLon = maxLon;
		}

		@Override
		public List<Way> call() throws Exception {
			return _owner._b.getWaysInRange(_minLat, _maxLat, _minLon, _maxLon);
		}
	}

}