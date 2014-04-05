package shared;

public class PathResponse implements Response {

	@Override
	public ResponseType getType() {
		return ResponseType.PATH;
	}
}
