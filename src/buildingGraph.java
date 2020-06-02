import java.util.*;

public class buildingGraph {

    /** Constructor that creates mapping of buildings to graph indices and sets up
     * adjacency matrix
     * @param buildingNames names of building subset to create graph with
     */
    public buildingGraph(String[] buildingNames) {
        this.buildingDistances = new double[buildingNames.length][buildingNames.length];
        for (int i = 0; i < buildingNames.length; i++) {
            buildings.put(buildingNames[i], i);
            buildingIndices.put(i, buildingNames[i]);
        }
        constructGraph();
    }

    /** Wrapper method for calculating and displaying minimum path */
    public void calcMinPath() {
        minPath();
        displayPath();
    }

    /** Constructs graph based on building names given in constructor */
    private void constructGraph() {
        for (String buildingRow : buildings.keySet()) {
            double[] currRow = Main.csvRows.get(buildingRow);
            for (String buildingCol : buildings.keySet()) {
                buildingDistances[buildings.get(buildingRow)][buildings.get(buildingCol)] =
                        currRow[buildings.get(buildingCol)];
            }
        }
    }

    /** Displays the constructed adjacency matrix for graph in table format */
    public void displayGraph() {
        for (String buildingName : buildings.keySet()) {
            System.out.print(buildingName + " ");
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
        pathDistances = new HashMap<>();
        Path minPath = null;
        double minDist = Double.MAX_VALUE;
        for (Path longPath : resultsDP.keySet()) {
            if (longPath.traversed.size() == buildings.size()-2 && resultsDP.get(longPath) < minDist) {
                minPath = longPath;
                minDist = resultsDP.get(longPath);
            }
        }
        Integer pathNode = minPath.headNode;
        HashSet<Integer> pathTraversed = new HashSet<Integer>(minPath.traversed);
        while (true) {
            Integer prevNode = currNodeParents.get(new Path(pathNode, pathTraversed));
            pathTraversed.remove(prevNode);
            if (prevNode == null) {
                break;
            }
            pathDistances.put(pathNode, buildingDistances[pathNode][prevNode]);
            pathNode = prevNode;
        }
        pathDistances.put(0, 0.0);
    }

    /** Displays optimal path with building names and distance between */
    public void displayPath() {
        if (pathDistances != null) {
            for (Map.Entry<Integer, Double> node : pathDistances.entrySet()) {
                if (node.getValue() > 0) {
                    System.out.println(node.getValue());
                }
                System.out.println(buildingIndices.get(node.getKey()));
            }
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

    /** Mapping representing minimum path with keys as nodes and value as the distance from
     * given node to previous/adjacent node along path
     */
    HashMap<Integer, Double> pathDistances;

    /** Mapping of path objects (head node and collection of nodes along path from start)
     * to distance for intermediate computations */
    HashMap<Path, Double> resultsDP;

    /** Mapping of path objects (head node and collection of nodes along path from start)
     * to the node traversed before reaching head node
     */
    HashMap<Path, Integer> currNodeParents;

    /** Collection of possible combinations of traversal from empty path to last node -
     * path combinations do not include last node
     */
    private List<HashSet<Integer>> combinations = new ArrayList<>();

    /** Adjacency matrix used to represent complete weighted graph */
    public double[][] buildingDistances;

    /** Mapping of building names to indices in adjacency matrix */
    private HashMap<String, Integer> buildings = new HashMap<>();

    /** Mapping of indices to building names for display purposes */
    private HashMap<Integer, String> buildingIndices = new HashMap<>();

}
