package shared;

import java.io.Serializable;

/**
 * SERVER -> CLIENT
 * A Response is the type of object read by the Client. The server sends Responses
 * based on client Requests. Because all Responses implement the same interface,
 * the client will always know what to do with them. Usually, it will switch on
 * the ResponseType and then determine how to proceed.
 * @author samkortchmar
 *
 */
public interface Response extends Serializable {
	public enum ResponseType {
		AUTO_CORRECTIONS, //Wraps a list of suggestions for the text box.
		NEAREST_NEIGHBORS, //Wraps a list of neighbors for the map pane.
		WAYS, //Wraps a list of ways for the map pane to render.
		PATH, //Wraps a list of ways for the map pane to draw as a path.
		SERVER_STATUS //Wraps a message to be displayed on the GUI.
	}
	ResponseType getType();
}