package shared;

import java.io.Serializable;

public interface Response extends Serializable {
	public enum ResponseType {
		AUTO_CORRECTIONS,
		NEAREST_NEIGHBORS,
		WAYS,
		PATH,
		SERVER_STATUS
	}
	ResponseType getType();
}