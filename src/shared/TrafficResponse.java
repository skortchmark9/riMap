package shared;


public class TrafficResponse implements Response {
	private static final long serialVersionUID = 1L;
	private final String _name;
	private final Double _val;
	private final boolean _status;
	
	/**
	 * Default constructor.
	 * wrap the name and value of a traffic 
	 * update in this response
	 * @param name - the name of the way whose traffic info is being updated
	 * @param val - the amount of traffic on the way
	 */
	public TrafficResponse(String name, Double val, boolean trafficStatus) {
		_name = name;
		_val = val;
		_status = trafficStatus;
	}

	@Override
	public ResponseType getType() {
		return ResponseType.TRAFFIC;
	}
	
	/**
	 * @return
	 * the name of the way whose traffic val is being updated
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @return
	 * the amount of traffic on the way
	 */
	public Double getVal() {
		return _val;
	}

	public boolean getStatus() {
		return _status;
	}

}
