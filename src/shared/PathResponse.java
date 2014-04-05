package shared;

import java.util.List;
import maps.Way;

public class PathResponse implements Response {
	List<Way> _path;
	
	public PathResponse(List<Way> path) {
		_path = path;
	}
	
	public List<Way> getPath() {
		return _path;
	}
	
	@Override
	public ResponseType getType() {
		return ResponseType.PATH;
	}
}
