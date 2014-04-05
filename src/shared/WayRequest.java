package shared;

public class WayRequest implements Request {
	
	private double minLat, maxLat, minLong, maxLong = -1;
	boolean filled = false;
	
	WayRequest(double minLat, double maxLat, double minLong, double maxLong) {
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.minLong = minLong;
		this.maxLong = maxLong;
	}

	@Override
	public RequestType getType() {
		return RequestType.WAYS;
	}

	double getMinLat() {
		return minLat;
	}
	double getMaxLat() {
		return maxLat;
	}
	double getMinLong() {
		return minLong;
	}
	double getMaxLong() {
		return minLong;
	}
}
