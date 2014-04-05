package shared;

public class WayRequest implements Request {
	
	public final double _minLat, _maxLat, _minLon, _maxLon;
	boolean filled = false;
	
	WayRequest(double minLat, double maxLat, double minLong, double maxLong) {
		_minLat = minLat;
		_maxLat = maxLat;
		_minLon = minLong;
		_maxLon = maxLong;
	}

	@Override
	public RequestType getType() {
		return RequestType.WAYS;
	}

}
