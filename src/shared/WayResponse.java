package shared;

import java.util.List;

import maps.Way;

/**
 * A Response to a client's WayRequest - contains a list of all ways in the
 * requested Range.
 * @author samkortchmar
 * Note that a more detailed understanding of Responses can be found in the Response interface
 */
public class WayResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	private final List<Way> _ways;
	private final double _minLat, _maxLat, _minLon, _maxLon;
	
	public WayResponse(List<Way> ways, double minLat, double maxLat, double minLon, double maxLon) {
		_ways = ways;
		_minLat = minLat;
		_maxLat = maxLat;
		_minLon = minLon;
		_maxLon = maxLon;
	}
	
	
	@Override
	public ResponseType getType() {
		return ResponseType.WAYS;
	}
	public List<Way> getWays() {
		return _ways;
	}
	
	/**
	 * returns the corners in which all accompanying ways are defined
	 * returned array is ordered: <br> 
	 * <strong>
	 * {minLat, maxLat, minLon, maxLon}
	 * </strong>
	 * @return
	 */
	public double[] getMinMaxLatLon() {
		return new double[]{_minLat, _maxLat, _minLon, _maxLon};
	}
	
	@Override
	public String toString() {
		return String.format("WayResponse - _ways = %s", _ways);
	}
}
