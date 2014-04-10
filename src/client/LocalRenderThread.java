package client;

import maps.MapFactory;
import frontend.Frontend;

public class LocalRenderThread implements Runnable {
	
	private Frontend _frontend;
	private double _minLat, _maxLat, _minLon, _maxLon;
	
	LocalRenderThread(Frontend frontend, double minLat, double maxLat, double minLon, double maxLon) {
		_frontend = frontend;
		_minLat = minLat;
		_maxLat = maxLat;
		_minLon = maxLon;
		_maxLon = maxLon;
	}

	@Override
	public void run() {
		_frontend.addWays(MapFactory.getLocalWaysInRange(_minLat, _maxLat, _minLon, _maxLon));
	}

}
