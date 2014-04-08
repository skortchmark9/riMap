CS032_Maps
==========

Maps Project for CS032 by:
Samuel Kortchmar <skortchm> 
	&
Elias Martinez Cohen <emc3>

==========================================

* Stars:
	KDTree and other stuff came from Elias's (<emc3>) Stars project. This went virtually unmodified except for one small change in how the KDTree was built.
* Autocorrect:
	We used Sam's autocorrect (<skortchm>). We didn't have to change it much.
* Bacon:
	We used Sam's bacon project, but we basically re-wrote the whole thing so the BinarySearchFile class really does not resemble anything we have handed in before. We spent a lot of time making binary search as fast and awesome as possible. In fact we probably spent the most time optimizing and debugging and modding the BinarySearchFile class.

==========================================

* BUGS:
	- One bug in particular we couldn't get to:
		We paint ways by getting all nodes inside the range of the viewport and then painting all ways those nodes are attached to. 
		However, if a way is sufficiently large, the endpoints (nodes) will be outside the viewport of the map. 
		Thus the endpoints wont be returned when we search forr the viewport's range and consequently the super-long way won't be painted to the screen.
		This problem could be potentially worse when the user is zoomed in, but it requires that a Way is sufficiently long with turns or any other nodes along the straight line.
	- Message Box in the gui doesn't scroll up when there's a new message. The new messages are appended to the top, but the user would have to manually scroll up to see them. We simply did not have enough time to fix this as it was a pretty minor bug.
	- Pretty major bug we realized just before handin: The BSF method scanForwards is working harder than it should. We literally found this bug 15 minutes before handin. Will fix 4 traffic.

==========================================

* DESIGN:
	- The mapfactory controls most of the pieces of the different project to work together. It is a very powerful class. Its responsibilities include creating new KDTree, creating new RadixTree for autocorrect, and Querying the binary search files (BSFs) for info. It also is set up to split the BSF searches into blocks so that in the future threading would become easier. We did not have time to implement such a sophisticated threading structure but we will try to do so for Traffic.

==========================================

* OPTIMIZATIONS:
	We implemented various cacheing schemes:
		- In BSF, we cache the position of newlines to make searching for line stars and ends faster. These positions are stored in a Guava TreeSet.
		- In MapFactory, when we load Ways & Nodes from disk (from resource files) we also cache these in a HashMap. The idea here is that every time we need to go to disk to get a Way or a Node, we first check the HashMap to see if we have already grabbed the node and/or way info. This way we can get way/node info we have already cached in O(1) instead of however long it takes to Binary Search the file.