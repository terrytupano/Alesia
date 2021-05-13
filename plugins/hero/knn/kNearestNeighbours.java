package plugins.hero.knn;
import java.util.*;

public class kNearestNeighbours {
	// Performs leave-one-out cross validation across the entire dataset for each value of K from 1-10 for both weighted
	// and unweighted classifiers.
	// Prints the percentage accuracy achieved with each configuration.
	// NB: takes several minutes to run.
	public static void main(String[] args) {
		ArrayList<Observation> inputObservations = FileHandler.read("data/observations.mtx", "data/classes.labels");

		System.out.println(
				"LEAVE-ONE-OUT CROSS VALIDATION FOR WEIGHTED KNN\n***********************************\nK\t\tAccuracy");
		// For each k.
		for (int K = 1; K <= 10; K++) {
			int correct = 0;
			int incorrect = 0;

			// Loops over the dataset of Observation objects.
			for (int i = 0; i < inputObservations.size(); i++) {
				// Gets the ith Observation, records its label and removes it from the list.
				Observation test = inputObservations.get(i);
				String trueLabel = test.getClassLabel();
				inputObservations.remove(i);

				// Resets the Observation's label and tries to guess it using the classifier.
				test.setClassLabel(null);
				test = classify(test, inputObservations, K, "cosine", true);

				// Records whether the guess was correct.
				if (test.getClassLabel().equals(trueLabel)) {
					correct += 1;
				} else {
					incorrect += 1;
				}

				// Resets the label and adds the Observation back to the list.
				test.setClassLabel(trueLabel);
				inputObservations.add(i, test);
			}

			// Calculates and prints the proportion of correct answers.
			double percentageCorrect = (double) correct / (correct + incorrect) * 100;
			System.out.println(K + "\t\t" + percentageCorrect);
		}

		System.out.println(
				"LEAVE-ONE-OUT CROSS VALIDATION FOR UNWEIGHTED KNN\n***********************************\nK\t\tAccuracy");
		// For each k.
		for (int K = 1; K <= 10; K++) {
			int correct = 0;
			int incorrect = 0;

			// Loops over the dataset of Observation objects.
			for (int i = 0; i < inputObservations.size(); i++) {
				// Gets the ith Observation, records its label and removes it from the list.
				Observation test = inputObservations.get(i);
				String trueLabel = test.getClassLabel();
				inputObservations.remove(i);

				// Resets the Observation's label and tries to guess it using the classifier.
				test.setClassLabel(null);
				test = classify(test, inputObservations, K, "cosine", false);

				// Records whether the guess was correct.
				if (test.getClassLabel().equals(trueLabel)) {
					correct += 1;
				} else {
					incorrect += 1;
				}

				// Resets the label and adds the Observation back to the list.
				test.setClassLabel(trueLabel);
				inputObservations.add(i, test);
			}

			// Calculates and prints the proportion of correct answers.
			double percentageCorrect = (double) correct / (correct + incorrect) * 100;
			System.out.println(K + "\t\t" + percentageCorrect);
		}
	}

	/*
	 * Takes a Observation of unknown class and gives it a class label based on the votes of its k-nearest neighbours.
	 * Parameters: - test: a Observation object of unknown class - train: an ArrayList of Observations of known class -
	 * K: the number of neighbours which will vote on the class label - distanceMeasure: the method by which neighbour
	 * distances will be calculated - weighted: true if neighbour votes are to be weighted based on closeness
	 */
	public static Observation classify(Observation test, ArrayList<Observation> train, int K, String distanceMeasure,
			boolean weighted) {
		// Calculates the neighbour distances between the test example and each training example.
		Neighbour[] neighbours = new Neighbour[train.size()];
		for (int i = 0; i < neighbours.length; i++) {
			// Creates a new Neighbour object for that distance with the training example's label.
			if (distanceMeasure == "euclidian")
				neighbours[i] = new Neighbour(Distance.getEuclid(train.get(i), test), train.get(i).getClassLabel());
			else if (distanceMeasure == "cosine")
				neighbours[i] = new Neighbour(Distance.getCosine(train.get(i), test), train.get(i).getClassLabel());
			else
				throw new IllegalArgumentException("Invalid argument for distance measure: " + distanceMeasure);
		}

		// Sorts the array of neighbours by distance.
		Arrays.sort(neighbours);

		// Calculates the votes of the K nearest neighbours, unweighted or weighted.
		String decision = null;
		if (weighted == false) { // Unweighted
			LinkedHashMap<String, Integer> votes = new LinkedHashMap<String, Integer>();
			for (int i = 0; i < K; i++) {
				// Gets the label of the ith nearest neighbour.
				String label = neighbours[i].classLabel;

				// Increments the vote for that neighbour's class if already in the list.
				if (votes.containsKey(label))
					votes.put(label, votes.get(label) + 1);
				// Adds a vote for that neighbour's class if it is not in the list.
				else
					votes.put(label, 1);
			}

			// Sets the decision as the label with the greatest number of votes.
			double maxVote = 0;
			for (Map.Entry<String, Integer> vote : votes.entrySet()) {
				if (vote.getValue() > maxVote) {
					decision = vote.getKey();
					maxVote = vote.getValue();
				}
			}
		} else { // Weighted
			LinkedHashMap<String, Double> votes = new LinkedHashMap<String, Double>();
			for (int i = 0; i < K; i++) {
				// Gets the label of the ith nearest neighbour.
				String label = neighbours[i].classLabel;

				// Adds the weighted distance to the vote for that neighbour's class if already in the list.
				if (votes.containsKey(label))
					votes.put(label, votes.get(label) + 1.0 / neighbours[i].distance);
				// Adds a vote with the weighted distance for that neighbour's class if not in the list.
				else
					votes.put(label, 1.0 / neighbours[i].distance);
			}

			// Sets the decision as the label with the greatest number of votes.
			double maxVote = -1.0;
			for (Map.Entry<String, Double> vote : votes.entrySet()) {
				if (Math.abs(vote.getValue()) > maxVote) {
					decision = vote.getKey();
					maxVote = vote.getValue();
				}
			}
		}

		// Sets the test example's label to that label.
		test.setClassLabel(decision);

		return test;
	}
}
