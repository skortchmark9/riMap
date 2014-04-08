package shared;

import java.util.Map;

public class TrafficResponse implements Response {
	private static final long serialVersionUID = 1L;
	Map<String, Double> _trafficMap;

	public TrafficResponse(Map<String, Double> trafficMap) {
		_trafficMap = trafficMap;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.TRAFFIC;
	}
	
	public Map<String, Double> getTraffic() {
		return _trafficMap;
	}

}
