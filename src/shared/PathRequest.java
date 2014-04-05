package shared;

import maps.Node;

public class PathRequest implements Request {
	Node start, end;
	
	PathRequest(Node start, Node end) {
		this.start = start;
		this.end = end;
	}
	@Override
	public RequestType getType() {
		return RequestType.PATH;
	}
}
