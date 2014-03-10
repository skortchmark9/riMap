package baconGraph;

import java.util.LinkedList;
import java.util.List;


/**
 * This class holds information about each actor in the baconGraph.
 * @author skortchm
 *
 */
class Actor {
	String name; //The name of the actor
	String ID; //The actor's unique ID
	ActorType actorType; //The actor's position in the graph. Most often NODE.
	String commaSeparatedMovies = null; //Any movies the actor might appear in.
	enum ActorType {ROOT, NODE}
	Resources r;

	Actor(String name, String ID, ActorType at, Resources r) {
		this.name = name;
		this.ID = ID;
		this.actorType = at;
		this.r = r; 
	}

	Actor(String name, String ID, Resources r) {
		this(name, ID, ActorType.NODE, r);
	}

	Actor(String ID, Resources r) {
		this(ID, ActorType.NODE, r);
	}

	/** A smart constructor that fills in the other pieces of information
	 * about an actor
	 * @param nameOrID - either the unique identifier or the name.
	 * @param ActorType - in most cases, root Actors will be started with 
	 * names, and node actors will be started with IDs. */
	Actor(String nameOrID, ActorType at, Resources r) {
		actorType = at;
		this.r = r;
		switch (actorType) {
		case ROOT: //Searching by name.
			name = nameOrID;
			ID = r.indexFile.getXByY("id", name);
			if (ID == null) {
				System.out.println(String.format("ERROR: Could not find: %s in Index File", nameOrID));
				System.exit(1);
			}
			break;
		default:
			this.ID = nameOrID; //Searching by ID.
			//getXsbyY also returns info about the films the actor appeared in.
			String[] nameAndMovies = r.actorsFile.getXsByY(nameOrID, "name", "film");
			if (nameAndMovies != null) {
				if (nameAndMovies[0].length() > 0) {
					name = nameAndMovies[0];
				}
				if (nameAndMovies[1].length() > 0) {
					name = nameAndMovies[1];
				}
			}
			else {
				System.out.println("ERROR: COULD NOT FIND ACTOR INFORMATION");
				System.exit(1);
			}
		}
	}

	/** 
	 * To save binary search calls, this method allows us to 
	 * add movies easily while constructing the graph. 
	 * f we have that information available to us.
	 * @param commaSeparatedMovies - a string of movie IDs.
	 */
	void addMovies(String commaSeparatedMovies) {
		this.commaSeparatedMovies = commaSeparatedMovies;
	}


	/** Gets a list of Movie objects from the current actor.
	 *  @return - movies the actor appeared in.*/
	List<Movie> getMovies() {
		//If we don't have this information already, we'll need to find it in the actorsFile.
		List<Movie> movies = new LinkedList<>();
		if (commaSeparatedMovies == null) {
			//Attempts to find films the actor has appeared in.
			if (ID == null) {
				System.out.println("ERROR: ACTOR NOT FOUND");
				System.exit(1);
			} 
			else {
			commaSeparatedMovies = r.actorsFile.getXByY("film", ID);
			}
		}
		if (commaSeparatedMovies != null) {
			String[] comSeparatedMovies = Constants.comma.split(commaSeparatedMovies);
				for(String movieID : comSeparatedMovies) {
					//We want to avoid binary searches if we can.
					String[] titleAndStarringActors = r.moviesFile.getXsByY(movieID, "name", "starring");
					if (titleAndStarringActors != null) {
						String title = titleAndStarringActors[0];
						String starringActors = titleAndStarringActors[1];
						List<String> actorIDs = new LinkedList<>();
						for(String s : Constants.comma.split(starringActors)) {
							if (!s.equals("")) { //As usual, we hate the empty string.
								actorIDs.add(s);
							}
						}
						if (title.length() > 0) {
							movies.add(new Movie(title, actorIDs, r));
						}
					}
				}
				return movies;
			}
		return movies;
	}

	public String getLastInitial() {
		//Check if the name contains a number, i.e. '50 Cent'
		String digits = name;
		digits = Constants.digits.matcher(digits).replaceAll("");
		//			digits = digits.replaceAll("\\D", "");
		if (digits.length() > 0) {
			return digits;
		}

		//Finds the last instance of whitespace in the string and
		//gets the letter directly after it. If there is none,
		//we can assume the name is one word, and so we just want
		//the first character.
		int lastWhitespaceIndex = -1;
		boolean encounteredWord = false;
		for(int i = name.length() - 1; i > -1; i--) {
			if (Character.isWhitespace(name.charAt(i)) && encounteredWord) {
				lastWhitespaceIndex = i;
				break;
			} else {
				encounteredWord = true;
			}
		}
		try {
			String initial =  Character.toString(name.charAt(lastWhitespaceIndex + 1));
			return initial;
		} catch (StringIndexOutOfBoundsException s) {
			System.out.println("ERROR: Problem with NAME: " + name);
			return null;
		}
	}



	public String getFirstInitial() {
		//Check if the name contains a number, i.e. '50 Cent'
		String digits = name;
		digits = Constants.digits.matcher(digits).replaceAll("");

		if (digits.length() > 0) {
			return digits;
		}
		try {
			String initial =  Character.toString(name.charAt(0));
			return initial;
		} catch (StringIndexOutOfBoundsException s) {
			System.out.println("ERROR: Problem with NAME: " + name);
			return null;
		}
	}



	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return String.format("Name: %s, ID: %s", name, ID);
	}


	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Actor)) return false;

		Actor s = (Actor) o;
		return (this.ID.equals(s.ID));
	}


	@Override
	/**
	 * I was going to make this more complicated, see the commented code,
	 * but I decided it would be best to keep things simple. Universal
	 * hashcodes make keeping track of nodes and actors a lot easier. 
	 */
	public int hashCode() {
		/*final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;*/
		return name.hashCode();
	}
}
