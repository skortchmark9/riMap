package shared;

public class ServerStatus implements Response {

	private static final long serialVersionUID = 1L;
	private String msg;
	
	public ServerStatus(String msg) {
		this.msg = msg;
	}
	@Override
	public ResponseType getType() {
		return ResponseType.SERVER_STATUS;
	}
	
	public String getMsg() {
		return msg;
	}
}
