package shared;


public class TrafficResponse implements Response {
	private static final long serialVersionUID = 1L;
	String _name;
	Double _val;
	
	/**
	 * Default constructor.
	 * wrap the name and value of a traffic 
	 * update in this response
	 * @param name - the name of the way whose traffic info is being updated
	 * @param val - the amount of traffic on the way
	 */
	public TrafficResponse(String name, Double val) {
		_name = name;
		_val = val;
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

}
