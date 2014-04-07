package shared;

import java.util.List;
/**
 * A Response to an Autocorrect Request
 * @author samkortchmar
 */
public class AutocorrectResponse implements Response {
	
	private static final long serialVersionUID = 1L;
	List<String> autocorrections; //The autocorrections to be suggested.
	int boxNo; //The box that requested autocorrections.
	
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
	@Override
	public String toString() {
		return String.format("ACResponse: autocorrections = %s boxNo = %s", autocorrections, boxNo);
	}
}
