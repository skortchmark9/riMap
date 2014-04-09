CS032_Maps
==========

Maps Project for CS032 by:
Samuel Kortchmar <skortchm> 
	&
Elias Martinez Cohen <emc3>

==========================================

* PROTOCOL:

Our protocol for sending information between the server and client is simple but powerful. The backbone of the communication is two interfaces - Request and Response. All client requests are wrapped in Request objects and all server responses are wrapped in Response objects. A server doesn't necessarily need to receive a Request in order to receive a Response (& vice versa), but usually that's how it works. Both interfaces expose an enum which allows them to be processed appropriately. The following table attempts to detail pairs of Requests and Responses.
Requests & Responses
-----------------------------------------------------------------------------------------
	Request Name	Enum			Response Name	Enum			|
	Autocorrect	AUTO_CORRECTIONS	Autocorrect	AUTO_CORRECTIONS	|
	Neighbors	NEAREST_NEIGHBORS	Neighbors	NEAREST_NEIGHBORS	|
	Path		PATH			Path		PATH			|
	Way		WAYS			Way		WAYS			|
						Traffic		TRAFFIC			|
						ServerStatus	SERVER_STATUS		|
						ClientConnectionCLIENT_CONNECT		|
-----------------------------------------------------------------------------------------
The first four are fairly self-explanatory. The last 3 Responses are issued without prompting by the server
to inform clients about 1.) Traffic conditions received from the traffic bot.
			2.) Server health and backend readiness.
			3.) Initialization information for the client.

Requests and Responses, when generated, are added to a queue which is constantly serviced by a receiving/pushing thread. Variants of these threads on the client/server side call readObject or writeObject. When reading objects, we must cast them to Requests or Responses immediately, and then pass them to other methods for processing.

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
* DESIGN DETAILS

After Maps, our project actually didn't require many changes to its underlying projects. For example, Djikstra's algorithm was easily modified to account for traffic by simply multiplying the distance of a way by its traffic value, which we've stored in a hashmap. Similarly, the chatroom from lab 6 provided much of the structure for our Client/Server handling and connections.

In maps, we already had a reasonably clean break between our backend and our frontend - the bridge was a static called MapFactory. We still use MapFactory, but almost all of its functionality has been moved to the server side of things.

==========================================



* OPTIMIZATIONS:
	We implemented various cacheing schemes:
		- In BSF, we cache the position of newlines to make searching for line stars and ends faster. These positions are stored in a Guava TreeSet. The TreeSet allows us to create ranges that define the length of a line. The gaps between the ranges represent newlines.
		- In MapFactory, when we load Ways & Nodes from disk (from resource files) we also cache these in a HashMap. The idea here is that every time we need to go to disk to get a Way or a Node, we first check the HashMap to see if we have already grabbed the node and/or way info. This way we can get way/node info we have already cached in O(1) instead of however long it takes to Binary Search the file.
