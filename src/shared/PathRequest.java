package shared;

import maps.Node;

/**
 * A request for a path from the client to the server.
 * @author samkortchmar
 */
public class PathRequest implements Request {

	private static final long serialVersionUID = 1L;
	private final Node _source, _dest; //the source and destination of the path
	private final int _timeout; //the maximum time to spend searching
	
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
