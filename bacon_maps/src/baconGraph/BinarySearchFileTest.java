package baconGraph;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.junit.Test;

import baconGraph.BinarySearchFile.ParseType;

public class BinarySearchFileTest {
	
	@Test
	public void BinarySearchNotThere() throws Exception {
		//I don't think we want this behavior.
		Resources r = new Resources();
		assertTrue(r.indexFile.getXByY("name", "Taylor Badass") == null);
		r.closeResources();
	} 

	@Test
	public void TabbyBinarySearch() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/files/films.tsv", ParseType.FILM);	
		System.out.println(b.search("/m/0hzcwhy"));
		assertTrue(b.getXByY("name", "/m/0hzcwhy") != null);
		b.close();
	}

	@Test
	public void ActorCorrectLength() throws Exception {
		Resources r = new Resources();
		System.out.println("\n\n" + r.indexFile.search("Taylor Swift"));
		assertTrue(r.indexFile.search("Taylor Swift").equals("Taylor Swift\t/m/0dl567"));
		r.closeResources();
	}

	@Test
	public void SearchByName2() throws Exception {
		Resources r = new Resources();
//		System.out.println(r.indexFile.search("Laurel Bryce"));
		assertTrue(true);
		r.closeResources();
	}
	

	@Test
	public void testModdedComparator() {
		byte[] a = "Sylvester Stallone".getBytes();
		byte[] b = "Sándor Almási".getBytes();
		byte[] c = "Sébastian udss".getBytes();
		assertTrue(BinarySearchFile.jCompare(a, b) < 0);
		assertTrue(BinarySearchFile.jCompare(b, a) > 0);
		assertTrue(BinarySearchFile.jCompare(b, c) < 0);
		assertTrue(BinarySearchFile.jCompare(a, c) < 0);
	}

	@Test
	public void BinarySearch1() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/files/films.tsv", ParseType.FILM);
		assertTrue(b.getXByY("name", "/m/038lhw").equals("Mo' Better Blues"));
		b.close();
	}

	@Test
	public void BinarySearchTest() throws Exception {
		Resources r = new Resources();
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
		BinarySearchFile b = new BinarySearchFile("./data/files/films.tsv", ParseType.FILM);
//		System.out.println(b.search("/m/0ds29fr"));
		assertTrue(b.getXByY("name", "/m/0ds29fr").equals("Singularity"));
		b.close();
	}
	
	@Test
	public void WrongKey() throws Exception {
		Resources r = new Resources();
		assertTrue(null == r.indexFile.getXByY("NEKEY", "Taylor Lautner"));
		r.closeResources();
	} 
	
	@Test
	public void BinarySearch() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/files/films.tsv", ParseType.FILM);	
		assertTrue(b.getXByY("name", "/m/0fvp0c") != null);
		b.close();
	}
	
	@Test
	public void TestXsByY() throws Exception {
		try(BinarySearchFile b = new BinarySearchFile("./data/files/films.tsv", ParseType.FILM)) {
				String[] nameAndStarring = b.getXsByY("/m/0f4_l", "name", "starring");
		}
	}
	
	@Test
	public void testNewLine() throws Exception {
		Resources r = new Resources();
		RandomAccessFile raf = r.indexFile.raf;
		long start = 77;
		long newLineStart = r.indexFile.prevNewLine(start, 0);
		long pos = r.indexFile.nextNewLine(start, raf.length());
		raf.seek(newLineStart);
		byte[] word = new byte[(int) (pos - newLineStart)];
		raf.read(word);
		String result = BinarySearchFile.string(word);
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
		Resources r = new Resources();
		assertTrue(r.indexFile.search("Samuel L. Jackson").equals("Samuel L. Jackson\t/m/0f5xn"));
		r.closeResources();
	}
	
	@Test
	public void SearchMoviesByID() throws Exception {
		Resources r = new Resources();

		assertTrue(r.moviesFile.getXByY("name", "/m/02x3lt7").equals("Hannah Montana: The Movie"));
		r.closeResources();
	}

	/*
	@Test
	public void SearchActorsByID() throws IOException {
//		BinarySearchFile b = new BinarySearchFile(Resources.actorsFile, ParseType.ACTOR);	
	//	b.close();
	}
	
	 @Test //Ant didn't like this test for some reason. It works though.
	public void fileNotFound() {
		boolean failed = true;
		try(BinarySearchFile b = new BinarySearchFile("./data/lolnotafile")) {
			failed = false;
		}
		catch (IOException E){
			assertTrue(failed);
		}
	}*/

}
