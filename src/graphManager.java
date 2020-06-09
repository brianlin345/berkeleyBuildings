import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;

/** Class containing overall information and mapping for all building sets created by the user.
 * @author Brian Lin
 */
public class graphManager implements Serializable {

    /** Constructor for graphManager that sets up save file
     *
     * @param saveName default name to save graphManager instance to
     */
    public graphManager(String saveName) {
        saveFile = Paths.get(Main.GRAPHDIR.getPath(), saveName).toFile();
    }

    /** Reads a graphManager from disk under the given file name
     *
     * @param saveName name of file to read from .graphs directory
     * @return deserialized graphManager object
     */
    public static graphManager readGraphManager(String saveName) {
        File graphManagerFile = Paths.get(Main.GRAPHDIR.getPath(), saveName).toFile();
        if (graphManagerFile.exists()) {
            return serializeUtils.readObject(graphManagerFile, graphManager.class);
        }
        return null;
    }

    /** Writes this graphManager to disk
     *
     * @throws IOException if error occurs when writing to disk
     */
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

    /** Reads csv file containing table of lat/lon coordinates into mapping of
     * building names to pairs of lat/lon values
     * @param fileName name of file to read raw coordinates from
     */
    public void readCoordinates(String fileName) {
        csvCoordinates = new HashMap<>();
        File distanceFile = Paths.get(Main.CWD.getPath(), fileName).toFile();
        try {
            BufferedReader br = new BufferedReader(new FileReader(distanceFile));
            String row = "";
            while ((row = br.readLine()) != null) {
                String[] rowCoordinates = row.split(",");
                double[] coordinatePair = new double[]{Double.parseDouble(rowCoordinates[1]), Double.parseDouble(rowCoordinates[2])};
                csvCoordinates.put(rowCoordinates[0], coordinatePair);
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

    /** Check if the given buildingGraph has been created.
     *
     * @param graphName name of graph to check for
     * @return if graph under given name exists
     */
    public boolean checkGraph(String graphName) {
        return graphSet.containsKey(graphName);
    }

    /** Adds a new graph with mapping to its hashcode used for serialization
     *
     * @param graphName name of graph to add
     * @param graphID hashcode of new graph
     */
    public void addGraph(String graphName, String graphID) {
        graphSet.put(graphName, graphID);
    }

    /** Removes the graph under given name from mappings and deletes file on disk.
     * Assumes a graph exists under the given name
     *
     * @param graphName name of graph to remove
     */
    public void removeGraph(String graphName) {
        File graphFile = Paths.get(Main.GRAPHDIR.getPath(), graphSet.get(graphName)).toFile();
        graphFile.delete();
        graphSet.remove(graphName);
    }

    /** Returns hashcode for the graph with given name, assuming there exists a graph with that name
     *
     * @param graphName name of graph to get hashcode for
     * @return hashcode for graph under given name
     */
    public String getGraph(String graphName) {
        return graphSet.get(graphName);
    }

    /** Returns the number of buildingGraphs tracked by this graphManager.
     *
     * @return number of tracked building graphs
     */
    public int numGraphs() {
        return graphSet.size();
    }

    /** Displays every graph tracked by this graphManager using overridden toString method
     */
    public void displayGraphs() {
        for (String graphName : graphSet.keySet()) {
            buildingGraph curr = buildingGraph.readGraph(graphSet.get(graphName));
            System.out.println(curr);
            System.out.println();
        }
    }

    public void setGraphic(boolean status) {
        this.graphic = status;
    }

    /** Mapping of building names to distances from all other buildings
     * from distance matrix csv file */
    public HashMap<String, double[]> csvRows = new HashMap<>();

    /** Mapping of building names to their latitude/longitude coordinates
     * from coordinate csv file */
    public HashMap<String, double[]> csvCoordinates = new HashMap<>();

    /** Mapping between building names and indices from raw distance file */
    public HashMap<String, Integer> csvBuildingIndices = new HashMap<>();

    /** Mapping of building graph names to hash code for lookups */
    private HashMap<String, String> graphSet = new HashMap<>();

    /** File object to save graph data to on disk */
    private File saveFile;

    /** Indicates whether graphical or text representation of optimal paths will be generated for all building sets.
     * True: graphical output, False: text output
     */
    public boolean graphic = true;
}
