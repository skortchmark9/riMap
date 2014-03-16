package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;

import maps.MapFactory;
import maps.Node;
import maps.PathNodeWrapper;
import backend.Backend;
import backend.Constants;
import edu.brown.cs032.emc3.kdtree.KDStub;
import edu.brown.cs032.emc3.kdtree.KDimensionable;
import graph.PathFinder;

public class PathTester {
	Node source;
	Node dest;
	Backend b;


	PathTester(String s, Backend b) {
		this.b = b;
		CLIParse(s);
		if (source == null || dest == null) {
			System.out.println("ERROR: COULD NOT FIND SOURCE OR DEST");
		} else {
		PathFinder<PathNodeWrapper, Node> p = new PathFinder<PathNodeWrapper, Node>(new PathNodeWrapper(source), new PathNodeWrapper(dest));
		p.getPath();
		}
	}
	
	private void streetnames(String street1, String xStreet1, String street2, String xStreet2) {
		source = MapFactory.createIntersection(street1, xStreet1);
		dest = MapFactory.createIntersection(street2, xStreet2);
	}
	
	private void latlng(double lat1, double lon1, double lat2, double lon2) {
		KDimensionable source = new KDStub(lat1, lon1);
		KDimensionable dest = new KDStub(lat2, lon2);
		source = b.getNearestNeighbors(1, source).get(0);
		dest = b.getNearestNeighbors(1, dest).get(0);
	}
	
	
	private void CLIParse(String s) {
		List<String> matchList = new ArrayList<>(4);
		Matcher regexMatcher = Constants.quotes.matcher(s);
		while (regexMatcher.find()) {
			  if (regexMatcher.group(1) != null) {
			        // Add double-quoted string without the quotes
			        matchList.add(regexMatcher.group(1));
			    } else if (regexMatcher.group(2) != null) {
			        // Add single-quoted string without the quotes
			        matchList.add(regexMatcher.group(2));
			    } else {
			        // Add unquoted word
			        matchList.add(regexMatcher.group());
			    }
		}
		if (matchList.size() != 4) {
			System.out.println("ERROR: INVALID CLI ARGS - number of arguments");
			return;
		}
		boolean isDoubles = true; //Is the string just a string of doubles?
		for(String word : matchList) {
			Scanner scanner = new Scanner(word);
			isDoubles &= scanner.hasNextDouble();
			scanner.close();
		}
		String arg0 = matchList.get(0);
		String arg1 = matchList.get(1);
		String arg2 = matchList.get(2);
		String arg3 = matchList.get(3);
		
		if (isDoubles) {
			try {
				latlng( Double.parseDouble(arg0),
						Double.parseDouble(arg1),
						Double.parseDouble(arg2),
						Double.parseDouble(arg3)); 
			} catch (NumberFormatException nfe) {
				System.out.println("ERROR: INVALID CLI ARGS - could not parse doubles");
				return;
			}
		}
		else {
			streetnames(arg0, arg1, arg2, arg3);
		}
	}
}