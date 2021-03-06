package backend;

import java.awt.Color;
import java.util.regex.Pattern;

import autocorrect.Generator;
import autocorrect.Ranker;

public class Constants {
	
	//XXX DEBUG MODE
	public static final boolean DEBUG_MODE = false; //XXX TURN DIS OFF BEFORE HANDIN / SYSTEM TESTS
	//XXX DEBUG MODE
	
	//Precompiled regex patterns to make split and replace faster. 
	public static final Pattern tab = Pattern.compile("\t");
	public static final Pattern comma = Pattern.compile(",");
	public static final Pattern digits = Pattern.compile("\\D");
	public static final Pattern quotes = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
	public static final Pattern spaces = Pattern.compile("\\s+");
	public static final Pattern period = Pattern.compile(".");
	
	
	public static final Generator defaultGenerator = 
			new Generator(2, Generator.SuggestType.PREFIX, Generator.SuggestType.WHITESPACE);
	
	public static final Ranker defaultRanker = new Ranker(Ranker.CompareMode.MAPS);
	
	//Info for rendering lines on map;	
	public static final double INITIAL_LAT =  41.837323; //Start @ Brown's campus
	public static final double INITIAL_LON = -71.420103;
	public static final double GEO_DIMENSION_FACTOR = 0.02;
	public static final double MIN_ZOOM = 0.05; //x5 times more zoom out (from original submission)
	public static final double MAX_ZOOM = 12.0;
	public static final double ZOOM_FILTERING_THRESHOLD = 1;
	public static final double ARROW_KEY_MOVEMENT = 0.04d;
	
	//Some colors
	public static final Color MIDNIGHT = new Color(60, 62, 67);
	public static final Color GLOW_IN_THE_DARK = new Color(236, 240, 230);
	public static final Color BG_COLOR = new Color(0,17,21);
	public static final Color FG_COLOR = new Color(252, 251, 248);
	//For indicating traffic
	public static final Color LOW_TRAFFIC	= new Color(233, 255, 0);
	public static final Color HIGH_TRAFFIC	= new Color(255, 25, 0);
	public static final Color MED_TRAFFIC	= new Color(255, 162, 0);
	
	//Boundary lines on map
	public static double MINIMUM_LATITUDE = 40.1581762;
	public static double MAXIMUM_LATITUDE = 42.0952906;
	public static double MINIMUM_LONGITUDE = -73.7485663;
	public static double MAXIMUM_LONGITUDE =  -70.5590942;
	
	public static final int DEFAULT_REQUEST_TIMEOUT = 5;
	
	public static final int BufferLength = 1024;
	public static final int TabBufferLength = 256;
	
	/**
	 * For threaded file reading into KD Tree
	 */
	public static final int numThreads = 10;
	
	//For threading "Waygetters" (executor pool) in MapPane
	public static final int THREADPOOL_CORE_SIZE = 4;
	public static final int THREADPOOL_MAX_SIZE = 4;
	public static final long THREADPOOL_TIMEOUT = 3L; //time threads will wait for new work before suicide (seconds)
	
	/* Port numbers -- Change if they are in use */
	public static final int DEFAULT_SERVER_PORT = 9885;
	public static final int DEFAULT_TRAFFIC_PORT = 9888;

}
