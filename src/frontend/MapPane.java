package frontend;
import edu.brown.cs032.emc3.kdtree.KDStub;
import graph.PathNode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import backend.Backend;
import backend.Constants;
import backend.Util;
import maps.MapFactory;
import maps.Node;
import maps.Way;

public class MapPane extends JPanel implements MouseWheelListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static double scale = 1.0;
	private static final int PIXEL_WIDTH = 700;
	private static final int PIXEL_HEIGHT = 700;
	//private List<Node> startLocs;
	//private List<Node> endLocs;
	private List<Way> renderedWays;
	private ClickNeighbor source;
	private ClickNeighbor target;
	private Backend b;
	private boolean clickSwitch = true;
	 


	MapPane(Backend b)   {
		this.b = b;
		this.setBackground(Color.black);
		this.setPreferredSize(new Dimension(PIXEL_WIDTH, PIXEL_HEIGHT));
		this.setMaximumSize(getPreferredSize());
		this.setFocusable(true);
		
		Corners.reposition(Constants.INITIAL_LAT, Constants.INITIAL_LON); //init to home depot (lol)
		Util.out("Corners:");
		Util.out("\tTop Left:", "("+Corners.topLeft[0]+",", Corners.topLeft[1]+")");
		Util.out("\tTop Right:", "("+Corners.topRight[0]+",", Corners.topRight[1]+")");
		Util.out("\tBottom Right:", "("+Corners.bottomRight[0]+",", Corners.bottomRight[1]+")");
		Util.out("\tBottom Left:", "("+Corners.bottomLeft[0]+",", Corners.bottomLeft[1]+")");
		initInteraction(); //initializes all interactions for the map view.
		
		//new synchronous list for all ways in viewport (ways we need to render)
		//renderedWays = Collections.synchronizedList(MapFactory.getWaysInRange(0, 0, 0, 0));
<<<<<<< HEAD
		renderedWays = b.getWaysInRange(Corners.topLeft[0], Corners.bottomLeft[0], Corners.topLeft[1], Corners.topRight[1]);
=======
		source = null;
		target = null;
		renderedWays = b.getWaysInRange(Corners.bottomLeft[0], Corners.topLeft[0], Corners.topLeft[1], Corners.topRight[1]);
>>>>>>> 8b4d12956faa669169cdb37502e39659522567e5
		if (Constants.DEBUG_MODE) {
			Util.out("Finished - Got All Ways in range", "(Elapsed:", Util.lap() +")");
		}
		this.repaint(); //paint the initial set of ways
		this.requestFocusInWindow();
		
		if (Constants.DEBUG_MODE)
			Util.memLog();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		//Render Ways
		g2d.setColor(Color.WHITE);
		for (Way way : renderedWays) {
			if (way != null) {
				int[] start = geo2pixel(way.getStart().getCoordinates());
				int[] end = geo2pixel(way.getTarget().getValue().getCoordinates());
				
				if (Constants.DEBUG_MODE) {
					//Util.out("Start point:", "("+start[0]+",", start[1]+")");
					//Util.out("End point:", "("+end[0]+",", end[1]+")");
				}
				
				g2d.drawLine(start[0], start[1], end[0], end[1]);
			}
		}
		
		//Render Click Points
		if (source != null) {
			
			if (Constants.DEBUG_MODE)
				Util.out("Point A:", "("+source.screenCoords[0]+",", source.screenCoords[1]+")");
			
			g2d.setColor(Color.GREEN);
			g2d.drawOval(source.screenCoords[0] - 5, source.screenCoords[1] - 5, 10, 10);
		}
		if (target != null) {
			
			if (Constants.DEBUG_MODE)
				Util.out("Point B:", "("+target.screenCoords[0]+",", target.screenCoords[1]+")");
			
			g2d.setColor(Color.RED);
			g2d.drawOval(target.screenCoords[0] - 5, target.screenCoords[1] - 5, 10, 10);
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
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "minus");

		//key binding for zoom in
		am.put("plus", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				zoomIn();
			}

		});
		
		//key binding for zoom out
		am.put("minus", new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				zoomOut();
			}
		});
		
	    //key binding for zooming with scroll wheel
	    addMouseWheelListener(this);

		//key binding for panning with click-n-drag.
	    MouseAdapter handler = new MouseHandler();
	    addMouseMotionListener(handler); //tell handler to track dragging
	    addMouseListener(handler); //tell handler to track clicks
	}
	
	/**
	 * Zooms the map view out (unless we are at min zoom)
	 */
	private void zoomOut() {
		if (scale > Constants.MIN_ZOOM + 0.1) {
			double geoWidth = Constants.GEO_DIMENSION_FACTOR / scale;
			scale -= 0.1;
			double newGeoWidth = Constants.GEO_DIMENSION_FACTOR / scale;
			double pageDiff = (newGeoWidth-geoWidth)/2;
			double newLat = Corners.topLeft[0] + pageDiff;
			double newLon = Corners.topLeft[1] - pageDiff;
			Corners.reposition(newLat, newLon);
			
			if (source != null)
				//TODO: reposition the source thingy
			if (target != null) {
				//TODO: reposition the target thingy
			}
			
		}
		//TODO: zoom out 0.1
	}
	
	/**
	 * Zooms the map view in
	 */
	private void zoomIn() {
		//TODO: zoom in  0.1
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Mouse handler class to handle click & drag events
	 * @author emc3
	 */
	private class MouseHandler extends MouseAdapter {
		
		@Override
		public void mouseDragged(MouseEvent e) {
			//TODO: pan map
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if (Constants.DEBUG_MODE)
				Util.out("Click registered!");
			
			if (clickSwitch) {
				source = new ClickNeighbor(e.getX(), e.getY());
				clickSwitch = false;
			}
			
			else {
				target = new ClickNeighbor(e.getX(), e.getY());
				clickSwitch = true;
			}
			repaint();
		}
		
	}


	void parseWay(String way) {
		String[] locs = Constants.tab.split(way);
//		startLocs.add(new PathNode(Double.parseDouble(locs[0]), Double.parseDouble(locs[1])));
//		endLocs.add(new PathNode(Double.parseDouble(locs[2]), Double.parseDouble(locs[3])));
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
			topLeft[0] = lat;
			topLeft[1] = lon;
			
			double width = Constants.GEO_DIMENSION_FACTOR/scale;
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
	 * were created by clicking the map.
	 */
	void clearClickPoints() {
		source = null;
		target = null;
		this.repaint();
	}
	
	/**
	 * Private class representing the point a user clicks on
	 * a screen.
	 * @author emc3
	 *
	 */
	private class ClickNeighbor {
		private Node node = null;
		private int[] screenCoords = null;
		
		/**
		 * Default constructor<br> 
		 * First takes in the parametric coordinates representing 
		 * a point on the screen and converts it to a  lat-lon pair.
		 * Then it queries the backend for a nearest neighbor node.
		 * It stores this node in its node field and also keeps track of 
		 * the screen coordinates of that node for painting. 
		 * 
		 * @param x - the x coordinate of the click on the screen.
		 * @param y - the y coordinate of the click on the screen.
		 */
		private ClickNeighbor(int x, int y) {
			double[] geoCoords = pixel2geo(x,y);
			KDStub p = new KDStub(geoCoords[0], geoCoords[1]);
			node = b.getNearestNeighbors(1, p).get(0);
			screenCoords = geo2pixel(node.getCoordinates());
		}
		
	}

}

