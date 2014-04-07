package shared;

import java.util.List;

public class AutocorrectResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	List<String> autocorrections;
	int boxNo;
	
	public AutocorrectResponse(List<String> autocorrections, int boxNo) {
		this.autocorrections = autocorrections;
		this.boxNo = boxNo;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.AUTO_CORRECTIONS;
	}
	
	public List<String> getAutocorrections() {
		return autocorrections;
	}
	
	public int getBoxNo() {
		return boxNo;
	}

}
