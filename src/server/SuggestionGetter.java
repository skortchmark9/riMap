package server;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import shared.AutocorrectResponse;
import shared.Response;
import backend.Util;

/**
 * Abtracts the suggestion getters for all 4 boxes, executes
 * each box's requests seperately.
 * @author emc3
 *
 */
class SuggestionGetter {
	private ClientHandler _owner;
	private AtomicInteger _box1_threadCount, _box2_threadCount, _box3_threadCount, _box4_threadCount;
	private Executor _exec;
	
	/**
	 * 
	 * @param owner
	 */
	SuggestionGetter(ClientHandler owner) {
		_owner = owner;
		
		_box1_threadCount = new AtomicInteger(0);
		_box2_threadCount = new AtomicInteger(0);
		_box3_threadCount = new AtomicInteger(0);
		_box4_threadCount = new AtomicInteger(0);
		
		_exec = Util.defaultThreadPool("Autocorrection Executor", 1, 4);
	}
	
	/**
	 * 
	 * @param input
	 * @param boxNum
	 */
	void suggestFor(String input, int boxNum) {		
		_exec.execute(new SuggestionWorker(input, boxNum));
	}
	
	/**
	 * 
	 * @author emc3
	 *
	 */
	private class SuggestionWorker implements Runnable {
		int _id, _boxNum;
		String _input;
		
		/**
		 * 
		 * @param input
		 * @param boxNum
		 */
		private SuggestionWorker(String input, int boxNum) {
			_input = input;
			_boxNum = boxNum;
		}
		
		
		@Override
		public void run() {
			
			//use each box's separate thread count to get & check ID 
			Response resp;
			switch (_boxNum) {
			case 1:
				_id = _box1_threadCount.incrementAndGet();
				resp = new AutocorrectResponse(_owner._b.getAutoCorrections(_input), _boxNum); 
				if (_id == _box1_threadCount.get())
					_owner._responseQueue.add(resp);
				break;
			case 2:
				_id = _box2_threadCount.incrementAndGet();
				resp = new AutocorrectResponse(_owner._b.getAutoCorrections(_input), _boxNum); 
				if (_id == _box2_threadCount.get())
					_owner._responseQueue.add(resp);
				break;
			case 3:
				_id = _box3_threadCount.incrementAndGet();
				resp = new AutocorrectResponse(_owner._b.getAutoCorrections(_input), _boxNum); 
				if (_id == _box3_threadCount.get())
					_owner._responseQueue.add(resp);
				break;
			case 4:
				_id = _box4_threadCount.incrementAndGet();
				resp = new AutocorrectResponse(_owner._b.getAutoCorrections(_input), _boxNum); 
				if (_id == _box4_threadCount.get())
					_owner._responseQueue.add(resp);
				break;
			}
		}
		
	}
	
}