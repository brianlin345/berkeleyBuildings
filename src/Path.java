import java.util.HashSet;
import java.util.Objects;

/** Class for an intermediate traversed path in a building set, used during optimal path calculation
 * @author Brian Lin
 */
public class Path {

    /** Constructor for a path with its head node and traversed nodes
     *
     * @param headNode current node of path
     * @param t nodes traversed to reach current node
     */
    public Path(int headNode, HashSet<Integer> t) {
        this.headNode = headNode;
        this.traversed = t;
    }

    /** Overridden equals method for hashing comparison */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        boolean headNodeEquals = headNode == path.headNode;
        boolean traversedEquals = (traversed == null && path.traversed == null) ||
                (traversed != null && traversed.equals(path.traversed));
        return headNodeEquals && traversedEquals;
    }

    /** Overridden hash method for hashing comparison */
    @Override
    public int hashCode() {
        return Objects.hash(headNode, traversed);
    }

    /** Current node for this path */
    public int headNode;

    /** Collection of nodes traversed from start to reach head node */
    public HashSet<Integer> traversed;
}
