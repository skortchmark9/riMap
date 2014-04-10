<h4>Traffic</h4>
================

This project is handles real-world map data and provides path information. You can pan around and click points on the map, or you can use the input fields to input cross-streets and find paths between them. The project is backed by a few interesting data structures. It connects to a traffic server and dynamically updates clients with traffic information.

1.) KD Tree, which handles the nodes in the map. It has a custom constructor which reduces construction time to k*nlogn. The KD Tree also allows us to specify the boundaries of the map.<br>
2.) Radix Tree, which provides autocompletion results for each street. Faster and more efficient than a typical trie, it also creates suggestions based on levenshtein distance, possible whitespace errors, and bigram frequency.<br>
3.) Binary Search File - a custom wrapper for Java's Random Access File - this class has numerous optimizations that allow for quick retrieval of information from the Random Access File with minimal system calls.
<br><br>

The project uses multiple threads to ensure that the frontend GUI remains responsive no matter what kind of requests are being made to the server.
<br>
<br>
