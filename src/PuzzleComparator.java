package src;
import java.util.Comparator;

public class PuzzleComparator implements Comparator<Puzzle> {
    // We use a comparator so that it can be passed into the PriorityQueue for
    // easy initialization

    @Override
    public int compare(Puzzle p1, Puzzle p2) {
        // Simply compares the f(n) value for both Puzzles
        return Integer.compare(p1.f(), p2.f());
    }

}
