package shared;

import java.util.List;

import maps.Way;

public class WayResponse implements Response {
	
	List<Way> _ways;
	
	public WayResponse(List<Way> ways) {
		_ways = ways;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.WAYS;
	}
	
	List<Way> getWays() {
		return _ways;
	}
	
}
