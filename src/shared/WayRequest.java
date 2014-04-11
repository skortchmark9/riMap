package shared;

/**
* A Request for ways within a 2d range.
* @author samkortchmar
* Note that more detailed comments are available in the Request Interface
*/
public class WayRequest implements Request {
	
	private static final long serialVersionUID = 1L;
	private final double _minLat, _maxLat, _minLon, _maxLon, _zoom;
	
	public WayRequest(double minLat, double maxLat, double minLong, double maxLong, double zoom) {
		_minLat = minLat;
		_maxLat = maxLat;
		_minLon = minLong;
		_maxLon = maxLong;
		_zoom = zoom;
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
	public double getZoom() {
		return _zoom;
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
