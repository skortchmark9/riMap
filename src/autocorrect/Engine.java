package autocorrect;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Engine {
	public RadixTree rt;
	public Ranker r;
	public Generator g;

	Engine(Generator g, Ranker r, List<String> files) {
		this.g = g;
		this.r = r;
		this.rt = new RadixTree();
		for(String file : files) {
			this.rt.populateRadixTree(file);
		}
	}
	
	public Engine(Generator g, Ranker r, RadixTree rt) {
		this.g = g;
		this.r = r;
		this.rt = rt;
	}
	
	public void addFile(String file) {
		this.rt.populateRadixTree(file);
	}
	
	public void clearRT() {
		this.rt = new RadixTree();
	}
	
	public boolean hasFiles() {
		return !rt.isEmpty();
	}
	
	public List<String> suggest(String userInput) {
		String[] input =  AutoCorrectUtils.lineParse(userInput);
		String currentWord = input[input.length - 1];
		String previousWord = "";
		if (input.length > 1)
			previousWord = input[input.length - 2];	

		r.setPreviousWord(previousWord);
		r.setCurrentWord(currentWord);

		LinkedList<Suggestion> allResults = g.generateSuggestions(previousWord, currentWord, this.rt);
		Collections.sort(allResults, r);
		LinkedList<Suggestion> topSuggestions = new LinkedList<>();
		while(topSuggestions.size() < AutoCorrectConstants.numResults && allResults.size() > 0) {
			Suggestion topSuggestion = allResults.removeFirst();
			if (!topSuggestions.contains(topSuggestion)) {
				topSuggestions.add(topSuggestion);
			}
		}
		List<String> results = new LinkedList<>();
		for(Suggestion s : topSuggestions) {
			input[input.length - 1] = s.toString();
			results.add(AutoCorrectUtils.arrayParse(input));
		}
		return results;
	}
}
