package plugins.hero.knn;
import java.io.*;
import java.util.*;

public class FileHandler {
    // Reads a matrix of features and a vector of class labels and outputs an Observation ArrayList.
    public static ArrayList<Observation> read(String matrixPath, String labelPath){
        File matrixFile = new File(matrixPath);
        File labelFile = new File(labelPath);
        BufferedReader reader = null;
        Observation[] obsArr = null;
        
        // Reads the matrix file. 
        try {
            String text = null;

            // Initialises the BufferedReader and skips the first two lines of the file.
            reader = new BufferedReader(new FileReader(matrixFile));
            reader.readLine();
            reader.readLine();

            // Finds the largest observation number and sets it as the array size.
            int size = 0;
            while ((text = reader.readLine()) != null) {
                int obsNo = Integer.parseInt(text.substring(0, text.indexOf(" ")));
                if (obsNo > size)
                    size = obsNo;
            }
            obsArr = new Observation[size];

            // Reinitialises the BufferedReader and skips the first two lines of the file.
            reader = new BufferedReader(new FileReader(matrixFile));
            reader.readLine();
            reader.readLine();

            // Stores the feature values in the Observation object at each index. 
            while ((text = reader.readLine()) != null) {
                String[] split = text.split(" ");
                int obsNo = Integer.parseInt(split[0]) - 1;
                int feature = Integer.parseInt(split[1]);
                int value = Integer.parseInt(split[2]);

                if (obsArr[obsNo] == null) {
                    // Creates an Observation object at that index if there is not one already.
                    obsArr[obsNo] = new Observation();
                    // Inserts the key-value pair into the relevant Observation object.
                    obsArr[obsNo].putFeature(feature, value);
                }
                else {
                    // Inserts the key-value pair into the relevant Observation object.
                    obsArr[obsNo].putFeature(feature, value);
                }
            }
            
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        // Reads the class label file. 
        try {
            String text = null;
            reader = new BufferedReader(new FileReader(labelFile));

            while ((text = reader.readLine()) != null) {
                // Sets the class label of the Observation object corresponding to the number on that line.
                String[] split = text.split(",");
                obsArr[Integer.parseInt(split[0]) - 1].setClassLabel(split[1]);
            }

            
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        
        ArrayList<Observation> obsList = new ArrayList<Observation>(Arrays.asList(obsArr));
        
        return obsList;
    }
}
