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
	private Executor _box1_exec, _box2_exec, _box3_exec, _box4_exec;
	
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
		
		_box1_exec = Util.defaultThreadPool("Box 1", 2, 4);
		_box2_exec = Util.defaultThreadPool("Box 2" ,2, 4);
		_box3_exec = Util.defaultThreadPool("Box 3", 2, 4);
		_box4_exec = Util.defaultThreadPool("Box 4", 2, 4);
	}
	
	/**
	 * 
	 * @param input
	 * @param boxNum
	 */
	void suggestFor(String input, int boxNum) {
		switch (boxNum) {
		case 1:
			_box1_exec.execute(new SuggestionWorker(input, boxNum));
			break;
		case 2:
			_box2_exec.execute(new SuggestionWorker(input, boxNum));
			break;
		case 3:
			_box3_exec.execute(new SuggestionWorker(input, boxNum));
			break;
		case 4:
			_box4_exec.execute(new SuggestionWorker(input, boxNum));
			break;
		}
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