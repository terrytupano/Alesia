package plugins.hero.knn;
import java.util.*;

public class Distance {
	public static double getEuclid(Observation a, Observation b) {
		double squaredDistance = 0;

		// Iterates over the features of a.
		Set<Integer> aFeatures = a.getFeatures();
		for (int feature : aFeatures) {
			// If that key exists in b:
			if (b.getFeature(feature) != null)
				// Adds the squared difference between the feature values to the distance.
				squaredDistance += Math.pow((a.getFeature(feature) - b.getFeature(feature)), 2);
			else
				// Adds the squared feature value to the distance.
				squaredDistance += Math.pow(a.getFeature(feature), 2);
		}

		// Iterates over the features of b to find the features in b but not a.
		Set<Integer> bFeatures = b.getFeatures();
		for (int feature : bFeatures) {
			if (a.getFeature(feature) == null)
				// Adds the squared feature value to the distance.
				squaredDistance += Math.pow(b.getFeature(feature), 2);
		}

		return Math.sqrt(squaredDistance);
	}

	public static double getCosine(Observation a, Observation b) {
		// Simultaneously calculates the norm of a and the dot product of a and b.
		double normA = 0;
		int dotProduct = 0;
		Set<Integer> aFeatures = a.getFeatures();
		// Iterates over the features of a.
		for (int feature : aFeatures) {
			// Adds the square of the feature's value to the norm.
			normA += Math.pow(a.getFeature(feature), 2);

			// If that key exists in b:
			if (b.getFeature(feature) != null)
				// Adds the squared difference between the feature frequencies to the dot product.
				dotProduct += (a.getFeature(feature) * b.getFeature(feature));
		}
		normA = Math.sqrt(normA);

		// Calculates the norm of b.
		double normB = 0;
		Set<Integer> bFeatures = b.getFeatures();
		for (int feature : bFeatures)
			normB += Math.pow(b.getFeature(feature), 2);
		normB = Math.sqrt(normB);

		// Returns the cosine distance of the two Observation vectors (1-similarity).
		double distance = 1 - (dotProduct / (normA * normB));
		if (distance < 0) // Negative number due to floating-point error
			return 0;
		else
			return distance;
	}

}
