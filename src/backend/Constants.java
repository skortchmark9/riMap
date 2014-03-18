package backend;

import java.util.regex.Pattern;

import autocorrect.Generator;
import autocorrect.Ranker;

public class Constants {
	
	//XXX DEBUG MODE
	public static final boolean DEBUG_MODE = true; //XXX TURN THIS OFF BEFORE HANDIN / SYSTEM TESTS
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
	
	public static final Ranker defaultRanker = new Ranker(Ranker.CompareMode.SMART);
	
	//Info for rendering lines on map:
	public static final double INITIAL_LAT = 41.842678; //Home Depot
	public static final double INITIAL_LON = -71.417541;
	public static final float GEO_DIMENSION_FACTOR = 0.02f;
	public static final float MIN_ZOOM = 0.1f;
	public static final float MAX_ZOOM = 2.0f;
	
	//A prime number!
	public static final int ADJUST_LINE_BUFFER = 4037;
	public static final int BufferLength = 512;
	public static final int TabBufferLength = 256;

	/**
	 * For threaded file reading into KD Tree
	 */
	public static final int numThreads = 10;
}
