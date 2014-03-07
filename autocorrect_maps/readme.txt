README:

Most of the work gets done in suggestion.evaluate, using weights defined in constants.

My smart rank is for moderately smart people! It makes the assumption that the user is reasonably good at spelling, and therefore it prioritizes word completion over correcting misspellings. However - it doesn't expect the user to be too smart. It also reduces the probability of suggesting really long words by subtracting the distance of the suggestion from the current node on the tree. Finally, it places a high degree of importance on bigram probability. Mathematically, it evaluates suggestions based on the following formula.

unigram frequency * 2 + bigram frequency * 20 - levenshtein distance * 20 - number of jumps from the prefix to the suggestion in the tree * 4.