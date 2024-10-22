import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/** Main class for Berkeley optimal building paths program.
 * @author Brian Lin
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
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
                        } else {
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
                    case "help":
                        if (args.length == 1) {
                            helpHandler();
                        } else {
                            System.out.println("Invalid command format.");
                        }
                        break;
                    case "graphic":
                        if (args.length == 2) {
                            graphicHandler(args);
                            graphData.writeGraphManager();
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
    }

    /** Sets up persistence with directory to save graphs to on disk */
    public static void setupPersistence() throws IOException {
        if (!GRAPHDIR.exists()) {
            GRAPHDIR.mkdir();
            graphData = new graphManager(graphDataPath);
            graphData.readDistances(csvPath);
            graphData.readCoordinates(csvCoordinates);
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
            System.out.println(graphData.graphic);
            Scanner scan = new Scanner(System.in);
            List<String> buildingList = new ArrayList<>();
            System.out.println("Enter building names to add to path or STOP to finishing inputting:");
            while(true) {
                String input = scan.nextLine();
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
                String input = scan.nextLine();
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
                    graphData.csvRows, graphData.csvBuildingIndices, graphData.csvCoordinates, graphData.graphic);
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
            System.out.println("Current information for this building set:\n" + updateGraph.toString());
            while(updateInputExit) {
                System.out.println(
                        "Enter 1 to add buildings, 2 to remove buildings, or 3 to continue.");
                String updateInput = scan.nextLine();
                switch (updateInput) {
                    case "1":
                        System.out.println("Enter building names to add to path or STOP to finishing inputting:");
                        while (true) {
                            String input = scan.nextLine();
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
                            String input = scan.nextLine();
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
                System.out.println("Updated information for this building set:\n" + updateGraph.toString());
                System.out.println("Enter the building name to start at:");
                String input = scan.nextLine();
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
            updateGraph.updateBuildingGraph(graphData.csvRows, graphData.csvBuildingIndices,
                    graphData.csvCoordinates, graphData.graphic);
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

    /** Handler for help command in main.
     *
     */
    public static void helpHandler() {
        StringBuilder helpText = new StringBuilder("Commands: \n");
        helpText.append("add [name]: adds a new set of buildings to calculate optimal path for.\n");
        helpText.append("update [name]: changes contents of an existing set of buildings to calculate optimal path for.\n");
        helpText.append("remove [name]: removes an existing set of buildings.\n");
        helpText.append("list: displays all existing sets of buildings to calculate optimal path for.\n");
        helpText.append("graphic [on/off]: toggles from graphical to text representation of paths with \"on\" for graphics and \"off\" for text.\n");
        helpText.append("\nIncluded buildings: \n");
        int displayIndex = 1;
        String firstBuilding = "";
        for (String building : graphData.csvBuildingIndices.keySet()) {
            if (displayIndex % 2 == 0) {
                helpText.append(String.format("%-55.55s  %-55.55s%n", building, firstBuilding));
            } else {
                firstBuilding = building;
            }
            displayIndex += 1;
        }
        System.out.println(helpText);
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

    /** Handler for the graphics command in main.
     *
     * @param args arguments given to program from main method
     */
    public static void graphicHandler(String[] args) {
        if (args[1].equals("on")) {
            graphData.setGraphic(true);
        } else if (args[1].equals("off")) {
            graphData.setGraphic(false);
        } else {
            System.out.println("Invalid graphics status.");
        }
    }

    /** Path to csv file with raw distance values */
    public static String csvPath = "buildingDistances.csv";

    /** Path to csv file with raw coordinate values */
    public static String csvCoordinates = "buildingCoordinates.csv";

    /** File object for CWD */
    public static final File CWD = new File(".");

    /** File object for directory storing building graphs created by user */
    public static final File GRAPHDIR = Paths.get(CWD.getPath(), ".graphs").toFile();

    /** Default name to save graphManager instance under */
    public static final String graphDataPath = "graphData";

    /** graphManager instance containing overall graph data */
    public static graphManager graphData;
}
