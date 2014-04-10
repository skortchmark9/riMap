package shared;

import java.io.Serializable;

/**
 *   CLIENT -> SERVER
 * This interface is responsible for all communication from the client
 * to the server. Because all Requests implement the same interface, there
 * is no confusion about what the server is receiving. Usually, the server
 * will switch on the RequestType and then determine how to generate the
 * appropriate Response.
 * @author samkortchmar
 *
 */
public interface Request extends Serializable {

	public enum RequestType {
		AUTO_CORRECTIONS, //A search for autocorrections - used by text boxes.
		NEAREST_NEIGHBORS, //A search for nearest neighbors - used for clicking.
		WAYS, //A search for ways - rendering the lines on the map.
		PATH, //A search for a path - rendering a path between points on map.
	}
	RequestType getType();
	String toString();
}