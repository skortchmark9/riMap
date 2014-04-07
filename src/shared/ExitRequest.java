package shared;

/**
 * A special request to be sent by the frontEnd to the client.
 * It isn't sent to the server, but causes the client to shut down cleanly.
 * @author samkortchmar
 *
 */
public class ExitRequest implements Request {

	private static final long serialVersionUID = 1L;

	@Override
	public RequestType getType() {
		return RequestType.EXIT;
	}
}
