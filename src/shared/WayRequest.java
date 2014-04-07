package shared;

/**
 * A Request for ways within a 2d range.
 * @author samkortchmar
 *
 */
public class WayRequest implements Request {
	
	private static final long serialVersionUID = 1L;
	private final double _minLat, _maxLat, _minLon, _maxLon;
	
	public WayRequest(double minLat, double maxLat, double minLong, double maxLong) {
		_minLat = minLat;
		_maxLat = maxLat;
		_minLon = minLong;
		_maxLon = maxLong;
	}
	public double getMinLat() {
		return _minLat;
	}
	public double getMaxLat() {
		return _maxLat;
	}
	public double getMinLon() {
		return _minLon;
	}
	public double getMaxLon() {
		return _maxLon;
	}
	@Override
	public RequestType getType() {
		return RequestType.WAYS;
	}
	
	@Override
	public String toString() {
		return String.format("WayReq - minLat = %s maxLat = %s minLon = %s maxLon = %s", _minLat, _maxLat, _minLon, _maxLon);
	}
}
