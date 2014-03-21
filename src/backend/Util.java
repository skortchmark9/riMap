/**
 * 
 */
package backend;

import java.text.DecimalFormat;

import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * @author emc3
 *
 */
public class Util {
	
	private static long START = 0;
	private static JTextArea msgBox;
	
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
		x = Math.abs(x);
		String num = String.valueOf(x);
		num = num.replace(".", "");
		while (num.length() < 4) {
			num+= "0";
		}
		return num.substring(0, 4);
	}
	
	static int decimalDigits(double x, int n) {
	    double ans;
	    ans = (x - (int) x) * Math.pow(10, n);
	    return (int) ans;
	}
	
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
	
	public static String capitalize(String original) {
		return original.length() == 0 ? original : original.substring(0, 1).toUpperCase() + original.substring(1);	
	}
	
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
		return (START = System.currentTimeMillis()); 
	}
	
	public static long lap() {
		return (System.currentTimeMillis() - START);
	}
	
	public static long timeSince(long start) {
		return (System.currentTimeMillis() - start);
	}

	public static boolean boundariesInRange(double minLat, double maxLat, double minLon, double maxLon) {
		if (Constants.MINIMUM_LATITUDE >= minLat || Constants.MAXIMUM_LATITUDE <= maxLat 
				|| Constants.MINIMUM_LONGITUDE >= minLon || Constants.MAXIMUM_LONGITUDE <= maxLon)
			return true;
		return false;
	}

	public static void setGUIMessageBox(JTextArea msgBox2) {
		Util.msgBox = msgBox2;
	}
	
	public static void guiMessage(String str) {
		if (msgBox == null) return;
		msgBox.setText(str + "\n" + msgBox.getText());
	}
}
