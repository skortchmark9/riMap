package backend;
import java.util.Random;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * Maintaining two separate hashmaps for the previous and next
 * newlines seemed inefficient - after all, the previous newLine
 * was just the next newLine from the line before. This class is 
 * a thin wrapper for a RangeSet, a cool thing from Guava which lets
 * us define disconnected and continuous ranges in a set. We build
 * a range between the position of the fileReader and the next/prev 
 * newLine like so..[position..newLine) or (newLine..position].
 * Because we don't actually include the newLines in the range, but
 * but keep them as the upper bound of the range, we can quickly
 * retrieve the newLine's position, and then be sure if it is actually
 * a newLine by checking if it exists in the set anywhere. We build the
 * ranges with the information we have available to us, and Guava
 * automatically concatenates them. Below is an example where the file
 * has length 40. The newlines occur at 10, and 25. and calls to the range
 * between 25 - 34 will return null, because the area has not been traversed.
 * (0, 10) [11, 25) [35, 40]
 * @author samkortchmar
 *
 */
public class LineMap {
	RangeSet<Long> lineMap = TreeRangeSet.create();
	
	/**
	 * Adds the newLine to the current RangeSet
	 * @param position - the position of the cursor.
	 * @param nextNewLine
	 */
	void putNext(long position, long nextNewLine) {
		if (position > nextNewLine) {
			Util.err("ERROR: Did you mean putPrev?");
			return;
		}
		//Closed open is google's syntax for [position, nextNewLine)
		synchronized(lineMap) {
			lineMap.add(Range.closedOpen(position, nextNewLine));
		}
	}
	
	void putPrev(long position, long prevNewLine) {
		if (position < prevNewLine) {
			Util.err("ERROR: Did you mean putNext?");
			return;
		} else {
			synchronized(lineMap) {
				lineMap.add(Range.openClosed(prevNewLine, position));
			}
		}
	}
	
	Long getNextNewLine(long position) {
		Range<Long> r = lineMap.rangeContaining(position);
		if (r == null) {
			return null;
		}
		Long possibleNextLine = r.upperEndpoint();
		if (lineMap.contains(possibleNextLine)) {
			return null;
		} else {
			return possibleNextLine;
		}
	}
	
	Long getPrevNewLine(long position) {
		Range<Long> r = lineMap.rangeContaining(position);
		if (r == null) {
			return null;
		}
		Long possiblePrevLine = r.lowerEndpoint();
		if (lineMap.contains(possiblePrevLine)) {
			return null;
		} else {
			return possiblePrevLine;
		}
	}
	
	int suggestLineLength(long rafLength) {
		int lineLength = 0;
		int successfulTries = 0;
		for(int i = 0; i < 20; i++) {
			Random r = new Random(rafLength);
			Range<Long> range = lineMap.rangeContaining(r.nextLong());
			if (range != null) {
				lineLength += (range.upperEndpoint() - range.lowerEndpoint());
				successfulTries++;
			}
		}
		return lineLength / successfulTries;
	}
}


