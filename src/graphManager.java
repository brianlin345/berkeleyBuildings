import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;

public class graphManager implements Serializable {

    public graphManager(String saveName) {
        saveFile = Paths.get(Main.GRAPHDIR.getPath(), saveName).toFile();
    }

    public static graphManager readGraphManager(String saveName) {
        File graphManagerFile = Paths.get(Main.GRAPHDIR.getPath(), saveName).toFile();
        if (graphManagerFile.exists()) {
            return serializeUtils.readObject(graphManagerFile, graphManager.class);
        }
        return null;
    }

    public void writeGraphManager() throws IOException {
        serializeUtils.writeObject(saveFile, this);
    }

    /** Reads csv file containing table of distances into mapping of
     * building names to distance from all other buildings in file
     * @param fileName name of file to read raw distances from
     */
    public void readDistances(String fileName){
        csvRows = new HashMap<>();
        csvBuildingIndices = new HashMap<>();
        File distanceFile = Paths.get(Main.CWD.getPath(), fileName).toFile();
        try {
            BufferedReader br = new BufferedReader(new FileReader(distanceFile));
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
                csvBuildingIndices.put(buildingNames[buildingIndex], buildingIndex);
                buildingIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Checks if the given building name is present in the raw distance file.
     *
     * @param buildingName name of building to check for
     * @return if building name found in raw distances file
     */
    public boolean checkBuilding(String buildingName) {
        return csvRows.containsKey(buildingName);
    }

    /** Mapping of building names to distances from all other buildings
     * from distance matrix csv file */
    public HashMap<String, double[]> csvRows = new HashMap<>();

    /** Mapping between building names and indices from raw distance file */
    public HashMap<String, Integer> csvBuildingIndices = new HashMap<>();

    /** Mapping of building graph names to hash code for lookups */
    public HashMap<String, String> graphSet = new HashMap<>();

    public File saveFile;
}
