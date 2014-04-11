package server;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import kdtree.KDimensionable;
import maps.MapFactory;
import maps.Node;
import shared.NeighborsResponse;
import shared.Response;
import backend.Constants;
import backend.Util;

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
	 * @param owner - the clienthandler in charge of the neighbor getter,
	 * used for sending results of computation
	 */
	NeighborGetter(ClientHandler owner) {
		_owner = owner;
		_threadCount = new AtomicInteger(0);
		_exec = Util.defaultThreadPool("Neighbor Getter", Constants.THREADPOOL_CORE_SIZE, Constants.THREADPOOL_MAX_SIZE);
	}
	
	
	/**
	 * Gets neighbors
	 * @param num - the number of neighbors, usually 1
	 * @param loc - KDimensionable nearest to neighbors
	 */
	void getNeighbors(int num, KDimensionable loc, boolean isSource) {
		_exec.execute(new NeighborWorker(num, loc, isSource));
	}
	
	/**
	 * Thread for getting nearest neighbors without hanging up the server
	 */
	private class NeighborWorker implements Runnable {
		int _id, _numNeighbors;
		boolean _isSource;
		KDimensionable _location;
		
		/**
		 * 
		 * @param num
		 * @param loc
		 */
		public NeighborWorker(int num, KDimensionable loc, boolean isSource) {
			_numNeighbors = num;
			_location = loc;
			_isSource = isSource;
		}
		
		@Override
		public void run() {
			//The id is responsible for ensuring this is the most recent query thread
			_id = _threadCount.incrementAndGet();
			Node n = _owner._b.getNearestNeighbors(_numNeighbors, _location).get(0);
			List<String> intersectingStreets = MapFactory.getIntersectingStreets(n);
			Response resp = new NeighborsResponse(n, _isSource, intersectingStreets.get(0), intersectingStreets.get(1));
			if (_id == _threadCount.get()) {
				_owner._responseQueue.add(resp);
			}
		}
		
	}
}