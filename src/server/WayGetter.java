package server;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import maps.Way;
import shared.WayResponse;
import backend.Util;

/**
 * 
 * @author emc3
 *
 */
class WayGetter {
	private final ClientHandler _owner;
	private ThreadPoolExecutor _exec;
	private AtomicInteger _threadCount;
	private WayWorker _worker;
	
	/**
	 * @param owner
	 */
	public WayGetter(ClientHandler owner) {
		_owner = owner;
		_exec = Util.defaultThreadPool(2, 4);
		_threadCount = new AtomicInteger(0);
	}
	
	/**
	 * 
	 * @param minLat
	 * @param maxLat
	 * @param minLon
	 * @param maxLon
	 */
	void getWays(double minLat, double maxLat, double minLon, double maxLon) {
		WayWorker temp = new WayWorker(minLat, maxLat, minLon, maxLon);
		_exec.execute(temp);
		
		if (_exec.getActiveCount() > 1) {
			_worker.interrupt();
			try {_worker.join();} catch (InterruptedException e) {
				//like so whatever
			}
			_exec.purge();
		}
		
		_worker = temp;
	}
	
	/**
	 * 
	 * @author emc3
	 *
	 */
	class WayWorker extends Thread{
		int _id;
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
		public void run() {
			_id = _threadCount.incrementAndGet();
			List<Way> ways = _owner._b.getWaysInRange(_minLat, _maxLat, _minLon, _maxLon);
			if (_id == _threadCount.get()) {
				_owner._responseQueue.add(new WayResponse(ways));
			}
		}
	}
	
}