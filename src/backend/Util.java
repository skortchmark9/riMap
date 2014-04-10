/**
 * 
 */
package backend;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * @author emc3
 */
public class Util {
	
	private static long START = 0; //the 'clock'
	
	/**
	 * Better than System.out.println. Prints any number of things in a line to STDOUT.
	 * 
	 * @param varg - a variable number of Objects to be concatenated into a string.<br>
	 * spaces added between args (think console.log() in js) 
	 */
	public static void out(Object...varg) {
		System.out.println(composeString(varg));
	}
	
	/**
	 * Better than System.err.println. Prints any number of things in a line to STDERR.
	 * @param varg - a variable number of Objects to be concatenated into a string.<br>
	 * spaces added between args (think console.log() in js) 
	 */
	public static void err(Object...varg) {
		System.err.println(composeString(varg));
	}
	
	
	public static void debug(Object...varg) {
		if (Constants.DEBUG_MODE) {
			System.out.println(composeString(varg));
		}
	}
	
	private static String composeString(Object...varg) {
		String str = "";
		for (Object o : varg) {
			String s;
			
			if (o instanceof String) s = (String)o;
			else s = o.toString();
			
			if (str.equals("")) str += s;
			else str += " " + s;
		}
		return str;
	}
	
	/**
	 * Logs the current state of the 
	 * Virtual memory. (should be only used as an
	 * estimate). 
	 */
	public static void memLog() {
		Runtime r = Runtime.getRuntime();
		out("\t--MEM--");
		out("\tALLOC'd:", (((double)r.totalMemory())/1048576.0)+"MB", "|", "FREE:", ((double)(r.freeMemory())/1048576.0)+"MB", "|", "MAX:", (((double) r.maxMemory())/1048576.0)+"MB");
	}
	
	
	/**
	 * formats a double as a string of the first 4 digits without the period, so that
	 * the String can be used to find nodes with the corresponding ID.
	 * @param x - the double to stringify
	 * @return
	 * a string containing the first four digits of the double 
	 * without a period delimiting decimals
	 */
	public static String getFirst4Digits(double x) {
		x = Math.abs(x);
		String num = String.valueOf(x);
		num = num.replace(".", "");
		while (num.length() < 4) {
			num+= "0";
		}
		return num.substring(0, 4);
	}
	
	/**
	 * formats the string as title case.
	 * Used b/c strings in the radix tree are all lowercase.
	 * This puts the first char of all strings to uppercase.
	 * @param original - the original string to format
	 * @return
	 * A new formatted string in title case (uppercase first characters)
	 */
	public static String capitalizeAll(String original) {
		String[] broken = Constants.spaces.split(original);
		String result = "";
		int i = 0;
		int length = broken.length;
		while (i < length) {
			result += capitalize(broken[i]);
			if (i != length - 1) {
				result+=" ";
			}
			i++;
		}
		return result;
	}
	
	/**
	 * Capitalizes only the first character in a word.
	 * @param original - the word to capitalize
	 * @return
	 * the original string with a capital first letter
	 */
	public static String capitalize(String original) {
		return original.length() == 0 ? original : original.substring(0, 1).toUpperCase() + original.substring(1);	
	}
	
	/**
	 * Concatenates byte arrays into a new array containing both original arrays
	 * 
	 * @param first - the first byte array to concat
	 * @param second - the second byte array to concat
	 * @return
	 * A new bye array containing both first and second byte arrays
	 */
	public static byte[] concatByteArrays(byte[] first, byte[] second) {
		byte[] result = new byte[first.length + second.length];
		int lastIndex = 0;
		for(int i = 0; i < first.length; i++) {
			result[i] = first[i];
			lastIndex = i;
		}
		lastIndex++;
		for(int i = 0; i < second.length; i++) {
			result[i + lastIndex] = second[i];
		}
		return result;
	}
	/**
	 * Starts the stopwatch
	 * @return the time of clock start
	 */
	public static long resetClock() {
		START = System.currentTimeMillis(); 
		return START;
	}
	
	/**
	 * Makes a measurement of the stopwatch based on Util.START
	 * @return
	 * A long representing how much time has passed since Util.resetClock() 
	 * has been called
	 */
	public static long lap() {
		return (System.currentTimeMillis() - START);
	}
	
	/**
	 * @param start - the start of the time period to test
	 * @return
	 * the time elapsed since the given long
	 * 
	 */
	public static long timeSince(long start) {
		return (System.currentTimeMillis() - start);
	}
	
	
	/**
	 * @param minLat - the current minimum latitude of the map view
	 * @param maxLat - the current maximum latitude of the map view
	 * @param minLon - the current minimum longitude of the map view
	 * @param maxLon - the current maximum longitude of the map view
	 * @return
	 * true if the view encompasses a boundary
	 * false if the view is somewhere inside the boundaries. 
	 */
	public static boolean boundariesInRange(double minLat, double maxLat, double minLon, double maxLon) {
		if (Constants.MINIMUM_LATITUDE >= minLat || Constants.MAXIMUM_LATITUDE <= maxLat 
				|| Constants.MINIMUM_LONGITUDE >= minLon || Constants.MAXIMUM_LONGITUDE <= maxLon)
			return true;
		return false;
	}

	public static ThreadPoolExecutor defaultThreadPool(String name, int core, int max) {
		ThreadPoolExecutor result = new ThreadPoolExecutor(core, max, Constants.THREADPOOL_TIMEOUT,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), 
				new ThreadFactoryBuilder().setNameFormat(name + "-pool %d").build());
		return result;
	}
}
