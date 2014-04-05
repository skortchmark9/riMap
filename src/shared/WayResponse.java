package shared;

import java.util.List;

import maps.Way;

public class WayResponse implements Response {
	
	List<Way> ways;

	@Override
	public ResponseType getType() {
		return ResponseType.WAYS;
	}
	
	List<Way> getWays() {
		return ways;
	}
	
}
