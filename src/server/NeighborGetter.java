package server;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import backend.Constants;
import backend.Util;
import kdtree.KDimensionable;
import shared.NeighborsResponse;
import shared.Response;

/**
 * 
 * @author emc3
 *
 */
class NeighborGetter {
	private final ClientHandler _owner;
	AtomicInteger _threadCount;
	private Executor _exec;
	
	/**
	 * 
	 * @param owner
	 */
	NeighborGetter(ClientHandler owner) {
		_owner = owner;
		_threadCount = new AtomicInteger(0);
		_exec = Util.defaultThreadPool(Constants.THREADPOOL_CORE_SIZE, Constants.THREADPOOL_MAX_SIZE);
	}
	
	
	/**
	 * 
	 * @param num
	 * @param loc
	 */
	void getNeighbors(int num, KDimensionable loc) {
		_exec.execute(new NeighborWorker(num, loc));
	}
	
	
	/**
	 * 
	 * @author emc3
	 *
	 */
	private class NeighborWorker implements Runnable {
		int _id, _numNeighbors;
		KDimensionable _location;
		
		/**
		 * 
		 * @param num
		 * @param loc
		 */
		public NeighborWorker(int num, KDimensionable loc) {
			_numNeighbors = num;
			_location = loc;
		}
		
		@Override
		public void run() {
			_id = _threadCount.incrementAndGet();
			Response resp = new NeighborsResponse(_owner._b.getNearestNeighbors(_numNeighbors, _location));
			if (_id == _threadCount.get()) {
				_owner._responseQueue.add(resp);
			}
		}
		
	}
}