package frontend;

import maps.MapFactory;

/**
 * In case the server shuts down, this class can be used to keep the client
 * running until it comes back. It may also be used for supporting the map
 * when the server is running slowly, but repainting concurrency is an issue 
 * and we haven't had time to deal with it yet.
 * @author samkortchmar
 */
public class LocalRenderThread implements Runnable {
	
	private MapPane _map;
	private double _minLat, _maxLat, _minLon, _maxLon, _zoom;
	
	@SuppressWarnings("static-access")
	public LocalRenderThread(MapPane map, double minLat, double maxLat, double minLon, double maxLon) {
		_map = map;
		_minLat = minLat;
		_maxLat = maxLat;
		_minLon = maxLon;
		_maxLon = maxLon;
		_zoom = map.scale;
	}

	@Override
	public void run() {
		_map.renderWays(MapFactory.getLocalWaysInRange(_minLat, _maxLat, _minLon, _maxLon, _zoom));
	}
}