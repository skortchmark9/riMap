package autocorrect;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import org.junit.Test;

public class RadixTreeTest {
	
	public final String dictionary = "./autocorrect_maps/data/autocorrect/dictionary.txt";
	public final String dickens = "./autocorrect_maps/data/autocorrect/great_expectations.txt";
	
	
	@Test
	public void whitespaceTest1() {
		/** Testing to see if it will identify the correct break in compounded words.*/
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		boolean outcome = true;
		for(Suggestion s : rt.whitespace("hishat")) {
			if (!s.equals(new Suggestion("his hat")))
				outcome = false;
		}
		assertTrue(outcome);
	}

	
	@Test
	public void lineParseTest() {
		String test = " yes and many others all of the'm but you here is mrs brandley i ll\n say no more \n \n";
		String[] result = {"yes", "and", "many", "others", "all", "of", "the", "m", "but", "you", "here", "is", "mrs", "brandley", "i", "ll", "say", "no", "more"};
		assertTrue(Arrays.equals(result, Utils.lineParse(test)));
	}
	@Test
	public void rtDictionaryTest() {
		//Reads through the whole dictionary and then searches all the words in that dictionary in the created tree.
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		rt.populateRadixTree(dictionary);
		assertTrue(rt.searchThru(dickens));
	}
	
	@Test
	public void rtMultipleInsertTest() {
		// Tests for frequencies working correctly
		RadixTree rt = new RadixTree();
		rt.insert("cat");
		rt.insert("catfish");
		rt.insert("cat");
		rt.insert("cat");
		rt.insert("cat");
		rt.insert("dog");
		assertTrue(rt.getFreq("cat") == 4 && rt.getFreq("dog") == 1);
	}
	
	@Test
	public void rtIntermediateTest() {
		// Tests for intermediate nodes working correctly
		RadixTree rt = new RadixTree();
		String [] words = {"cat", "catfish", "cart"};
		for(String s : words)
			rt.insert(s);
		boolean outcome = true;
		for(String s : words)
			if (!rt.search(s)) {
				outcome = false;
			}
		assertTrue(outcome);
	}
	
	@Test
	public void getPrefixSuggestionsTest1() {
		// Can we generate prefix suggestions? 
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		System.out.println(rt.getPrefixSuggestions("akim", 10));
		assertTrue(rt.getPrefixSuggestions("akim", 10).contains(new Suggestion("akimbo")));
		assertTrue(rt.getPrefixSuggestions("t").contains(new Suggestion("table")));
	}
	
	@Test
	public void getPrefixSuggestionsTest2() {
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		boolean outcome = true;
		for(Suggestion s : rt.getPrefixSuggestions("ab")) {
			if (!s.getWord().startsWith("ab"))
				outcome = false;
		}
		assertTrue(outcome);
	}

	
	@Test
	public void levenshteinTest1() {
		// Test against reference implementation.
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dictionary);
		boolean outcome = true;
		for (Suggestion s : rt.levenshtein("cat", 2)) {
			if (Utils.computeLevenshteinDistance(s.getWord(), "cat") != s.getLED())
				outcome = false;
		}
		assertTrue(outcome);
	}
	
	@Test
	public void levenshteinTest2() {
		// Test against reference implementation.
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		boolean outcome = true;
		for (Suggestion s : rt.levenshtein("bird", 5)) {
			if (Utils.computeLevenshteinDistance(s.getWord(), "bird") != s.getLED())
				outcome = false;
		}
		assertTrue(outcome);
	}
	
	@Test
	public void whitespaceTest2() {
		//Testing to make sure BOTH outputted words are found in the tree.
		RadixTree rt = new RadixTree();
		rt.insert("hi", "my");
		rt.insert("there", "b");
		assertTrue(rt.whitespace("hithere").contains(new Suggestion("hi there")));
		assertTrue(!rt.whitespace("hither").contains(new Suggestion("hi there")));
	}
	
	@Test
	public void whitespaceTest3() {
		// Testing to see if it will identify the correct break in compounded words.
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		rt.insert("hi");
		rt.insert("shat");
		
		boolean outcome = true;
		List<Suggestion> suggestions = rt.whitespace("hishat");
		for(Suggestion s : suggestions) {
			if (!(s.equals(new Suggestion("his hat")) || s.equals(new Suggestion("hi shat"))))
					outcome = false;
		}
		assertTrue(outcome);
	}

	@Test
	public void bigramNeighborsTest1() {
		// Testing....
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		rt.insert("hat", "ballin");
		HashMap<String, Integer> results = rt.getBigramNeighbors("hat");
		assertTrue(results.containsKey("his"));
		assertTrue(results.containsKey("ballin"));
		}
	
	@Test
	public void bigramProbabilityTest1() {
		RadixTree rt = new RadixTree();
		rt.insert("hat", "ballin");
		rt.insert("hat", "ballin");
		rt.insert("hat", "ballin");
		rt.insert("the", "ballin");
		rt.insert("hat", "ballin");
		rt.insert("hat", "ballin");
		rt.insert("the", "ballin");
		assertTrue(rt.getBigramNeighbors("hat").containsValue(5));
	}

	
	/*
    @Test
    public void suggestTest() {
        List<String> files = new ArrayList<String>(1);
        files.add(dickens);
        Generator g = new Generator(3, Generator.SuggestType.WHITESPACE, Generator.SuggestType.PREFIX);
        Ranker r = new Ranker("hat", "hat");
        
        Engine e = new Engine(g, r, files);
        assertTrue(e.suggest("my hat").size() == 5);
    }
    
    @Test
    public void suggestTest1() {
        List<String> files = new ArrayList<String>(1);
        files.add(dickens);
        Generator g = new Generator(0);
        Ranker r = new Ranker("hat", "hat");
        
        Engine e = new Engine(g, r, files);
        assertTrue(e.suggest("my hat").size() == 1);
    }

	
	
	@Test
	public void testRankings() {
		RadixTree rt = new RadixTree();
		rt.populateRadixTree(dickens);
		String previousWord = "the";
		String currentWord = "mand";
		Generator g = new Generator(1, Generator.SuggestType.WHITESPACE, Generator.SuggestType.PREFIX);
		LinkedList<Suggestion> allResults = g.generateSuggestions(previousWord, currentWord, rt);
		Collections.sort(allResults, new Ranker(previousWord, currentWord));
			LinkedList<Suggestion> topResults = new LinkedList<>();
			for(int i = 0; i < Constants.numResults && i < allResults.size(); i++) {
				Suggestion topResult = allResults.removeFirst();
				if (!topResults.contains(topResult)) {
					topResults.add(topResult);
				}
				else
					i--;
			}
//		for(Suggestion s : topResults)
//			System.out.println("Suggestion: " + s.toString());
	}
	
/*	@Test
	public void testEngine() {
		String previousWord = "the";
		String currentWord = "mand";

		Generator g = new Generator(1, Generator.SuggestType.WHITESPACE, Generator.SuggestType.PREFIX);
		Ranker r = new Ranker(previousWord, currentWord);
		LinkedList<String> files = new LinkedList<>();
		files.add(dickens);
		files.add(dictionary);
		Engine e = new Engine(g, r, files);
		for(Suggestion s : e.suggest(previousWord, currentWord))
			System.out.println("Suggestion: " + s.toString());
	}*/
	

	//*/
	
}
