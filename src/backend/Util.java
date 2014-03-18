/**
 * 
 */
package backend;

/**
 * @author emc3
 *
 */
public class Util {
	
	private static long START = 0;
	
	/**
	 * Better than System.out.println. Prints any number of things in a line to STDOUT.
	 * 
	 * @param varg - a variable number of Objects to be concatenated into a string.<br>
	 * spaces added between args (think console.log() in js) 
	 */
	public static void out(Object...varg) {
		String str = "";
		for (Object o : varg) {
			if (str.equals(""))
				str += o;
			else
				str += " " + o;
		}
		System.out.println(str);
	}
	
	/**
	 * Better than System.err.println. Prints any number of things in a line to STDERR.
	 * @param varg - a variable number of Objects to be concatenated into a string.<br>
	 * spaces added between args (think console.log() in js) 
	 */
	public static void err(Object...varg) {
		String str = "";
		for (Object o : varg) {
			if (str.equals(""))
				str += o;
			else
				str += " " + o;
		}
		System.err.println(str);
	}
	
	public static void memLog() {
		Runtime r = Runtime.getRuntime();
		out("\t--MEM--");
		out("\tALLOC'd:", (((double)r.totalMemory())/1048576.0)+"MB", "|", "FREE:", ((double)(r.freeMemory())/1048576.0)+"MB", "|", "MAX:", (((double) r.maxMemory())/1048576.0)+"MB");
	}
	
	public static String getFirst4Digits(double x) {
		String ans = (int) x + "";
		if (x < 100) {
			return ans + decimalDigits(x, 2); 
		} else {
			return ans + decimalDigits(x, 3);
		}
	}
	
	static int decimalDigits(double x, int n) {
	    double ans;
	    ans = (x - (int) x) * Math.pow(10, n);
	    return (int) ans;
	}
	
	/**
	 * Starts the stopwatch
	 * @return the time of clock start
	 */
	public static long resetClock() {
		return (START = System.currentTimeMillis()); 
	}
	
	public static long lap() {
		return (System.currentTimeMillis() - START);
	}
	
}
