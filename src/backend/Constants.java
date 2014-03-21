package backend;

import java.awt.Color;
import java.util.regex.Pattern;

import autocorrect.Generator;
import autocorrect.Ranker;

public class Constants {
	
	//XXX DEBUG MODE
	public static final boolean DEBUG_MODE = false; //XXX TURN THIS OFF BEFORE HANDIN / SYSTEM TESTS
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
	
	//Info for rendering lines on map:
	public static final double INITIAL_LAT = 41.842678; //Home Depot
	public static final double INITIAL_LON = -71.417541;
	public static final double MINIMUM_LATITUDE = 40.1581762;
	public static final double MAXIMUM_LATITUDE = 42.0952906;
	public static final double MINIMUM_LONGITUDE = -73.7485663;
	public static final double MAXIMUM_LONGITUDE =  -70.5590942;
	public static final double GEO_DIMENSION_FACTOR = 0.02;
	public static final double MIN_ZOOM = 0.2;
	public static final double MAX_ZOOM = 12.0;
	public static final Color MIDNIGHT = new Color(60, 62, 67);
	public static final Color GLOW_IN_THE_DARK = new Color(236, 240, 230);
	
	public static final int BufferLength = 520;
	public static final int TabBufferLength = 256;

	/**
	 * For threaded file reading into KD Tree
	 */
	public static final int numThreads = 10;
}
