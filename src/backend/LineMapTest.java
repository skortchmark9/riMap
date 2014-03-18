package backend;

import static org.junit.Assert.*;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Test;

public class LineMapTest {

	@Test
	public void test() {
		LineMap l = new LineMap();
		l.putNext(0, 10);
		l.putNext(2, 10);
		l.putPrev(46, 40);
		l.putNext(45, 50);
		l.putNext(3, 10);
		assertTrue(l.getNextNewLine(2) == 10);
		assertTrue(l.getNextNewLine(3) == 10);
		assertTrue(l.getNextNewLine(49) == 50);
		assertTrue(l.getNextNewLine(20) == null);
		assertTrue(l.getPrevNewLine(47) == 40);
	}
	
}
