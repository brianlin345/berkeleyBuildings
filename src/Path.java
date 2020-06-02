import java.util.HashSet;
import java.util.Objects;

public class Path {

    public Path(int headNode, HashSet<Integer> t) {
        this.headNode = headNode;
        this.traversed = t;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(headNode, traversed);
    }

    /** Current node for this path */
    public int headNode;

    /** Collection of nodes traversed from start to reach head node */
    public HashSet<Integer> traversed;
}
