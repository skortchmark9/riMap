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
	private String _sourceXS1 = "", _sourceXS2 = "";
	private String _destXS1 = "", _destXS2 = "";
	
	public PathRequest(Node source, Node dest, int timeout) {
		_source = source;
		_dest = dest;
		_timeout = timeout;
	}
	
	public PathRequest(Node source, Node dest, int timeout, String sxs1, String  sxs2, String dxs1, String dxs2) {
		_source = source;
		_dest = dest;
		_timeout = timeout;
		_sourceXS1 = sxs1.equals("Cross Street 1") ? "" : sxs1;
		_sourceXS2 = sxs2.equals("Cross Street 2") ? "" : sxs2;
		_destXS1 = dxs1.equals("Cross Street 1") ? "" : dxs1;
		_destXS2 = dxs2.equals("Cross Street 2") ? "" : dxs2;
	}
	
	public Node getSource() {
		return _source;
	}
	
	public String getCrossStreet(boolean source, int oneOrTwo) {
		switch (oneOrTwo) {
		case 1: return source ? _sourceXS1 : _destXS1;
		case 2: return source ? _sourceXS2 : _destXS2;
		default: return "";
		}
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
	
	@Override
	public String toString() {
		return String.format("PathReq source = %s dest = %s timeout = %s\n"
				+ "START xs1 = %s xs2 = %s"
				+ "\nEND xs1 = %s xs2 = %s",
				_source, _dest, _timeout, _sourceXS1, _sourceXS2, _destXS1, _destXS2);
	}
}
