package baconGraph;

import java.util.regex.Pattern;

public class Constants {
	//Precompiled regex patterns to make split and replace faster. 
	public static final Pattern tab = Pattern.compile("\t");
	public static final Pattern comma = Pattern.compile(",");
	public static final Pattern digits = Pattern.compile("\\D");
	public static final int BufferLength = 1024;

}
