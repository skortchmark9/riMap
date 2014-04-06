package shared;

import java.util.LinkedList;
import java.util.List;

import maps.Way;

public class PathResponse implements Response {
	List<Way> _path;
	String _msg;
	
	public PathResponse(List<Way> path, String msg) {
		_path = path;
		_msg = msg;
	}
	
	public PathResponse(String msg) {
		_path = new LinkedList<>();
		_msg = msg;
	}
	
	public List<Way> getPath() {
		return _path;
	}
	
	@Override
	public ResponseType getType() {
		return ResponseType.PATH;
	}
}
