package server;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import shared.Response;
import shared.WayResponse;
import backend.Constants;
import backend.Util;

/**
 * 
 * @author emc3
 *
 */
class WayGetter {
	private final ClientHandler _owner;
	private AtomicInteger _threadCount;
	private Executor _exec;
	
	/**
	 * @param owner
	 */
	public WayGetter(ClientHandler owner) {
		_owner = owner;
		_threadCount = new AtomicInteger(0);
		_exec = Util.defaultThreadPool(Constants.THREADPOOL_CORE_SIZE, Constants.THREADPOOL_MAX_SIZE);
	}
	
	/**
	 * 
	 * @param minLat
	 * @param maxLat
	 * @param minLon
	 * @param maxLon
	 */
	void getWays(double minLat, double maxLat, double minLon, double maxLon) {
		_exec.execute(new WayWorker(minLat, maxLat, minLon, maxLon));
		
	}
	
	/**
	 * 
	 * @author emc3
	 *
	 */
	private class WayWorker implements Runnable {
		private int _id;
		private double _minLat, _maxLat, _minLon, _maxLon;
		
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
			Response resp = new WayResponse(_owner._b.getWaysInRange(_minLat, _maxLat, _minLon, _maxLon));
			if (_id == _threadCount.get()) {
				_owner._responseQueue.add(resp);
			}
		}
	}
	
}