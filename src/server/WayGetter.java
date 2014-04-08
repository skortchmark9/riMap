package server;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
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
	private ThreadPoolExecutor _exec, _waitExec;
	Future<List<Way>> _future;
	private AtomicInteger _waitThreadCount;
	
	/**
	 * @param owner
	 */
	public WayGetter(ClientHandler owner) {
		_owner = owner;
		_exec = Util.defaultThreadPool(1, 1);
		_waitExec = Util.defaultThreadPool(1, 1);
		_waitThreadCount = new AtomicInteger(0);
	}
	
	/**
	 * 
	 * @param minLat
	 * @param maxLat
	 * @param minLon
	 * @param maxLon
	 */
	void getWays(double minLat, double maxLat, double minLon, double maxLon) {
		if (_future != null) {
			_future.cancel(true); //cancel future and interrupt routine
			_exec.purge();
			_waitExec.purge();
		}
		
		_future = _exec.submit(new WayWorker(minLat, maxLat, minLon, maxLon));
		_waitExec.execute(new WayWaiter());
	}
	
	/**
	 * 
	 * @author emc3
	 *
	 */
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
	
	
	/**
	 * A runnable which waits for the future to return.
	 * If the future is interrupted, the thread simply quits.
	 *  
	 * @author emc3
	 */
	private class WayWaiter implements Runnable {
		int _id;
		
		WayWaiter() {
			_id = _waitThreadCount.incrementAndGet();
		}
		
		@Override
		public void run() {
			if (_future != null) {
				try {
					List<Way> ways = _future.get();
					if (_id == _waitThreadCount.get())
						_owner._responseQueue.add(new WayResponse(ways));
				} catch (InterruptedException | CancellationException | ExecutionException e) {
					//simply continue
				}
				_future = null;
			}
		}
		
	}
	
}