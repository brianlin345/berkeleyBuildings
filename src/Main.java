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
                    if (args.length == 2) {
                        updateHandler(args);
                    } else {
                        System.out.println("Invalid command format.");
                    }
                    break;
                case "remove":
                    if (args.length == 2) {
                        removeHandler(args);
                        graphData.writeGraphManager();
                    } else{
                        System.out.println("Invalid command format.");
                    }
                    break;
                case "list":
                    if (args.length == 1) {
                        listHandler();
                    } else {
                        System.out.println("Invalid command format.");
                    }
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
        if (!graphData.checkGraph(graphName)) {
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
            graphData.addGraph(graphName, newGraph.getGraphID());
        } else {
            System.out.println("A graph with this name already exists.");
        }
    }

    /** Handler for update command in main with cycles for adding and removing buildings.
     *
     * @param args arguments given to program from main method
     * @throws IOException if graph saving encounters error
     */
    public static void updateHandler(String[] args) throws IOException {
        String graphName = args[1];
        if (graphData.checkGraph(graphName)) {
            Scanner scan = new Scanner(System.in);
            buildingGraph updateGraph = buildingGraph.readGraph(graphData.getGraph(graphName));
            boolean updateInputExit = true;
            System.out.println(String.format("Now updating %s", graphName));
            while(updateInputExit) {
                System.out.println(
                        "Enter 1 to add buildings, 2 to remove buildings, or 3 to continue.");
                String updateInput = scan.next();
                switch (updateInput) {
                    case "1":
                        System.out.println("Enter building names to add to path or STOP to finishing inputting:");
                        while (true) {
                            String input = scan.next();
                            if (input.equals("STOP")) {
                                break;
                            }
                            if (graphData.checkBuilding(input) && !updateGraph.checkBuilding(input)) {
                                updateGraph.addBuilding(input);
                            } else {
                                System.out.println("Invalid building name.");
                            }
                        }
                        break;
                    case "2":
                        System.out.println("Enter building names to remove from path or STOP to finishing inputting:");
                        while (updateGraph.numBuildings() > 0) {
                            String input = scan.next();
                            if (input.equals("STOP")) {
                                break;
                            }
                            if (graphData.checkBuilding(input) && updateGraph.checkBuilding(input)) {
                                updateGraph.removeBuilding(input);
                            } else {
                                System.out.println("Invalid building name.");
                            }
                        }
                        break;
                    case "3":
                        updateInputExit = false;
                        break;
                    default:
                        System.out.println("Invalid input.");
                        break;
                }
            }
            while(true) {
                System.out.println("Enter the building name to start at:");
                String input = scan.next();
                if (updateGraph.checkBuilding(input)) {
                    if (!updateGraph.getBuilding(0).equals(input)) {
                        updateGraph.setBuilding(updateGraph.getBuilding(0),
                                updateGraph.getBuildingIndex(input));
                        updateGraph.setBuilding(input, 0);
                    }
                    break;
                } else {
                    System.out.println("Invalid building name.");
                }
            }
            updateGraph.updateBuildingGraph(graphData.csvRows, graphData.csvBuildingIndices);
            updateGraph.displayGraph();
            updateGraph.calcMinPath();
        } else {
            System.out.println("No graph with this name found.");
        }
    }

    /** Handler for remove command in main.
     *
     * @param args arguments given to program from main method
     */
    public static void removeHandler(String[] args) {
        String graphName = args[1];
        if (graphData.checkGraph(graphName)) {
            graphData.removeGraph(graphName);
            System.out.println(String.format("Building set %s removed successfully.", graphName));
        } else {
            System.out.println("No graph with this name found.");
        }
    }

    /** Handler for list command in main.
     */
    public static void listHandler() {
        if (graphData.numGraphs() > 0) {
            graphData.displayGraphs();
        } else {
            System.out.println("No building sets have been created yet.");
        }
    }

    /** Path to csv file with raw distance values */
    public static String csvPath = "buildingDistance_sample.csv";

    /** File object for CWD */
    public static final File CWD = new File(".");

    /** File object for directory storing building graphs created by user */
    public static final File GRAPHDIR = Paths.get(CWD.getPath(), ".graphs").toFile();

    /** Default name to save graphManager instance under */
    public static final String graphDataPath = "graphData";

    /** graphManager instance containing overall graph data */
    public static graphManager graphData;
}
