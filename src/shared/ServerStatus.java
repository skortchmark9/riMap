package shared;

public class ServerStatus implements Response {

	@Override
	public ResponseType getType() {
		return ResponseType.SERVER_STATUS;
	}
}
