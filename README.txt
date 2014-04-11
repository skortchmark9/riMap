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
	Bugs for TRAFFIC and NOT maps:
	- when the client starts and the traffic server is not running, sometimes it tells the user that the traffic server is unavailable but most of the time it just doesn't work.

==========================================
* DESIGN DETAILS

After Maps, our project actually didn't require many changes to its underlying projects. For example, Djikstra's algorithm was easily modified to account for traffic by simply multiplying the distance of a way by its traffic value, which we've stored in a hashmap. Similarly, the chatroom from lab 6 provided much of the structure for our Client/Server handling and connections.

In maps, we already had a reasonably clean break between our backend and our frontend - the bridge was a static called MapFactory. We still use MapFactory, but almost all of its functionality has been moved to the server side of things.

our protocol uses object input stream and object output stream, but we have two types of objects being sent across the network: Responses and Requests. Both Response and Request objects are interfaces. these interfaces contain an ENUM and a method called getType() the getType method relies on the enum, for example if you create an autocorrect response object accross the network, the receiver (client) will only know that it is a response object. It can then call getType on the generic response object and depending on the value of the returned enum it knows what type of response it is. it can then cast the response to , say, AutoCorrectResponse and handle it appropriately.

The protocol design was made much easier by the choice to use ObjectInputStream and ObjectOutputStream as we didn't have to stringify anything to send it across the network; we could just send the Response objects as they were. Response / Request objects simply contained the necessary info they needed. For example, A request to Get ways in range only needs to contain the minimum / maximum latitude longitude pairs defining the bounding box of the ways we want to get. Coversely, the way response object simple contains a list of ways within the range of the response.

we made it a point to have the client and server operate independently of eachother as well as the traffic bot. The server will run without a traffic bot but will simply alert the user that the traffic bot is unavailable. If the client starts before a server is available, it will wait about 1 minute for a server to become available before quitting. If the traffic bot quits while a server/client is running, the user will see a message in the GUI alerting them to the status of the traffic bot. Likewise, if the server quits and the client is still running, a user will see another pop up message saying that the server is unavailable. Also, the client caches all the ways it receives from server,so if you have a large cache of ways, even when the server exits the client should be able to move around in the range which it has chached!

There are a bunch of extra festures and cool gizmos in our traffic project. We hope you appreciate them as much as we loved developing them!

==========================================
* RUNNING
	From thew project directory, run 'ant' to build.

	To run:
		Server-
			from the project directory, run 
			bin/server (with the appropriate args)
		Client-
			from the project directory, run
			bin/client (with the appropriate args)

	The usage of both programs is printed if inappropriate arguments are supplied.

==========================================

* OPTIMIZATIONS:
	We implemented various cacheing schemes:
		- In BSF, we cache the position of newlines to make searching for line stars and ends faster. These positions are stored in a Guava TreeSet. The TreeSet allows us to create ranges that define the length of a line. The gaps between the ranges represent newlines.
		- In MapFactory, when we load Ways & Nodes from disk (from resource files) we also cache these in a HashMap. The idea here is that every time we need to go to disk to get a Way or a Node, we first check the HashMap to see if we have already grabbed the node and/or way info. This way we can get way/node info we have already cached in O(1) instead of however long it takes to Binary Search the file.

		- filtering out smaller ways on zoom out
		- Dijkstra's has a timeout. most paths (even huge ones) are able to be found within 5 seconds.
		- lots and lots of threading all over the place
		- local cacheing of ways in the event of server loss

==========================================

Testing:
	some of our previous tests from projects still run and verify our program, but the handin will not include the files needed for testing (bacon files and stars files are too large). the file paths in the JUnit tests will need to be changed to the files on the department file system. Given more time our JUnit would have more rigorously tested the functionality of Maps in particular, but we saw the tests provided by previous projects as sufficient given the time constraints.

	Given the difficulty of creating system tests for a project such as Traffic, we did most of our testing by hand in the GUI. some of the things we did were:

		-running up to 5 clients on one server
		- quitting server while client is runnning , quitting the client while the server is runnning, quitting the traffic bot, etc.
		- trying strange autocorrect queries, especially for streets that do not exists (i.e. Waterman Avenue should never show up although Waterman and avenue are valid suggestions for other things)
		- many "debug" printouts -- Util has a method debug() that onlu prints if Constants.DEBUG_MODE is set to true.
		- 