# k-Nearest Neighbours classification in Java 
Implementation of a k-nearest neighbours algorithm run on sample data in MatrixMarket format. 
Neighbours can be weighted or unweighted and their distance can be measured using Euclidian 
or cosine distance. 

This classification is done in the method ```kNearestNeighbours.classify```, which takes four parameters:
* ```test```, a ```Vector``` object to be classified
* ```train```, an ArrayList of ```Vector``` objects of known class
* ```K```, the number of neighbours which will vote on the class label
* ```distanceMeasure```, which can be ```euclidian``` or ```cosine```
* ```weighted```, a boolean indicating whether the classification should be weighted (with closer 
neighbours having a larger vote). 

Note that the main method of ```kNearestNeighbours.classify``` conducts leave-one-out cross validation
for both an unweighted and a weighted classifier across 10 values of K, and thus takes several 
minutes to run. 

### Instructions
* From the root directory, compile with: ```javac -d . src/*```
* The main program can then be executed from the root directory using: ```java javaknn.kNearestNeighbours```

* Tests can be run from the root using: ```java -ea javaknn.Tests```

