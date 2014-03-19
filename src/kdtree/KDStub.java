package kdtree;

public class KDStub implements KDimensionable {

	double[] domain;

	public KDStub(double x, double y) {
		domain = new double[2];
		domain[0] = x;
		domain[1] = y;
	}

	@Override
	public double[] getCoordinates() {
		return domain;
	}

	@Override
	public int compareAxis(KDimensionable kd, int axis) {
		if (axis > 1 || axis < 0) {
			throw new IllegalArgumentException(String.format("Axis %d out of range: must be 1 or 0", axis));
		}
		double mine = domain[axis];
		double other = kd.getCoordinates()[axis];
		return Double.compare(mine, other);
	}

	@Override
	public int getNumDimensions() {
		return domain.length;
	}

	/**
	 * Note this distance is SQUARED
	 */
	@Override
	public double distanceTo(KDimensionable kd) {
		double dist = 0;
		if (this.getNumDimensions() != kd.getNumDimensions()) {
			throw new IllegalArgumentException("Wrong dimensionality of objects");
		}
		for(int i = 0; i < getNumDimensions(); i++) {
			dist += Math.pow(domain[i] - kd.getCoordinates()[i], 2);
		}
		return dist;
	}

}
