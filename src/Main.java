import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
	    readDistances(csvPath);
	    String[] buildings = {"Dwinelle", "VLSB", "Doe", "Evans"};
	    buildingGraph bg = new buildingGraph(buildings);
	    bg.calcMinPath();
    }

    /** Reads csv file containing table of distances into mapping of
     * building names to distance from all other buildings in file
     * @param fileName name of file to read raw distances from
     */
    public static void readDistances(String fileName){
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String[] buildingNames = br.readLine().split(",");
            int buildingIndex = 0;
            String row = "";
            while ((row = br.readLine()) != null) {
                String[] rowDistances = row.split(",");
                double[] rowDistancesNum = new double[rowDistances.length];
                for (int i = 0; i < rowDistancesNum.length; i++) {
                    rowDistancesNum[i] = Double.parseDouble(rowDistances[i]);
                }
                csvRows.put(buildingNames[buildingIndex], rowDistancesNum);
                buildingIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String csvPath = "buildingDistance_sample.csv";

    /** Mapping of building names to distances from all other buildings
    * from distance matrix csv file */
    static HashMap<String, double[]> csvRows = new HashMap<>();
}
