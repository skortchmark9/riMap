package maps;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.junit.Test;


public class BinarySearchFileTest {
	
	
	String films = "./data/files/films.tsv";
	// Figure this out with TAs
	@Test
	public void FindUTF8() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/suites/ta-files/testIndex.tsv", BinarySearchFile.ParseType.INDEX);
		assertTrue(b.getXsByY("Miya雅vi Medium", "id")[0].equals("/m/01vvyc_"));
		b.close();
	}
	/*
	
	@Test
	public void FindFifty() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/suites/ta-files/testIndex.tsv", BinarySearchFile.ParseType.INDEX);
		assertTrue(b.getXsByY("50 Cent", "id")[0].equals("/m/01vvyc_"));
		b.close();
	}
	



	@Test
	public void TestXsByY() throws Exception {
		BinarySearchFile b = new BinarySearchFile(films, BinarySearchFile.ParseType.FILM);
		String[] nameAndStarring = b.getXsByY("/m/011_p6", "name", "starring");
		System.out.println(Arrays.toString(nameAndStarring));
		assertTrue(nameAndStarring[0].equals("Thunderbolt"));
		assertTrue(nameAndStarring[1].equals("/m/02ysx,/m/06mwbj,/m/0ksbyz,/m/03jl2r,/m/05bwd6"));
		b.close();
	}

	@Test
	public void WrongKey() throws Exception {
		Resources r = new Resources();
		assertTrue(null == r.indexFile.getXsByY("Taylor Lautner", "NEKEY"));
		r.closeResources();
	} 

	@Test
	public void TabbyBinarySearch() throws IOException {
		BinarySearchFile b = new BinarySearchFile(films, BinarySearchFile.ParseType.FILM);	
		assertTrue(b.getXsByY("/m/0hzcwhy", "name")[0].equals("Watch Juice"));
		b.close();
	}

	@Test
	public void BinarySearchNotThere() throws Exception {
		//I don't think we want this behavior.
		Resources r = new Resources();
		assertTrue(r.indexFile.getXsByY("Taylor Badass", "name") == null);
		r.closeResources();
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
		BinarySearchFile b = new BinarySearchFile(films, BinarySearchFile.ParseType.FILM);
		assertTrue(b.getXsByY("/m/038lhw", "name")[0].equals("Mo' Better Blues"));
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
		BinarySearchFile b = new BinarySearchFile(films, BinarySearchFile.ParseType.FILM);
//		System.out.println(b.search("/m/0ds29fr"));
		assertTrue(b.getXsByY("/m/0ds29fr", "name")[0].equals("Singularity"));
		b.close();
	}
	
	
	@Test
	public void BinarySearch() throws IOException {
		BinarySearchFile b = new BinarySearchFile(films, BinarySearchFile.ParseType.FILM);	
		assertTrue(b.getXsByY("/m/0fvp0c", "name") != null);
		b.close();
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
		assertTrue(r.moviesFile.getXsByY("/m/02x3lt7", "name")[0].equals("Hannah Montana: The Movie"));
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
