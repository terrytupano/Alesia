package plugins.hero.knn;
import java.util.*;

// Represents an observation vector with a class label and a list of features with corresponding values.  
public class Observation {
	private LinkedHashMap<Integer, Integer> features = new LinkedHashMap<Integer, Integer>();
	private String classLabel = null;

	// Mutators and accessors.
	public void putFeature(int feature, int value) {
		features.put(feature, value);
	}
	public void setClassLabel(String classLabel) {
		this.classLabel = classLabel;
	}
	public String getClassLabel() {
		return classLabel;
	}
	public Integer getFeature(int feature) {
		return features.get(feature);
	}
	public Set<Integer> getFeatures() {
		return features.keySet();
	} // Iterator over features.
}
