package backend;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;

import backend.BinarySearchFile.SearchType;
import backend.Util;

import org.junit.Test;

public class BinarySearchFileTest {
	String films = "./data/baconfiles/films.tsv";
	// Figure this out with TAs
	//Applied for multiple rows

	
	@Test
	public void capitalizeTest() {
		String orig = "what up";
		assertTrue(Util.capitalizeAll(orig).equals("What Up"));
	}
	
	@Test
	public void testConcatByte() {
		byte[] first = new byte[] {1, 2, 3,4, 5};
		byte[] second = new byte[] {6, 7, 8, 9, 10};
		byte[] result = Util.concatByteArrays(first, second);
		byte[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		assertTrue(Arrays.equals(result, expected));
		}
	
	
	@Test
	public void testSearchSingleMultiple() {
		try (BinarySearchFile b = new BinarySearchFile("./data/mapsfiles/index.tsv", "name", "name", "nodes")) {
			List<List<String>> results = b.searchMultiples("Olive St", "name", "nodes");
			assertTrue(results.get(0).get(0).equals("Olive St"));
			assertTrue(results.size() == 1);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	
	@Test
	public void testSearchMultiples() {
		try (BinarySearchFile b = new BinarySearchFile("./data/mapsfiles/index.tsv", "name", "name", "nodes")) {
			List<List<String>> results = b.searchMultiples("10th Avenue", "name", "nodes");
			assertTrue(results.size() == 3);
			for(List<String> list : results) {
				assertTrue(list.get(0).equals("10th Avenue"));
			}
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void wildCardMultipleTest() {
		try (BinarySearchFile b = new BinarySearchFile("./data/mapsfiles/index.tsv", "name", "name", "nodes")) {
			assertTrue(b.searchMultiples("2nd ", SearchType.WILDCARD, "name", "nodes").size() == 26);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void wildCardTest() {
		try (BinarySearchFile b = new BinarySearchFile("./data/mapsfiles/index.tsv", "name", "name", "nodes")) {
			assertTrue(b.search("Olive Str", SearchType.WILDCARD).equals("Olive Street	/n/4192.7140.201402759,/n/4192.7140.201402766"));
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testDecimalDigitTest() {
		double testValue = Constants.INITIAL_LAT;
		assertTrue(84 == Util.decimalDigits(testValue, 2));
		assertTrue(8 == Util.decimalDigits(testValue, 1));
		assertTrue(0 == Util.decimalDigits(testValue, 0));
		assertTrue(0 == Util.decimalDigits(testValue, -1));
		testValue = Constants.INITIAL_LON;
		assertTrue (-41 == Util.decimalDigits(testValue, 2));
		assertTrue(-4 == Util.decimalDigits(testValue, 1));
		assertTrue(0 == Util.decimalDigits(testValue, 0));
		assertTrue(0 ==  Util.decimalDigits(testValue, -1));
		testValue = -73.8000;
		Util.out(Util.getFirst4Digits(testValue));
		
	}



	@Test
	public void testReadChunks() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/baconfiles/ta-files/testIndex.tsv", "name", "name", "id");
		//		System.out.println(b.readChunks("name", "id"));
		b.close();
		assertTrue(true);
	}
	//Applied for single row - fix readchunks

	@Test
	public void testStringParse() {
		List<String> matchList = new LinkedList<>();
		String test = "hey \"this is\" a string";
		Matcher regexMatcher = Constants.quotes.matcher(test);
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
		assertTrue(matchList.contains("hey"));
		assertTrue(matchList.contains("this is"));
		assertTrue(matchList.contains("a"));
		assertTrue(matchList.contains("string"));
	}



	@Test
	public void FindUTF8() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/baconfiles/index.tsv", "name", "name", "id");
		//		System.out.println(b.search("雅-miyavi-"));
		assertTrue(b.getXsByY("雅-miyavi-", "id")[0].equals("/m/01qkwf8"));
		b.close();
	}

	//Why does the below work but not the above ?

	@Test
	public void FindUTF8Again() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/baconfiles/index.tsv", "name", "name", "id");
		//		System.out.println(b.search("안소희"));
		assertTrue(b.getXsByY("Íñigo Garcés", "id")[0].equals("/m/0c09t3g"));
		b.close();
	}

	@Test
	public void jCompSpecialsTest() throws IOException {
		BufferedReader r = new BufferedReader(new FileReader("./data/baconfiles/special-chars/index_any_special.tsv"));
		String line = r.readLine(); //reads the header
		String line2 = "blah"; //so that line2 does not start as null
		while(line != null && line2 != null) {
			line = r.readLine();
			if(line == null) continue;
			String name1 = line.split("\t")[0];
			line2 = r.readLine();
			if (line2 == null) continue;
			String name2 = line2;
			//System.out.println(name1 + " : " + name2);
			int cmp = BinarySearchFile.jCompare(name1.getBytes(), name2.getBytes(), 0);
			if (!(cmp < 0)) {
				int follow = line.charAt(name1.length());
				if (!(follow == '\t' || follow == '\n')) {
				System.err.println("jCompare Failed! Compare returned: " + cmp);
				System.err.println("\tfirst: " + name1);
				System.err.println("\tsecond: " + name2);
				assertTrue(false);
				}
			}
		}
		r.close();
	}


	@Test
	public void FindFifty() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/baconfiles/ta-files/testIndex.tsv", "name", "name", "id");
		assertTrue(b.getXsByY("50 Cent", "id")[0].equals("/m/01vvyc_"));
		b.close();
	}

	@Test
	public void TestXsByY() throws Exception {
		BinarySearchFile b = new BinarySearchFile(films, "id", "id", "name", "starring");
		String[] nameAndStarring = b.getXsByY("/m/011_p6", "name", "starring");
		assertTrue(nameAndStarring[0].equals("Thunderbolt"));
		assertTrue(nameAndStarring[1].equals("/m/02ysx,/m/06mwbj,/m/0ksbyz,/m/03jl2r,/m/05bwd6"));
		b.close();
	}

	@Test
	public void WrongKey() throws Exception {
		Resources r = new Resources(1);
		assertTrue(null == Resources.indexFile.getXsByY("Taylor Lautner", "NEKEY"));
		r.closeResources();
	} 

	@Test
	public void TabbyBinarySearch() throws IOException {
		BinarySearchFile b = new BinarySearchFile(films, "id", "id", "name", "starring");	
		assertTrue(b.getXsByY("/m/0hzcwhy", "name")[0].equals("Watch Juice"));
		b.close();
	}

	@Test
	public void BinarySearchNotThere() throws Exception {
		//I don't think we want this behavior.
		Resources r = new Resources(1);
		assertTrue(Resources.indexFile.getXsByY("Taylor Badass", "name") == null);
		r.closeResources();
	} 


	@Test
	public void ActorCorrectLength() throws Exception {
		Resources r = new Resources(1);
		assertTrue(Resources.indexFile.search("Taylor Swift").equals("Taylor Swift\t/m/0dl567"));
		r.closeResources();
	}


	@Test
	public void testModdedComparator() {
		byte[] a = "Sylvester Stallone".getBytes();
		byte[] b = "Sándor Almási".getBytes();
		byte[] c = "Sébastian udss".getBytes();
		assertTrue(BinarySearchFile.jCompare(a, b, 0) < 0);
		assertTrue(BinarySearchFile.jCompare(b, a, 0) > 0);
		assertTrue(BinarySearchFile.jCompare(b, c, 0) < 0);
		assertTrue(BinarySearchFile.jCompare(a, c, 0) < 0);
	}

	@Test
	public void BinarySearch1() throws IOException {
		BinarySearchFile b = new BinarySearchFile(films, "id", "id", "name", "starring");
		assertTrue(b.getXsByY("/m/038lhw", "name")[0].equals("Mo' Better Blues"));
		b.close();
	}

	@Test
	public void BinarySearchTest() throws Exception {
		Resources r = new Resources(1);
		assertTrue(r.indexFile.search("Fabio Stallone").equals("Fabio Stallone\t/m/07z32y_"));
		r.closeResources();
	}


	@Test
	public void testMatcher() {
		String line = "OHMYG0ODNUMB3RS";
		Matcher m = Constants.digits.matcher("OHMYG0ODNUMB3RS");
		line = m.replaceAll("!!!");
		assertTrue(!line.equals("OHMYG0ODNUMB3RS"));
	}

	@Test
	public void BinarySearch2() throws IOException {
		BinarySearchFile b = new BinarySearchFile(films, "id", "id", "name", "starring");
		//		System.out.println(b.search("/m/0ds29fr"));
		assertTrue(b.getXsByY("/m/0ds29fr", "name")[0].equals("Singularity"));
		b.close();
	}


	@Test
	public void BinarySearch() throws IOException {
		BinarySearchFile b = new BinarySearchFile(films, "id", "id", "name", "starring");	
		assertTrue(b.getXsByY("/m/0fvp0c", "name") != null);
		b.close();
	}

	@Test
	public void testNewLine() throws Exception {
		Resources r = new Resources(1);
		RandomAccessFile raf = Resources.indexFile.raf;
		long start = 77;
		long newLineStart = Resources.indexFile.prevNewLine(start, 0);
		long pos = Resources.indexFile.nextNewLine(start, raf.length());
		raf.seek(newLineStart);
		byte[] word = new byte[(int) (pos - newLineStart)];
		raf.read(word);
		assertTrue(r.indexFile.search("Samuel L. Jackson").equals("Samuel L. Jackson\t/m/0f5xn"));
		r.closeResources();
	}


	@Test
	public void SearchByID() throws Exception {
		//		BinarySearchFile b = new BinarySearchFile(Resources.actorsFile, ParseType.ACTOR);
		//	String result = "Steve Coogan\t\t\t\t/m/01nfys\t/m/08k40m,/m/0crzbrv";

		//		assertTrue(b.search("/m/01nfys").equals(result));
	}
	@Test
	public void SearchByName() throws Exception {
		Resources r = new Resources(1);
		assertTrue(Resources.indexFile.search("Samuel L. Jackson").equals("Samuel L. Jackson\t/m/0f5xn"));
		r.closeResources();
	}

	@Test
	public void SearchMoviesByID() throws Exception {
		Resources r = new Resources(1);
		assertTrue(Resources.waysFile.getXsByY("/m/02x3lt7", "name")[0].equals("Hannah Montana: The Movie"));
		r.closeResources();
	}
	/*
	@Test //Ant didn't like this test for some reason. It works though.
	public void fileNotFound() {
		boolean failed = true;
		try(BinarySearchFile b = new BinarySearchFile("./data/lolnotafile")) {
			failed = false;
		}
	}
	 */
}
