package shared;

import java.util.List;

public class AutocorrectResponse implements Response {
	
	List<String> autocorrections;
	
	public AutocorrectResponse(List<String> autocorrections) {
		this.autocorrections = autocorrections;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.AUTO_CORRECTIONS;
	}
	
	public List<String> getAutocorrections() {
		return autocorrections;
	}

}
