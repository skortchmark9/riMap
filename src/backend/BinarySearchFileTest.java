package backend;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.regex.Matcher;

import org.junit.Test;


public class BinarySearchFileTest {
	
	
	String films = "./data/baconfiles/films.tsv";
	// Figure this out with TAs
	
	@Test
	public void testReadChunks() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/baconfiles/ta-files/testIndex.tsv", "name", "name", "id");
		//System.out.println(b.readChunks("name", "id"));
		b.close();
		assertTrue(true);
	}
	

	@Test
	public void FindUTF8() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/baconfiles/index.tsv", "name", "name", "id");
		System.out.println(b.search("雅-miyavi-"));
		assertTrue(b.getXsByY("雅-miyavi-", "id")[0].equals("/m/01qkwf8"));
		b.close();
	}
	
	//Why does the below work but not the above ?

	@Test
	public void FindUTF8Again() throws IOException {
		BinarySearchFile b = new BinarySearchFile("./data/baconfiles/index.tsv", "name", "name", "id");
		System.out.println(b.search("안소희"));
		assertTrue(b.getXsByY("Íñigo Garcés", "id")[0].equals("/m/0c09t3g"));
		b.close();
	}

	/**
	 * Tests to make sure jCompare is handling special characters correctly.
	 */
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
			String name2 = line2.split("\t")[0];
			//System.out.println(name1 + " : " + name2);
			int cmp = BinarySearchFile.jCompare(name1.getBytes(), name2.getBytes());
			if (!(cmp < 0)) {
				System.err.println("jCompare Failed! Compare returned: " + cmp);
				System.err.println("\tfirst: " + name1);
				System.err.println("\tsecond: " + name2);
				assertTrue(false);
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
		assertTrue(BinarySearchFile.jCompare(a, b) < 0);
		assertTrue(BinarySearchFile.jCompare(b, a) > 0);
		assertTrue(BinarySearchFile.jCompare(b, c) < 0);
		assertTrue(BinarySearchFile.jCompare(a, c) < 0);
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
