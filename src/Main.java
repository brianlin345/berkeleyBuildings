import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        readDistances(csvPath);
        setupPersistence();
        try {
            switch (args[0]) {
                case "add":
                    if (args.length == 2) {
                        addHandler(args);
                    } else {
                        System.out.println("Invalid command format.");
                    }
                    break;
                case "update":
                    break;
                case "quit":
                    break;
                default:
                    System.out.println("Invalid input.");
                    break;
            }
        } catch (IOException excp) {
            excp.printStackTrace();
        }
    }

    /** Reads csv file containing table of distances into mapping of
     * building names to distance from all other buildings in file
     * @param fileName name of file to read raw distances from
     */
    public static void readDistances(String fileName){
        csvRows = new HashMap<>();
        csvBuildingIndices = new HashMap<>();
        File distanceFile = Paths.get(CWD.getPath(), fileName).toFile();
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
    public static boolean checkBuilding(String buildingName) {
        return csvRows.containsKey(buildingName);
    }

    /** Sets up persistence with directory to save graphs to on disk */
    public static void setupPersistence() {
        if (!GRAPHDIR.exists()) {
            GRAPHDIR.mkdir();
        }
    }

    /** Handler for add command in main
     *
     * @param args arguments given to program from main method
     * @throws IOException if graph saving encounters error
     */
    public static void addHandler(String[] args) throws IOException {
        String graphName = args[1];
        if (!buildingGraph.graphSet.containsKey(graphName)) {
            Scanner scan = new Scanner(System.in);
            List<String> buildingList = new ArrayList<>();
            System.out.println("Enter building names to add to path or STOP to finishing inputting.");
            while(true) {
                String input = scan.next();
                if (input.equals("STOP")) {
                    break;
                }
                if (checkBuilding(input) && !buildingList.contains(input)) {
                    buildingList.add(input);
                } else {
                    System.out.println("Invalid building name.");
                }
            }
            while(true) {
                System.out.println("Enter the building name to start at.");
                String input = scan.next();
                if (buildingList.contains(input)) {
                    if (!buildingList.get(0).equals(input)) {
                        buildingList.set(buildingList.indexOf(input), buildingList.get(0));
                        buildingList.set(0, input);
                    }
                    break;
                } else {
                    System.out.println("Invalid building name.");
                }
            }
            buildingGraph newGraph = new buildingGraph(buildingList, graphName, csvRows, csvBuildingIndices);
            newGraph.calcMinPath();
            buildingGraph.graphSet.put(graphName, newGraph.getGraphID());
        } else {
            System.out.println("Graph already exists");
        }
    }

    /** Path to csv file with raw distance values */
    public static String csvPath = "buildingDistance_sample.csv";

    /** Mapping of building names to distances from all other buildings
    * from distance matrix csv file */
    public static HashMap<String, double[]> csvRows = new HashMap<>();

    /** Mapping between building names and indices from raw distance file */
    public static HashMap<String, Integer> csvBuildingIndices = new HashMap<>();

    /** File object for CWD */
    public static final File CWD = new File(".");

    /** File object for directory storing building graphs created by user */
    public static final File GRAPHDIR = Paths.get(CWD.getPath(), ".graphs").toFile();

}
