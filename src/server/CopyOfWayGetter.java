package server;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
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
	private ThreadPoolExecutor _exec;
	private Future<WayResponse> futureWays;
	private volatile boolean _running;

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
		WayResponse resp;
		while(_running) {
			if (futureWays != null && !futureWays.isDone()) {
				try {
					resp = futureWays.get();
					if (resp != null)
						_owner._responseQueue.add(resp);
				} catch (InterruptedException | CancellationException | ExecutionException e) {
<<<<<<< HEAD
=======
					//e.printStackTrace();
>>>>>>> db28c1c4bfdd83a6eefcbdc0db4ab2166a781e4d
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
		if (futureWays != null) {
			futureWays.cancel(true);
			_exec.purge();
		}
		
		if (_running)
			futureWays = _exec.submit(new WayWorker(minLat, maxLat, minLon, maxLon));
	}

	class WayWorker implements Callable<WayResponse> {
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
		public WayResponse call() throws Exception {
			List<Way> ways = _owner._b.getWaysInRange(_minLat, _maxLat, _minLon, _maxLon);
			return new WayResponse(ways, _minLat, _maxLat, _minLon, _maxLon);
		}
	}

}