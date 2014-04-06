package shared;

import maps.Node;

public class PathRequest implements Request {
	private final Node _source, _dest;
	
	PathRequest(Node source, Node dest) {
		_source = source;
		_dest = dest;
	}
	
	public Node getSource() {
		return _source;
	}
	
	public Node getDest() {
		return _dest;
	}
	@Override
	public RequestType getType() {
		return RequestType.PATH;
	}
}
