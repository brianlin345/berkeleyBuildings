import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        try {
            setupPersistence();
            switch (args[0]) {
                case "add":
                    if (args.length == 2) {
                        addHandler(args);
                        graphData.writeGraphManager();
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

    /** Sets up persistence with directory to save graphs to on disk */
    public static void setupPersistence() throws IOException {
        if (!GRAPHDIR.exists()) {
            GRAPHDIR.mkdir();
            graphData = new graphManager(graphDataPath);
            graphData.readDistances(csvPath);
            graphData.writeGraphManager();
        } else {
            graphData = graphManager.readGraphManager(graphDataPath);
        }
    }

    /** Handler for add command in main
     *
     * @param args arguments given to program from main method
     * @throws IOException if graph saving encounters error
     */
    public static void addHandler(String[] args) throws IOException {
        String graphName = args[1];
        if (!graphData.graphSet.containsKey(graphName)) {
            Scanner scan = new Scanner(System.in);
            List<String> buildingList = new ArrayList<>();
            System.out.println("Enter building names to add to path or STOP to finishing inputting:");
            while(true) {
                String input = scan.next();
                if (input.equals("STOP")) {
                    break;
                }
                if (graphData.checkBuilding(input) && !buildingList.contains(input)) {
                    buildingList.add(input);
                } else {
                    System.out.println("Invalid building name.");
                }
            }
            while(true) {
                System.out.println("Enter the building name to start at:");
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
            buildingGraph newGraph = new buildingGraph(buildingList, graphName,
                    graphData.csvRows, graphData.csvBuildingIndices);
            newGraph.calcMinPath();
            graphData.graphSet.put(graphName, newGraph.getGraphID());
        } else {
            System.out.println("A graph with this name already exists.");
        }
    }

    /** Path to csv file with raw distance values */
    public static String csvPath = "buildingDistance_sample.csv";

    /** File object for CWD */
    public static final File CWD = new File(".");

    /** File object for directory storing building graphs created by user */
    public static final File GRAPHDIR = Paths.get(CWD.getPath(), ".graphs").toFile();

    public static final String graphDataPath = "graphData";

    public static graphManager graphData;
}
