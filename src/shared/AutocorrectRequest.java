package shared;

public class AutocorrectRequest implements Request {
	private final String input;
	private final int boxNo;
	
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
}
