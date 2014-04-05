package shared;

public class AutocorrectRequest implements Request {
	public final String input;
	
	public AutocorrectRequest(String input) {
		this.input = input;
	}
	
	@Override
	public RequestType getType() {
		return RequestType.AUTO_CORRECTIONS;
	}
}
