import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/** Class representing a set of buildings and their optimal path as a connected graph
 * @author Brian Lin
 */
public class buildingGraph implements Serializable {

    /** Constructor that creates mapping of buildings to graph indices and sets up
     * adjacency matrix
     * @param buildingNames names of building subset to create graph with
     * @param name name of this graph for serialization
     * @param fileDistances mapping of distances to each building from file
     * @param fileIndices mapping of building names to indices in file rows
     */
    public buildingGraph(List<String> buildingNames, String name, HashMap<String, double[]> fileDistances,
                         HashMap<String, Integer> fileIndices, HashMap<String, double[]> fileCoords) throws IOException {
        this.distances = fileDistances;
        this.indices = fileIndices;
        this.graphName = name;
        this.coordinates = fileCoords;
        for (int i = 0; i < buildingNames.size(); i++) {
            buildings.put(buildingNames.get(i), i);
            buildingIndices.put(i, buildingNames.get(i));
        }
        constructGraph();
        getGraphID();
        writeGraph();
    }

    /** Updates a deserialized building graph by reconstructing the graph and optimal path
     *
     * @param fileDistances mapping of building names to rows to use from distance file
     * @param fileIndices mapping of building names to indices from distance file
     * @throws IOException if writing the graph to disk encounters an error
     */
    public void updateBuildingGraph(HashMap<String, double[]> fileDistances,
                               HashMap<String, Integer> fileIndices, HashMap<String, double[]> fileCoords) throws IOException {
        this.distances = fileDistances;
        this.indices = fileIndices;
        this.coordinates = fileCoords;
        constructGraph();
        writeGraph();
    }

    /** Reads a building graph from disk under the given id
     *
     * @param graphID hashcode id for the building graph to be read into memory
     * @return deserialized building graph
     */
    public static buildingGraph readGraph(String graphID) {
        File graphFile = Paths.get(Main.GRAPHDIR.getPath(), graphID).toFile();
        if (graphFile.exists()) {
            return serializeUtils.readObject(graphFile, buildingGraph.class);
        }
        return null;
    }

    /** Writes this building graph to disk under filename given by the unique hash code
     * for this building graph
     * @throws IOException if file writing or hashing encounters an error
     */
    public void writeGraph() throws IOException {
        File graphFile = Paths.get(Main.GRAPHDIR.getPath(), getGraphID()).toFile();
        serializeUtils.writeObject(graphFile, this);
    }

    /** Getter method for hashcode of this building graph that generates the hashcode from
     * serializing this object if hashcode not generated before
     * @return id for this building graph from hash function
     * @throws IOException if hashing encounters an error
     */
    public String getGraphID() throws IOException {
        if (this.graphID == null) {
            this.graphID = serializeUtils.getHash((Object) serializeUtils.serialize(this));
        }
        return this.graphID;
    }

    /** Checks if a given building name exists in this building graph
     *
     * @param buildingName name of building to check for
     * @return if given building is present in this building graph
     */
    public boolean checkBuilding(String buildingName) {
        return buildings.containsKey(buildingName);
    }

    /** Getter method for a given building by index assuming that it exists in this building graph
     *
     * @param buildingIndex index of building to return
     * @return name of building corresponding to the given building node index
     */
    public String getBuilding(int buildingIndex) {
        return buildingIndices.get(buildingIndex);
    }

    /** Getter method for a given building index by name assuming that it exists in this building graph
     *
     * @param buildingName name of building to return
     * @return index of building node corresponding to the given building name
     */
    public int getBuildingIndex(String buildingName) {
        return buildings.get(buildingName);
    }

    /** Adds a new building to this building graph by setting the building name
     * to the end of the collection of mappings
     *
     * @param buildingName name of building to add
     */
    public void addBuilding(String buildingName) {
        setBuilding(buildingName, buildings.size());
    }

    /** Sets a building to the given index for node traversal
     *
     * @param buildingName name of building to set
     * @param buildingIndex name of index to map given building to
     */
    public void setBuilding(String buildingName, int buildingIndex) {
        buildings.put(buildingName, buildingIndex);
        buildingIndices.put(buildingIndex, buildingName);
    }

    /** Removes the building under the given name from this building graph.
     * Assumes the given building exists in this building graph.
     * @param buildingName name of building to remove
     */
    public void removeBuilding(String buildingName) {
        if (buildings.get(buildingName) == buildings.size() - 1) {
            buildingIndices.remove(buildings.get(buildingName));
        } else {
            setBuilding(buildingIndices.get(buildings.size() - 1), buildings.get(buildingName));
            buildingIndices.remove(buildings.size()-1);
        }
        buildings.remove(buildingName);
    }

    /** Gets the number of buildings in this buildingGraph
     *
     * @return number of buildings
     */
    public int numBuildings() {
        return buildings.size();
    }

    /** Wrapper method for calculating and displaying minimum path */
    public void calcMinPath() {
        minPath();
        displayPath();
    }

    /** Constructs graph based on building names given in constructor */
    public void constructGraph() {
        this.buildingDistances = new double[buildings.size()][buildings.size()];
        for (String buildingRow : buildings.keySet()) {
            double[] currRow = distances.get(buildingRow);
            for (String buildingCol : buildings.keySet()) {
                buildingDistances[buildings.get(buildingRow)][buildings.get(buildingCol)] =
                        currRow[indices.get(buildingCol)];
            }
        }
    }

    /** Displays the constructed adjacency matrix for graph in table format */
    public void displayGraph() {
        for (int buildingIndex : buildingIndices.keySet()) {
            System.out.print(buildingIndices.get(buildingIndex) + " ");
        }
        System.out.println();
        for (int i = 0; i < buildingDistances.length; i++) {
            for (int j = 0; j < buildingDistances[0].length; j++) {
                System.out.print(buildingDistances[i][j] + " ");
            }
            System.out.println();
        }
    }

    /** Method to calculate minimum path with all enumerated combinations and lookup of
     * previously calculated smaller results using DP approach
     */
    private void minPath() {
        int[] vertices = new int[buildings.size()-1];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = i + 1;
        }
        getCombinations(vertices);
        combinations.sort(comboLenComparator);

        resultsDP = new HashMap<>();
        currNodeParents = new HashMap<>();

        for (HashSet<Integer> path : combinations) {
            for (int currNode = 1; currNode < buildings.size(); currNode++) {
                if (path.contains(currNode)) {
                    continue;
                }

                Path currPath = new Path(currNode, path);
                double minWeight = Double.MAX_VALUE;
                int parentNode = 0;

                if (path.size() == 0 ) {
                    minWeight = buildingDistances[0][currNode];
                }

                HashSet<Integer> copyPath = new HashSet<Integer>(path);
                for (int prevNode : path) {
                    double dist = buildingDistances[currNode][prevNode] + getCalcDistanceDP(prevNode, copyPath);
                    if (dist < minWeight) {
                        minWeight = dist;
                        parentNode = prevNode;
                    }
                }
                resultsDP.put(currPath, minWeight);
                currNodeParents.put(currPath, parentNode);

            }
        }

        getPath();
    }

    /** Generates optimal path based on parents mapping and intermediate results created during
     * DP calculation
     */
    private void getPath() {
        pathNodes = new int[buildingDistances.length];
        pathDistances = new double[buildingDistances.length];
        Path minPath = null;
        double minDist = Double.MAX_VALUE;
        for (Path longPath : resultsDP.keySet()) {
            if (longPath.traversed.size() == buildings.size()-2 && resultsDP.get(longPath) < minDist) {
                minPath = longPath;
                minDist = resultsDP.get(longPath);
            }
        }
        this.pathLen = minDist;
        int pathNode = minPath.headNode;
        HashSet<Integer> pathTraversed = new HashSet<Integer>(minPath.traversed);
        int nodeIndex = buildingDistances.length - 1;
        while (true) {
            Integer prevNode = currNodeParents.get(new Path(pathNode, pathTraversed));
            pathTraversed.remove(prevNode);
            if (prevNode == null) {
                break;
            }
            pathNodes[nodeIndex] = pathNode;
            pathDistances[nodeIndex] = buildingDistances[pathNode][prevNode];
            nodeIndex -= 1;
            pathNode = prevNode;
        }
        pathNodes[0] = 0;
        pathDistances[0] = 0.0;
    }

    /** Displays optimal path with building names and distance between.
     * Displays a graphical representation of the path using buildingGraphic if set in Main, otherwise outputs a text representation */
    public void displayPath() {
        if (Main.graphic) {
            buildingGraphic panel = new buildingGraphic(20, coordinates, buildingIndices, pathDistances, pathNodes);
            panel.convertCoords();
            panel.setBackground(Color.LIGHT_GRAY);
            JFrame frame = new JFrame(String.format("Optimal path for building set %s, starting from %s | Total path distance: %.2f meters",
                    this.graphName, buildingIndices.get(0), this.pathLen));
            frame.setSize(buildingGraphic.windowWidth, buildingGraphic.windowHeight);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.setVisible(true);
        } else {
            System.out.println();
            System.out.println(String.format("Optimal path for building set %s, starting from %s:",
                    this.graphName, buildingIndices.get(0)));
            if (pathDistances != null) {
                for (int nodeIndex = 0; nodeIndex < buildingDistances.length; nodeIndex++) {
                    if (pathDistances[nodeIndex] > 0) {
                        System.out.println("   |");
                        System.out.println(String.format("%.2f", pathDistances[nodeIndex]));
                        System.out.println("   |");
                    }
                    System.out.println(buildingIndices.get(pathNodes[nodeIndex]));
                }
            }
            System.out.println(String.format("Total path distance: %.2f meters", this.pathLen));
        }
    }

    /** Retrieves previous DP calculation by accessing appropriate entry in hashmap
     * using overridden equals and hashing
     * @param node head node of calculated result
     * @param path path containing previous head node - will remove head node to find
     *             previous result connecting head node to rest of path
     * @return previously calculated result of optimal distance between head node and rest of path
     */
    private double getCalcDistanceDP(int node, HashSet<Integer> path) {
        path.remove(node);
        Path lookupPath = new Path(node, path);
        double prevDistance = resultsDP.get(lookupPath);
        path.add(node);
        return prevDistance;
    }

    /** Wrapper method to enumerate all possible paths excluding start node
     *
     * @param input array of node indices to enumerate combinations
     */
    public void getCombinations(int[] input) {
        int[] data = new int[input.length];
        combinations = new ArrayList<>();
        genCombinations(input, 0, 0, data);
    }

    /** Generates combinations recursively up to length of input to enumerate all possible paths
     *
     * @param input input array containing node indices
     * @param start starting index to add values from input onto current combination
     * @param index current index to add values to combination
     * @param combos stores intermediate combinations
     */
    private void genCombinations(int[] input, int start, int index, int[] combos) {
        if (index == input.length) {
            return;
        }
        addCombo(combos, index);
        for (int i = start; i < input.length; i++) {
            combos[index] = input[i];
            genCombinations(input, i+1, index+1, combos);
        }
    }

    /** Adds a new combination to overall enumeration of combinations
     *
     * @param combos array of intermediate combinations containing combination to be added
     * @param index position to stop adding values ensuring new combination is added
     */
    private void addCombo(int[] combos, int index) {
        HashSet<Integer> newCombo = new HashSet<>();
        for(int i = 0; i < index; i++) {
            newCombo.add(combos[i]);
        }
        combinations.add(newCombo);
    }

    /** Comparator used on combinations set to order from smallest to largest length so that
     * bottom to top DP approach can be used
     */
    public static Comparator<HashSet<Integer>> comboLenComparator = new Comparator<HashSet<Integer>>() {
        @Override
        public int compare(HashSet<Integer> set1, HashSet<Integer> set2) {
            return set1.size() - set2.size();
        }
    };

    /** toString method displaying building set name and included buildings
     *
     * @return formatted string containing name and included buildings
     */
    @Override
    public String toString() {
        StringBuilder buildingGraphString = new StringBuilder(String.format("Name: %s \nBuildings: ", graphName));
        int buildingIndex = 0;
        for (String building : buildings.keySet()) {
            buildingGraphString.append(building);
            if (buildingIndex < buildings.size() - 1) {
                buildingGraphString.append(", ");
            }
            buildingIndex += 1;
        }
        return buildingGraphString.toString();
    }

    /** Ordered collection of nodes traversed from start on optimal path
     */
    private transient int[] pathNodes;

    /** Ordered collection of distance between each node along optimal path
     */
    private transient double[] pathDistances;

    /** Length of minimum path */
    private transient double pathLen;

    /** Mapping of path objects (head node and collection of nodes along path from start)
     * to distance for intermediate computations */
    private transient HashMap<Path, Double> resultsDP;

    /** Mapping of path objects (head node and collection of nodes along path from start)
     * to the node traversed before reaching head node
     */
    private transient HashMap<Path, Integer> currNodeParents;

    /** Collection of possible combinations of traversal from empty path to last node
     */
    private transient List<HashSet<Integer>> combinations;

    /** Adjacency matrix used to represent complete weighted graph */
    public double[][] buildingDistances;

    /** Mapping of building names to indices in adjacency matrix */
    private HashMap<String, Integer> buildings = new HashMap<>();

    /** Mapping of indices to building names for display purposes */
    private HashMap<Integer, String> buildingIndices = new HashMap<>();

    /** Name for this set of buildings given by user */
    private String graphName;

    /** Hashcode given to this building graph using MD5 hashing function */
    private String graphID;

    /** Mapping of building names to distances from file read in main */
    private transient HashMap<String, double[]> distances;

    /** Mapping of building names to indices from file read in main */
    private transient HashMap<String, Integer> indices;

    /** Mapping of building names to coordinate pairs from file read in main */
    private transient HashMap<String, double[]> coordinates;
}
