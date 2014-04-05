package shared;

public class ExitRequest implements Request {

	@Override
	public RequestType getType() {
		return RequestType.EXIT;
	}
}
