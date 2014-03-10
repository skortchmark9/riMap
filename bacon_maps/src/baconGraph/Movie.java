package baconGraph;

import java.util.LinkedList;
import java.util.List;
import baconGraph.BinarySearchFile.ParseType;

/** 
 * Object representation of movie. 
 * @author skortchm
 *
 */
public class Movie {
	String title; // movie title.
	List<String> actorIDs; //actors who appeared in it.
	Resources r;

	Movie(String title, List<String> ids, Resources r) {
		this.title = title;
		this.actorIDs = ids;
		this.r = r;
	}

	public String getTitle() {
		return title;
	}

	/** 
	 * Searches the actors file for actors whose IDs are stored 
	 * in the movie and returns them. 
	 * @return - a list of actors who were in the movie.
	 */
	public List<Actor> getActors() {
		List<Actor> actors = new LinkedList<Actor>();
		for(String actorID : actorIDs) {
				String[] nameAndMovies = r.actorsFile.getXsByY(actorID, "name", "film");
				if (nameAndMovies != null) {
					String name = nameAndMovies[0];
					String movies = nameAndMovies[1];
					if (name.length() > 0) {
						Actor a = new Actor(name, actorID, r);
						if (movies.length() > 0) {
							a.addMovies(movies);
						}
						actors.add(a);
					}
				}
		}
		return actors;
	}

	@Override
	public String toString() {
		return title + actorIDs.size();
	}
}