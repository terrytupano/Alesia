package plugins.hero.knn;

// A distance and a class label, used to calculate nearest neighbours. 
public class Neighbour implements Comparable<Neighbour> {
	public double distance;
	public String classLabel;

	public Neighbour(double distance, String classlabel) {
		this.distance = distance;
		this.classLabel = classlabel;
	}

	// Neighbours are compared based on distance.
	public int compareTo(Neighbour other) {
		return Double.compare(this.distance, other.distance);
	}

	public String toString() {
		String out = "Distance: " + distance + ", class label: " + classLabel;
		return out;
	}
}
