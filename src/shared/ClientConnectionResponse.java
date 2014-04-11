/**
 * 
 */
package shared;

import java.util.List;
import java.util.Map;

import maps.Way;

/**
 * @author emc3
 * Sends initial information to client upon connection - the current traffic hashmap especially
 * Note that a more detailed understanding of Responses can be found in the Response interface
 */
public class ClientConnectionResponse implements Response {
	private static final long serialVersionUID = 1L;
	private final List<Way> _ways;
	private final Map<String, Double> _trafficMap;
	private final double _minLat, _maxLat, _minLon, _maxLon;

	/**
	 * @param mAXIMUM_LONGITUDE 
	 * @param mINIMUM_LONGITUDE 
	 * @param mAXIMUM_LATITUDE 
	 * @param mINIMUM_LATITUDE 
	 * @param map 
	 * @param list 
	 * 
	 */
	public ClientConnectionResponse(List<Way> ways, Map<String, Double> trafficMap, double minLat, double maxLat, double minLon, double maxLon) {
		_ways = ways;
		_trafficMap = trafficMap;
		_minLat = minLat; _maxLat = maxLat;
		_minLon = minLon; _maxLon = maxLon;
	}
	
	@Override
	public ResponseType getType() {
		return ResponseType.CLIENT_CONNECT;
	}

	public List<Way> getWays() {
		return _ways;
	}

	public Map<String, Double> getTrafficMap() {
		return _trafficMap;
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
}
