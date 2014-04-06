package shared;

import maps.Node;

public class PathRequest implements Request {
	private final Node _source, _dest;
	private final int _timeout;
	
	public PathRequest(Node source, Node dest, int timeout) {
		_source = source;
		_dest = dest;
		_timeout = timeout;
	}
	
	public Node getSource() {
		return _source;
	}
	
	public Node getDest() {
		return _dest;
	}
	
	public int getTimeout() {
		return _timeout;
	}
	@Override
	public RequestType getType() {
		return RequestType.PATH;
	}
}
