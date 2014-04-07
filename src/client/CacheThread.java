package client;

import java.util.List;

import maps.MapFactory;
import maps.Way;

public class CacheThread implements Runnable {
	
	List<Way> _ways;
	
	CacheThread(List<Way> ways) {
		_ways = ways;
	}

	@Override
	public void run() {
		for (Way way : _ways) {
			MapFactory.cacheWay(way);
		}
	}
}
