package shared;

public class WayRequest implements Request {
	
	private final double _minLat, _maxLat, _minLon, _maxLon;
	
	WayRequest(double minLat, double maxLat, double minLong, double maxLong) {
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

}
