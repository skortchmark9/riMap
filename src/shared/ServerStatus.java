package shared;

/**
 * A somewhat unique Response because it's not actually related to any
 * client Requests. Instead, this is the server's way of communicating
 * status information to its clients.
 * @author samkortchmar
 *
 */
public class ServerStatus implements Response {

	private static final long serialVersionUID = 1L;
	private String _msg; //The status message to be conveyed.
	private boolean _up;
	
	public ServerStatus(boolean up, String msg) {
		_msg = msg;
		_up = up;
	}
	
	public ServerStatus(boolean up) {
		_up = up;
		_msg = _up ? "Server ready" : "Server not ready";
	}
	
	@Override
	public ResponseType getType() {
		return ResponseType.SERVER_STATUS;
	}
	
	public String getMsg() {
		return _msg;
	}
	
	public boolean isServerUp() {
		return _up;
	}
	
	@Override
	public String toString() {
		return String.format("ServerStatus msg = %s and status condition is %s", _msg, _up);
	}

}
