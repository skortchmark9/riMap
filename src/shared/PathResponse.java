package shared;

import java.util.LinkedList;
import java.util.List;

import maps.Way;

/**
 * A response from the server to the client in the event of a PathRequest
 * @author samkortchmar
 *
 */
public class PathResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	List<Way> _path; //The list of ways to be displayed on the map.
	String _msg; //A status message - either timeout, success, or no path.
	
	public PathResponse(List<Way> path) {
		//If there is a List<Way> then a path was found, so the message will
		//always be success.
		_path = path;
		_msg = "success!";
	}
	
	public PathResponse(String msg) {
		//If there was no path found, _path will always be empty.
		_path = new LinkedList<>();
		_msg = msg;
	}
	
	public List<Way> getPath() {
		return _path;
	}
	
	public String getMsg() {
		return _msg;
	}
	
	@Override
	public ResponseType getType() {
		return ResponseType.PATH;
	}
	
	@Override
	public String toString() {
		return String.format("PathResponse List<Way> = %s message = %s", _path, _msg);
	}
}
