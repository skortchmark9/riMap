package client;

import java.util.LinkedList;
import java.util.List;

import maps.MapFactory;
import maps.Way;
import backend.Util;

public class CacheThread implements Runnable {
	
	List<Way> _ways;
	boolean _isPath;
	
	
	CacheThread(List<Way> ways, boolean isPath) {
		_ways = ways;
	}

	@Override
	public void run() {
		List<String> blockOfWayIDs = new LinkedList<>();
		String blockSearchCode = "";
		for (Way way : _ways) {
			MapFactory.cacheWay(way);
			if (_isPath) //If it's a path, there is no ordering to the information so we don't need to store it.
				continue;
			String uniqueID = way.getUniqueID(); //Otherwise, it is probably ordered by searchcode - making it
			String waySearchCode = uniqueID.substring(0, 12); //A perfect fit for our map factory wayArray.
			if (waySearchCode.equals(blockSearchCode)) { //We can easily determine the searchcodes that define
				blockOfWayIDs.add(uniqueID); //certain "blocks" of ways in a given latitude/longitude.
				continue;
			}
			else {
				MapFactory.cacheBlock(blockSearchCode, blockOfWayIDs);
				blockSearchCode = waySearchCode;
			}
		}
	}
}
