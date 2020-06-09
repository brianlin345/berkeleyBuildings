import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import javax.swing.JPanel;

/** Class outputting a graphical representation of the optimal path for a given building set.
 * @author Brian Lin
 */
public class buildingGraphic extends JPanel{

    /** Constructor for the graphical output representing the minimal path for a given building set
     * Sets up the range for the viewing window so that coordinates will be displayed proportionately
     *
     * @param outer radius of each marker in the graphic representing a building
     * @param centers mapping of building names to their coordinates
     * @param buildingIndices mapping of path indices to building names
     * @param distances ordered collection of distances between each node on the optimal path
     * @param indices ordered collection of each building traversed on optimal path by index
     */
    public buildingGraphic(int outer, HashMap<String, double[]> centers, HashMap<Integer, String> buildingIndices,
                           double[] distances, int[] indices) {
        this.outer = outer;
        this.centers = centers;
        this.buildingIndices = buildingIndices;
        this.distances = distances;
        this.pathIndices = indices;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        for (Integer buildingIndex : buildingIndices.keySet()) {
            double[] coordinate = centers.get(buildingIndices.get(buildingIndex));
            if (coordinate[0] < minLon) {
                minLon = coordinate[0];
            }
            if (coordinate[0] > maxLon) {
                maxLon = coordinate[0];
            }
            if (coordinate[1] < minLat) {
                minLat = coordinate[1];
            }
            if (coordinate[1] > maxLat) {
                maxLat = coordinate[1];
            }
        }
        lonOffset = (maxLon-minLon) * 0.15;
        latOffset = (maxLat-minLat) * 0.15;
        lonStart = minLon - lonOffset;
        lonEnd = maxLon + lonOffset;
        latStart = minLat - latOffset;
        latEnd = maxLat + latOffset;
    }

    /** Paints the graphical representation for a optimal path with labels for buildings and distance between nodes
     * with nodes positioned in the window relative to their coordinate positions.
     * @param g Graphics object that draws the path representation
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g1 = (Graphics2D) g.create();
        g1.setStroke(new BasicStroke(2));
        g1.setColor(Color.decode("#003262"));
        for (int pathIndex = 0; pathIndex < distances.length-1; pathIndex++) {
            g1.setColor(Color.decode("#003262"));
            int[] center = centerPixels[pathIndex];
            int[] nextCenter = centerPixels[pathIndex + 1];
            g1.drawLine(center[0], center[1], nextCenter[0], nextCenter[1]);
            Graphics2D g2 = (Graphics2D) g.create();
            String distanceString = String.format("%.2f meters", distances[pathIndex + 1]);
            drawArrowHead(g2, center[0], nextCenter[0], center[1], nextCenter[1], distanceString);
        }
        for (int pointIndex = 0; pointIndex < distances.length; pointIndex++) {
            int[] center = centerPixels[pointIndex];
            g1.setColor(Color.decode("#FDB515"));
            g1.fillOval(center[0] - outer / 2, center[1] - outer / 2, outer, outer);
            g1.setColor(Color.BLACK);
            g1.drawString(buildingIndices.get(pathIndices[pointIndex]),
                    (int)(center[0] - (1.75 * outer)), center[1] - (int) (outer / 1.25));
        }
    }

    /** Draws an arrowhead along a given path between nodes for stylistic representation of a directed path
     *
     * @param g2 Graphics object from paint method
     * @param x1 starting x value of line
     * @param x2 ending x value of line
     * @param y1 starting y value of line
     * @param y2 ending y value of line
     * @param distance String value representing the distance along this line
     */
    private void drawArrowHead(Graphics2D g2, int x1, int x2, int y1, int y2, String distance) {
        double angle = Math.atan2(y2-y1, x2-x1);
        AffineTransform tx = g2.getTransform();
        tx.translate((int)((x2+x1)/2), (int)((y2+y1)/2));
        tx.rotate(angle - Math.PI / 2d);
        g2.setTransform(tx);

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 5);
        arrowHead.addPoint(-5, -5);
        arrowHead.addPoint(5, -5);
        g2.fill(arrowHead);
        AffineTransform tr = g2.getTransform();
        if ((angle > Math.PI / 2 && angle < Math.PI) || (angle < -Math.PI / 2 && angle > -Math.PI)) {
            tr.rotate(Math.toRadians(-90));
        } else {
            tr.rotate(Math.toRadians(90));
        }
        g2.setTransform(tr);
        Font currFont = g2.getFont();
        Font newFont = currFont.deriveFont(currFont.getSize() * 0.8F);
        g2.setFont(newFont);
        g2.drawString(distance, -25, -5);
    }

    /** Converts latitude/longitude coordinates for buildings to proportional pixel values for display in the graphics window
     *
     */
    public void convertCoords() {
        int[][] centersPixel = new int[distances.length][2];
        for (int index = 0; index < centersPixel.length; index++) {
            centersPixel[index][0] = (int)(((centers.get(buildingIndices.get(pathIndices[index]))[0] - lonStart) / (lonEnd-lonStart)) * windowWidth);
            centersPixel[index][1] = windowHeight - (int)
                    (((centers.get(buildingIndices.get(pathIndices[index]))[1] - latStart) / (latEnd-latStart)) * windowHeight);
        }
        this.centerPixels = centersPixel;
    }


    /** Radius for each node marker in pixels
     *
     */
    private int outer;

    /** Mapping between building names and their lat/lon pairs from coordinates file
     *
     */
    private HashMap<String, double[]> centers;

    /** Mapping between building indices and building names from the building set for this graphic
     *
     */
    private HashMap<Integer, String> buildingIndices;

    /** Collection of distances between nodes along the optimal path for the building set for this graphic
     *
     */
    private double[] distances;

    /** Collection of nodes traversed along the optimal path for the building set for this graphic
     *
     */
    private int[] pathIndices;

    /** Collection of x/y pixel values for nodes used in graphics drawing, ordered by traversal along optimal path
     *
     */
    private int[][] centerPixels;

    /** Starting longitude value used to set edge of frame and node positions
     *
     */
    private double lonStart;

    /** Ending longitude value used to set edge of frame and node positions
     *
     */
    private double lonEnd;

    /** Starting latitude value used to set edge of frame and node positions
     *
     */
    private double latStart;

    /** Ending latitude value used to set edge of frame and node positions
     *
     */
    private double latEnd;

    /** Value in pixels for the height of the output graphical window
     *
     */
    public static int windowHeight = (int)((4.0/6.0) * 800);

    /** Value in pixels for the width of the output graphical window
     *
     */
    public static int windowWidth = 800;

    /** Offset value added to longitude coordinates so that nodes do not appear on the edges of the window
     *
     */
    private double lonOffset;

    /** Offset value added to latitude coordinates so that nodes do not appear on the edges of the window
     *
     */
    private double latOffset;
}
