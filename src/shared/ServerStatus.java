package shared;

public class ServerStatus implements Response {

	private String msg;
	
	ServerStatus(String msg) {
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
