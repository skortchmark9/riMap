package shared;

import java.io.Serializable;

public interface Request extends Serializable {

	public enum RequestType {
		AUTO_CORRECTIONS,
		NEAREST_NEIGHBORS,
		WAYS,
		PATH,
		EXIT
	}
	RequestType getType();
}