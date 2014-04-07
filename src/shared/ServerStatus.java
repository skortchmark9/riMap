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
	
	public ServerStatus(String msg) {
		_msg = msg;
	}
	@Override
	public ResponseType getType() {
		return ResponseType.SERVER_STATUS;
	}
	
	public String getMsg() {
		return _msg;
	}
	
	@Override
	public String toString() {
		return String.format("ServerStatus msg = %s", _msg);
	}

}
