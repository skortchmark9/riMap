package shared;

public class ExitRequest implements Request {

	private static final long serialVersionUID = 1L;

	@Override
	public RequestType getType() {
		return RequestType.EXIT;
	}
}
