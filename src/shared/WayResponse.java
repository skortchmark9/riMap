package shared;

import java.util.List;

import maps.Way;

/**
 * A Response to a client's WayRequest - contains a list of all ways in the
 * requested Range.
 * @author samkortchmar
 *
 */
public class WayResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	private List<Way> _ways;
	
	public WayResponse(List<Way> ways) {
		_ways = ways;
	}
	@Override
	public ResponseType getType() {
		return ResponseType.WAYS;
	}
	public List<Way> getWays() {
		return _ways;
	}
	
	@Override
	public String toString() {
		return String.format("WayResponse - _ways = %s", _ways);
	}
}
