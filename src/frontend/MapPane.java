package frontend;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import kdtree.KDStub;
import maps.Node;
import maps.Way;
import backend.Constants;
import backend.Util;
import client.Client;

public class MapPane extends JPanel implements MouseWheelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static double scale = 1.0;
	private static final int PIXEL_WIDTH = 700;
	private static final int PIXEL_HEIGHT = 700;
	private List<Way> renderedWays, calculatedRoute;
	private ClickNeighbor _source;
	private ClickNeighbor _dest;
	private Client _client;
	private boolean clickSwitch = true;
	//private ExecutorService executor;
	private Frontend _front;
	
	/**
	 * Construct a MapPane linked to the given backend.
	 * The MapPane is responsible for drawing ways as 
	 * well as zooming / panning over the map.
	 * @param b - the backend to link to this MapPane.
	 */
	MapPane(Frontend front, Client client)   {
		_client = client;
		_front = front; //need this for updated text fields on clicks, resizing map
		this.setBackground(Constants.BG_COLOR);
		this.setPreferredSize(new Dimension(PIXEL_WIDTH, PIXEL_HEIGHT));
		this.setMaximumSize(getPreferredSize());
		this.setFocusable(true);
		
		Corners.reposition(Constants.INITIAL_LAT, Constants.INITIAL_LON); //init to home depot (lol)
		
		if (Constants.DEBUG_MODE) {
			Util.out("Corners:");
			Util.out("\tTop Left:", "("+Corners.topLeft[0]+",", Corners.topLeft[1]+")");
			Util.out("\tTop Right:", "("+Corners.topRight[0]+",", Corners.topRight[1]+")");
			Util.out("\tBottom Right:", "("+Corners.bottomRight[0]+",", Corners.bottomRight[1]+")");
			Util.out("\tBottom Left:", "("+Corners.bottomLeft[0]+",", Corners.bottomLeft[1]+")");
		}
		
		initInteraction(); //initializes all interactions for the map view.
		//new synchronous list for all ways in viewport (ways we need to render)
		//renderedWays = Collections.synchronizedList(MapFactory.getWaysInRange(0, 0, 0, 0));
		_source = null;
		_dest = null;
		
		//set up & request new ways
		renderedWays = new LinkedList<>();
		_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
		
		calculatedRoute = new LinkedList<>();
		
		this.repaint(); //paint the initial set of ways
		this.requestFocusInWindow();
		
		if (Constants.DEBUG_MODE)
			Util.memLog();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		//===================================
		//Render Ways
		//===================================
		g2d.setColor(Constants.FG_COLOR);
		g2d.setStroke(new BasicStroke(1));
		for (Way way : renderedWays) {
			if (way != null) {
				int[] start = geo2pixel(way.getStart().getCoordinates());
				int[] end = geo2pixel(way.getTarget().getCoordinates());
				
				//If the road is less than Constants.MIN_RENDER_LENGTH, don't paint it.
				if (Point.distance(start[0], start[1], end[0], end[1]) < Constants.MIN_RENDER_LENGTH)
					continue;
				
				g2d.drawLine(start[0], start[1], end[0], end[1]);
			}
		}
		
		//render calculated route
		g2d.setColor(Color.BLUE);
		g2d.setStroke(new BasicStroke(3));		
		for (Way way : calculatedRoute) {
			if (way != null) {
				int[] start = geo2pixel(way.getStart().getCoordinates());
				int[] end = geo2pixel(way.getTarget().getCoordinates());
				
				if (Constants.DEBUG_MODE) {
					//Util.out("Start point:", "("+start[0]+",", start[1]+")");
					//Util.out("End point:", "("+end[0]+",", end[1]+")");
				}
				g2d.drawLine(start[0], start[1], end[0], end[1]);
			}
		}
		
		//Render Click Points
		if (_source != null) {
			
			if (Constants.DEBUG_MODE) {
				//Util.out("Source pixel coords:", "("+source.screenCoords[0]+",", source.screenCoords[1]+")");
				Util.out("Source Node:", _source.node.toString());
			}
			g2d.setStroke(new BasicStroke(1));		
			g2d.setColor(Color.GREEN);
			g2d.drawOval(_source.screenCoords[0] - 5, _source.screenCoords[1] - 5, 10, 10);
		}
		if (_dest != null) {
			
			if (Constants.DEBUG_MODE) {
				//Util.out("Target pixel coords:", "("+target.screenCoords[0]+",", target.screenCoords[1]+")");
				Util.out("Target Node", _dest.node.toString());
			}
			g2d.setStroke(new BasicStroke(1));		
			g2d.setColor(Color.RED);
			g2d.drawOval(_dest.screenCoords[0] - 5, _dest.screenCoords[1] - 5, 10, 10);
		}
		
		//render boundaries if in range
		if (Util.boundariesInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1])) {
			
			if (Constants.DEBUG_MODE)
				Util.out("Painting Boundaries");
			
			int topLeft[] = geo2pixel(new double[]{Constants.MAXIMUM_LATITUDE, Constants.MINIMUM_LONGITUDE}); //top left boundary corner
			int topRight[] = geo2pixel(new double[]{Constants.MAXIMUM_LATITUDE, Constants.MAXIMUM_LONGITUDE}); //top right boundary corner
			int bottomRight[] = geo2pixel(new double[]{Constants.MINIMUM_LATITUDE, Constants.MAXIMUM_LONGITUDE});
			int bottomLeft[] = geo2pixel(new double[]{Constants.MINIMUM_LATITUDE, Constants.MINIMUM_LONGITUDE});
			g2d.setColor(Color.ORANGE);
			g2d.drawLine(topLeft[0], topLeft[1], topRight[0], topRight[1]); //top boundary
			g2d.drawLine(topRight[0], topRight[1], bottomRight[0], bottomRight[1]); //right boundary
			g2d.drawLine(bottomLeft[0], bottomLeft[1], bottomRight[0], bottomRight[1]); //bottom boundary
			g2d.drawLine(topLeft[0], topLeft[1], bottomLeft[0], bottomLeft[1]); //leftboundary
		}
	}
	
	/**
	 * Converts Latitude & Longitudes to screen coordinates
	 * based on the current viewport position (location and current zoom)
	 * 
	 * @param coordinates - a double[] of size 2, containing the 
	 * latitude & longitude of the point to convert.<br>
	 * Should be in the form <strong>{latitude, longitude}</strong>
	 * 
	 * @return
	 * an int[] of size 2, containing the newly calculated pixel coordinates,
	 * which can be used to accurately draw the point in the JPanel's paintComponent().
	 * the returned array will be in the form <strong>{x, y}</strong>
	 */
	private int[] geo2pixel(double[] coordinates) {
		double geoWidth = Constants.GEO_DIMENSION_FACTOR / scale; //geoWidth = current width of the view in terms of latitude/longitude degrees
		double[] offset = Corners.offsetFromTopLeft(coordinates); //offset = array of rise/run of latitude/longitude coordinates when compared to top left corner
		int x = (int) Math.round(offset[1]/geoWidth * PIXEL_WIDTH); //get ratio of longitude offset in view, and multiply that ratio by the pixel width of the view.
		int y = (int) Math.round((offset[0]/geoWidth) * PIXEL_HEIGHT); //get ratio of latitude offset in view, and multiply that ratio by the pixel height of the view.
		return new int[]{x, y};
	}
	
	/**
	 * The opposite of geo2pixel().<br>
	 * Converts screen coordinates to a latitude/longitude pair based on the 
	 * current viewport position (location & current zoom).
	 * 
	 * @param x - the x coordinate of the point on the screen
	 * @param y - the y coordinate of the point on the screen
	 *  
	 * @return
	 * a double[] of size 2 containing the newly calculated latitude and longitude 
	 * of the point in geographic space. Will be of the form <strong>{latitude, longitude}</strong> 
	 * 
	 */
	private double[] pixel2geo(int x, int y) {
		double geoWidth = Constants.GEO_DIMENSION_FACTOR / scale; //the current width of the view in terms of latitude/longitude degrees.
		double lonOffset = ((double)x) / ((double)PIXEL_WIDTH) * geoWidth; //the number of degrees of the longitude offset of the point from the left of the view.
		double latOffset = ((double)y) / ((double)PIXEL_WIDTH) * geoWidth; //the number of degrees of the latitude offset of the point from the top of the view.
		double lat = Corners.topLeft[0] - latOffset;
		double lon = Corners.topLeft[1] + lonOffset;
		return new double[]{lat,lon};
	}
	
	
	/**
	 * Sets the calculated route to be drawn over the map.
	 * @param route - the list of ways to be drawn.
	 */
	public void setCalculatedRoute(List<Way> route) {
		calculatedRoute = route;
	}
	
	/**
	 * Clear the currently painted "shortest route"
	 * (the route referred to is the route found by Dijkstra's)
	 */
	public void clearRoute() {
		calculatedRoute = new LinkedList<>();
	}
	
	
	/**
	 * Converts a pixel length to a geo length
	 * @param pixelOffset - the length of the offset in pixels
	 * @return
	 * the length of the offset in lat/lon degrees
	 */
	public double pixelOffset2geoOffset(int pixelOffset) {
			double pixelRatio = ((double) pixelOffset) / ((double) PIXEL_WIDTH);
			double geoWidth = Constants.GEO_DIMENSION_FACTOR / scale;
			double geoLen  = pixelRatio * geoWidth;
			return geoLen;
					
			//return (((double)pixelOffset)/((double)PIXEL_WIDTH)) * (Constants.GEO_DIMENSION_FACTOR / scale);
	}

	/**
	 * Initializes the interactions for the map:
	 * <ul>
	 * <li>Zoom (+/=, -, scroll)</li>
	 * <li>Pan (mouse drag)</li>
	 * <li>Select Node (mouse click)</li>
	 * </ul>
	 */
	private void initInteraction() {
		InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = getActionMap();

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "plus");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK), "plus");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "plus");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "minus");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.SHIFT_DOWN_MASK), "minus");
		
		//directional arrow inputs:
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");

		//key binding for zoom in
		am.put("plus", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Constants.DEBUG_MODE)
					Util.out("Plus handler executing");
				zoomIn();
			}

		});
		
		//key binding for zoom out
		am.put("minus", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Constants.DEBUG_MODE)
					Util.out("Minus handler executing");
				zoomOut();
			}
		});
		
		//key binding to move map upwards
		am.put("up", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Constants.DEBUG_MODE)
					Util.out("key up handler executing");
				
				//move map up
				double newLat = Corners.topLeft[0] + 0.04; //TODO: 0.04 as a constant?
				recalibrateMap(newLat, Corners.topLeft[1]);

				//request new ways in range
				_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
				repaint();
			}
		});
		
		//key binding to move map right
		am.put("right", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Constants.DEBUG_MODE)
					Util.out("key right handler executing");

				//move map up
				double newLon = Corners.topLeft[1] + 0.04; //TODO: 0.04 as a constant?
				recalibrateMap(Corners.topLeft[0], newLon);
				
				//request new ways in range
				_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
				repaint();
			}
		});
		
		//key binding to move map downwards
		am.put("down", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Constants.DEBUG_MODE)
					Util.out("key down handler executing");

				//move map up
				double newLat = Corners.topLeft[0] - 0.04; //TODO: 0.04 as a constant?
				recalibrateMap(newLat, Corners.topLeft[1]);
				//request new ways in range
				_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
				repaint();
			}
		});
		
		//key binding to move map left
		am.put("left", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Constants.DEBUG_MODE)
					Util.out("key left handler executing");

				//move map up
				double newLon = Corners.topLeft[1] - 0.04; //TODO: 0.04 as a constant?
				recalibrateMap(Corners.topLeft[0], newLon);
				//request new ways in range
				_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
				repaint();
			}
		});
		
	    //key binding for zooming with scroll wheel
	    addMouseWheelListener(this);

		//key binding for panning with click-n-drag.
	    MouseAdapter handler = new MapMouseHandler();
	    addMouseMotionListener(handler); //tell handler to track dragging
	    addMouseListener(handler); //tell handler to track clicks
	}
	
	/**
	 * Zooms the map view out (unless we are at min zoom)
	 * Also launches a new thread to get the new ways to paint 
	 */
	private void zoomOut() {
		//only zoom out if we are before threshold
		if (scale > Constants.MIN_ZOOM) {
			double oldGeoWidth = Constants.GEO_DIMENSION_FACTOR / scale; //get the old width
			scale *= 0.8; //decrement scale
			if (Constants.DEBUG_MODE)
				Util.out("New Scale:", scale);
			double newGeoWidth = Constants.GEO_DIMENSION_FACTOR / scale; //get new width
			double viewDiff = (newGeoWidth-oldGeoWidth)/2; //find difference of each side of view in new width
			
			//calculate new anchor point (top left lat/lon)
			double newLat = Corners.topLeft[0] + viewDiff; 
			double newLon = Corners.topLeft[1] - viewDiff; 
			recalibrateMap(newLat, newLon); //reposition all corners with new coords
			
			//request all new ways in new range
			_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
			this.repaint();
		}
	}
	
	/**
	 * Zooms the map view in (unless we are at max zoom)
	 */
	private void zoomIn() {
		//only zoom in if we are before threshold
		if (scale < Constants.MAX_ZOOM) {
			double oldGeoWidth = Constants.GEO_DIMENSION_FACTOR / scale; //get the old width
			scale *= 1.2; //decrement scale
			if (Constants.DEBUG_MODE)
				Util.out("New Scale:", scale);
			double newGeoWidth = Constants.GEO_DIMENSION_FACTOR / scale; //get new width
			double viewDiff = (oldGeoWidth - newGeoWidth)/2; //find difference of each side of view in new width
			
			//calculate new anchor point (top left lat/lon)
			double newLat = Corners.topLeft[0] - viewDiff; 
			double newLon = Corners.topLeft[1] + viewDiff; 
			recalibrateMap(newLat, newLon); //reposition all corners with new coords
			//no new ways to get
			this.repaint(); 
		}
	}
	
	/**
	 * Reposition the corners of the map and recalibrate the _source and _dest
	 * 
	 * @param newLat - new position of top left corner of map
	 * @param newLon - new position of top left corner of map
	 */
	private void recalibrateMap(double newLat, double newLon) {
		//reposition all corners with new coords
		Corners.reposition(newLat, newLon);
		
		//re-calibrate clickPoints
		if (_source != null)
			_source.recalibrate();
		if (_dest != null)
			_dest.recalibrate();
	}
	
	/**
	 * Zooms the mouse in according to the current mouse position
	 * (unless we are at max zoom)
	 * @param mousePos - the mouse point to zoom in towards
	 */
	private void zoomIn(Point mousePos) {
		if (scale < Constants.MAX_ZOOM) {
			scale *= 1.1;
			
			if (Constants.DEBUG_MODE)
				Util.out("New Scale:", scale);
			
			int newX = (int)Math.ceil((mousePos.x*(1.1-1) - 1));
			int newY = (int)Math.ceil((mousePos.y*(1.1-1) - 1));
			
			double[] newAnchor = pixel2geo(newX, newY);
			Corners.reposition(newAnchor[0], newAnchor[1]); //reposition all corners with new coords

			//re-calibrate clickPoints
			if (_source != null)
				_source.recalibrate();
			if (_dest != null)
				_dest.recalibrate();

			//no new ways to get
			this.repaint(); 
		}
	}
	
	/**
	 * Zooms the map out according to the current mouse position 
	 * (unless we are at min zoom)
	 * @param mousePos - the mousePoint to zoom out from
	 */
	private void zoomOut(Point mousePos) {
		if (scale > Constants.MIN_ZOOM) {
			scale *= 0.9;
			if (Constants.DEBUG_MODE)
				Util.out("New Scale:", scale);
			
			int newX = (int)Math.ceil((mousePos.x*(0.9-1) - 1));
			int newY = (int)Math.ceil((mousePos.y*(0.9-1) - 1));
			double[] newAnchor = pixel2geo(newX, newY);
			Corners.reposition(newAnchor[0], newAnchor[1]); //reposition all corners with new coords
			
			//re-calibrate clickPoints
			if (_source != null)
				_source.recalibrate();
			if (_dest != null)
				_dest.recalibrate();
			
			this.repaint(); // repaint for responsiveness
			
			//request all new ways in new range
			_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
			this.repaint();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int scrollAmount = e.getWheelRotation();
		if (Constants.DEBUG_MODE)
			Util.out("MOUSEWHEEL AMOUNT:", scrollAmount);
		if (scrollAmount < 0) {
			zoomIn(e.getPoint());
		} else {
			zoomOut(e.getPoint());
		}
	}
	
	/**
	 * Mouse handler class to handle click & drag events
	 * @author emc3
	 */
	private class MapMouseHandler extends MouseAdapter {
		
		private Point startP; //start point of dragging interactions
		
		@Override
		public void mousePressed(MouseEvent e) {
			startP = e.getPoint();
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			Point p = e.getPoint();
			//calculate new anchor point (top left lat/lon)
			double newLat = Corners.topLeft[0] + pixelOffset2geoOffset(p.y - startP.y); 
			double newLon = Corners.topLeft[1] - pixelOffset2geoOffset(p.x - startP.x);
			
			Corners.reposition(newLat, newLon); //reposition all corners with new coords
			
			startP = e.getPoint(); //re-define start p
			
			//re-calibrate click points
			if (_source != null)
				_source.recalibrate();
			if (_dest != null)
				_dest.recalibrate();

			//request new ways in view
			_client.requestWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
			repaint();
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (Constants.DEBUG_MODE)
				Util.out("Click registered!");
			if (clickSwitch) {
				_source = new ClickNeighbor(e.getX(), e.getY());
				clickSwitch = false;
			} else {
				_dest = new ClickNeighbor(e.getX(), e.getY());
				clickSwitch = true;
			}
			clearRoute();
			repaint();
		}
	}
	
	
	/**
	 * @return
	 * the node stored at the green circle,
	 * or the "start" click point.
	 * This method may return null if no ClickNeighbor has
	 * been created for the start node (i.e. if the start node was cleared).
	 */
	public Node getStart() {
		return _source.node;
	}
	
	/**
	 * @return
	 * the node stored at the red circle,
	 * or the "end" click point.
	 * This method may return null if no ClickNeighbor has
	 * been created for the start node (i.e. if the start node was cleared).
	 */
	public Node getEnd() {
		return _dest.node;
	}
	
	/**
	 * @return
	 * if both clickpoints have been defined on the map, i.e. both start and end have 
	 * been created by clicking, this method returns true. false otherwise.
	 */
	public boolean hasPoints() {
		return _source != null && _dest != null;
	}
	
	
	
	public void setPoint(Node node, boolean isSource) {
		if (isSource) {
			_source = new ClickNeighbor(node);
		} else {
			_dest = new ClickNeighbor(node);
		}
	}
	
	/**
	 * Sets the click points on the map for painting
	 * @param start - the start node to paint (green circle)
	 * @param end - the end node to paint (red circle)
	 */
	public void setPoints(Node start, Node end) {
		_source = new ClickNeighbor(start);
		_dest = new ClickNeighbor(end);
		repaint();
	}



	/**
	 * A class containing the latitude and longitude coordinates
	 * of all 4 corners of the map view. Each corner is represented
	 * by a double[] of size 2 in the format 
	 * <strong>{latitude, longitude}</strong>.
	 * <p>
	 * These corners (obviously) must be changed each time 
	 * the map is repositioned. This is done with the 
	 * <code>repositon(lat, lon)</code> method.
	 * 
	 * @author emc3
	 *
	 */
	private static class Corners {
		private static double[] topLeft		= new double[2],
								topRight	= new double[2], 
								bottomLeft	= new double[2], 
								bottomRight = new double[2];
		
		/**
		 * Gives the top left corner a new location and
		 * repositions all corners based on the zoom scale.
		 * 
		 * @param lat - the new latitude of the top left corner
		 * @param lon - the new longitude of the top left corner.
		 */
		private static void reposition(double lat, double lon) {
			double width = Constants.GEO_DIMENSION_FACTOR/scale;
			
			//Check to make sure move is in bounds
			if (lat > Constants.MAXIMUM_LATITUDE + 0.002 || lat-width < Constants.MINIMUM_LATITUDE - 0.002 ||
					lon < Constants.MINIMUM_LONGITUDE - 0.002 || lon+width > Constants.MAXIMUM_LONGITUDE + 0.002) {
				return;
			}
			
			topLeft[0] = lat;
			topLeft[1] = lon;
			
			topRight[0] = topLeft[0]; //topRight has same latitude as topLeft
			topRight[1] = topLeft[1] + width; //longitude is topLeft's longitude + width 
			
			bottomLeft[0] = topLeft[0] - width; //bottomLeft latitude is topLeft latitude - width
			bottomLeft[1] = topLeft[1]; //longitude is same as topLeft
			
			bottomRight[0] = bottomLeft[0]; //bottomRight has same latitude as bottomLeft
			bottomRight[1] = topRight[1]; //bottomRight has same longitude as topRight
		}
		
		/**
		 * Returns a double[] of size 2 containing the difference 
		 * (difference in latitude, difference of longitude) of the 
		 * parameter coordinates to the top left of the rendered view.
		 * 
		 * @param coords - the coordinates to calculate distance to the top left of the rendered view
		 * @return
		 * a double[] of size 2 containing the difference: {latDiff, lonDiff}
		 */
		private static double[] offsetFromTopLeft(double coords[]) {
			return new double[]{topLeft[0] - coords[0], coords[1] - topLeft[1]};
		}
	}
	
	/**
	 * Clears the points on the screen which
	 * were created by clicking the map, then clears
	 * the route (if it's painted)
	 */
	void clearClickPoints() {
		_source = null;
		_dest = null;
		clickSwitch = true;
		clearRoute();
		this.repaint();
	}
	
	/**
	 * Private class representing the point a user clicks on
	 * a screen.
	 * @author emc3
	 *
	 */
	class ClickNeighbor {
		private Node node = null;
		private int[] screenCoords = null;
		
		/**
		 * Default constructor<br> 
		 * This takes in the parametric coordinates representing 
		 * a point on the screen and converts it to a  lat-lon pair.
		 * It then makes a new request for the nearest neighbor to
		 * that click via the client.
		 * 
		 * @param x - the x coordinate of the click on the screen.
		 * @param y - the y coordinate of the click on the screen.
		 */
		private ClickNeighbor(int x, int y) {
			double[] geoCoords = pixel2geo(x,y);
			KDStub p = new KDStub(geoCoords[0], geoCoords[1]);
			_client.requestNearestNeighbors(1, p, clickSwitch);
		}
		
		/**
		 * Construct a ClickNeighbor using an existing node.
		 * @param n - the existing node to wrap in this ClickNeighbor
		 */
		private ClickNeighbor(Node n) {
			this.node = n;
			screenCoords = geo2pixel(node.getCoordinates());
		}
		
		/**
		 * This method re-calibrates the screen coordinates 
		 * after any translation. basically sets up the click neighbor for repainting.
		 */
		private void recalibrate() {
			screenCoords = geo2pixel(node.getCoordinates()); 
		}
		
	}
}

