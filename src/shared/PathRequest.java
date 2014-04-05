package shared;

import maps.Node;

public class PathRequest implements Request {
	public final Node _source, _dest;
	
	PathRequest(Node source, Node dest) {
		_source = source;
		_dest = dest;
	}
	@Override
	public RequestType getType() {
		return RequestType.PATH;
	}
}
