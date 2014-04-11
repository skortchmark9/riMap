package shared;

/**
 * A Request for autocorrections - given the specific input
 * @author samkortchmar
 */
public class AutocorrectRequest implements Request {

	private static final long serialVersionUID = 1L;
	private final String input; //The string to be autocorrected
	private final int boxNo; //the current textfield on the front end.
	
	public AutocorrectRequest(String input, int boxNo) {
		this.input = input;
		this.boxNo = boxNo;
	}
	
	public String getInput() {
		return input;
	}
	
	public int getBoxNo() {
		return boxNo;
	}
	
	@Override
	public RequestType getType() {
		return RequestType.AUTO_CORRECTIONS;
	}
	
	@Override
	public String toString() {
		return String.format("ACReq: input = %s boxNo = %s", getInput(), getBoxNo());
	}
}
