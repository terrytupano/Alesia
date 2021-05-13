package plugins.hero.knn;
import java.util.*;

// NB: Run with -ea flag. 
public class Tests {
	public static void main(String[] args) {
		testEuclid();
		testCosine();
		testClassify();

		System.out.println("TESTS OK");
	}

	public static void testEuclid() {
		Observation testA = new Observation();
		testA.putFeature(1, 5);
		testA.putFeature(2, 10);
		testA.putFeature(3, 1);
		Observation testB = new Observation();
		testB.putFeature(1, 2);
		testB.putFeature(3, 3);
		testB.putFeature(4, 2);

		assert (Distance.getEuclid(testA, testB) == 10.816653826391969);
	}

	public static void testCosine() {
		Observation testA = new Observation();
		testA.putFeature(1, 5);
		testA.putFeature(2, 3);
		testA.putFeature(3, 2);
		testA.putFeature(5, 2);
		Observation testB = new Observation();
		testB.putFeature(1, 3);
		testB.putFeature(2, 2);
		testB.putFeature(3, 1);
		testB.putFeature(4, 1);
		testB.putFeature(5, 1);
		testB.putFeature(6, 1);

		assert (Distance.getCosine(testA, testB) == 0.06439851429360033);
	}

	public static void testClassify() {
		ArrayList<Observation> train = new ArrayList<Observation>();
		Observation testA = new Observation();
		testA.putFeature(1, 5);
		testA.putFeature(2, 8);
		testA.putFeature(3, 3);
		Observation testB = new Observation();
		testB.putFeature(1, 3);
		testB.putFeature(2, 5);
		testB.setClassLabel("B");
		train.add(testB);
		Observation testC = new Observation();
		testC.putFeature(1, 1);
		testC.putFeature(2, 9);
		testC.setClassLabel("C");
		train.add(testC);
		Observation testD = new Observation();
		testD.putFeature(1, 2);
		testD.putFeature(2, 10);
		testD.putFeature(3, 1);
		testD.setClassLabel("B");
		train.add(testD);
		Observation testE = new Observation();
		testE.putFeature(1, 3);
		testE.putFeature(2, 1);
		testE.putFeature(3, 8);
		testE.setClassLabel("D");
		train.add(testE);
		Observation testF = new Observation();
		testF.putFeature(1, 5);
		testF.setClassLabel("D");
		train.add(testF);
		Observation testG = new Observation();
		testG.putFeature(1, 7);
		testG.putFeature(2, 2);
		testG.putFeature(3, 11);
		testG.setClassLabel("E");
		train.add(testG);
		Observation testH = new Observation();
		testH.putFeature(1, 9);
		testH.putFeature(4, 2);
		testH.setClassLabel("E");
		train.add(testH);
		Observation testI = new Observation();
		testI.putFeature(1, 3);
		testI.putFeature(2, 7);
		testI.putFeature(3, 1);
		testI.setClassLabel("A");
		train.add(testI);
		Observation testJ = new Observation();
		testJ.putFeature(1, 2);
		testJ.putFeature(2, 12);
		testJ.putFeature(4, 9);
		testJ.setClassLabel("E");
		train.add(testJ);
		Observation testK = new Observation();
		testK.putFeature(1, 1);
		testK.putFeature(3, 1);
		testK.putFeature(4, 5);
		testK.setClassLabel("E");
		train.add(testK);

		kNearestNeighbours.classify(testA, train, 1, "euclidian", true);
		assert (testA.getClassLabel() == "A");
		kNearestNeighbours.classify(testA, train, 2, "euclidian", true);
		assert (testA.getClassLabel() == "A");
		kNearestNeighbours.classify(testA, train, 3, "euclidian", true);
		assert (testA.getClassLabel() == "B");
		kNearestNeighbours.classify(testA, train, 4, "euclidian", true);
		assert (testA.getClassLabel() == "B");

	}
}
