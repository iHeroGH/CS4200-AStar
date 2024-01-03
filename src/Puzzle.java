package src;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.lang.reflect.Method;

public class Puzzle {

    // Puzzles should end up here
    static final int[][] GOAL_MATRIX = {
        {0, 1, 2},
        {3, 4, 5},
        {6, 7, 8}
    };

    private List<List<Integer>> matrix;
    private int zeroX;
    private int zeroY;

    private int g;
    private int hToUse;

    private Puzzle parent;

    public Puzzle(List<List<Integer>> matrix){
        // The matrix was given to us in the intended format
        initializeMatrix(matrix);
    }

    public Puzzle(Integer[][] matrix){
        // The matrix was given as arrays, not lists
        // No matter, we will simply translate it
        List<List<Integer>> translated = new ArrayList<>();

        for(Integer[] subMatrix : matrix){
            translated.add(Arrays.asList(subMatrix));
        }

        initializeMatrix(translated);
    }

    public Puzzle(String matrixIdentifier){
        // The matrix was given as a flattened string
        // We will simply read the string and build the ArrayList
        List<List<Integer>> translated = new ArrayList<>();
        List<Integer> sublist = new ArrayList<>();

        // Every 3 characters, we push the sublist to the translated matrix
        for(int i = 0; i < matrixIdentifier.length(); i++){
            if(sublist.size() == 3){
                translated.add(sublist);
                sublist = new ArrayList<>();
            }
            sublist.add(
                Integer.parseInt(
                    String.valueOf(matrixIdentifier.charAt(i))
                )
            );
        }

        // If we remain with more information, push that too
        if(sublist.size() != 0){
            translated.add(sublist);
        }

        initializeMatrix(translated);
    }

    public void initializeMatrix(List<List<Integer>> matrix){
        // A one-stop-hub for all our matrix needs

        // Ensure that matrix is valid
        if (!checkValidity(matrix))
            throw new IllegalArgumentException("Bad Matrix.");

        // Store some helpful information, useful for the A* algorithm
        this.matrix = matrix;
        this.parent = null;
        this.g = 0;
        this.hToUse = 0;

        // Find the position of the empty tile. Will make moving it around much
        // easier
        for(int y = 0; y < 3; y++){
            for(int x = 0; x < 3; x++){
                if (matrix.get(y).get(x) == 0){
                    this.zeroX = x;
                    this.zeroY = y;
                    break;
                }
            }
        }
    }

    public int numMisplaced(){
        // The first heuristic. Simply count the number of misplaced tiles
        // according to the goal matrix defined earlier
        int numMisplaced = 0;

        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                // We ignore the empty tile
                if (this.matrix.get(i).get(j) == 0){
                    continue;
                }

                // A discrepency
                if(GOAL_MATRIX[i][j] != this.matrix.get(i).get(j)){
                    numMisplaced++;
                }
            }
        }

        return numMisplaced;
    }

    public int manhattanDistance(){
        // The second heuristic. Counts the total distance of each tile to its
        // intended position
        // Quite time-consuming, but will find a final solution quicker (we hope)
        int manhattanDistance = 0;

        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                // Again, we ignore the empty tile
                if (this.matrix.get(i).get(j) == 0){
                    continue;
                }

                // The distance of this single tile to its intended position
                manhattanDistance += singleDistance(i, j);
            }
        }

        return manhattanDistance;
    }

    public int singleDistance(int i, int j){
        int trueX = -1;
        int trueY = -1;
        int intended = this.matrix.get(i).get(j);

        // Find out where it's supposed to be
        // Can be sped up if we maintain a cache of every tile to its intended
        // position. But this works too.
        for(int y = 0; y < 3; y++){
            for(int x = 0; x < 3; x++){
                if(GOAL_MATRIX[y][x] == intended){
                    trueX = x;
                    trueY = y;
                    break;
                }
            }
        }

        // Should have a true position by now
        if(trueX == -1 | trueY == -1){
            throw new RuntimeException(
                "Something went wrong with distance."
            );
        }

        // Find the total difference
        int diffX = Math.abs(trueX - j);
        int diffY = Math.abs(trueY - i);

        return diffX + diffY;
    }

    public boolean isGoal(){
        // If every tile is in the right spot
        return numMisplaced() == 0;
    }

    // Simple getters, setters, and checks follow
    public int getG(){
        return this.g;
    }

    public void setG(int g){
        this.g = g;
    }

    public boolean hasParent(){
        return parent != null;
    }

    public Puzzle getParent(){
        return this.parent;
    }

    public void setParent(Puzzle parent){
        this.parent = parent;
    }

    public int getHToUse(){
        return this.hToUse;
    }

    public void setHToUse(int hToUse){
        this.hToUse = hToUse;
    }

    public void setZeroX(int zeroX){
        this.zeroX = zeroX;
    }

    public void setZeroY(int zeroY){
        this.zeroY = zeroY;
    }

    public int f(){
        // f(n) = g(n) + h(n)
        // g(n) should already be set
        // h(n) is calculated depending on the requested heuristic

        // If something went wrong requesting a heuristic
        if(hToUse != 1 && hToUse != 2){
            throw new RuntimeException("Illegal hToUse");
        }

        int h = (hToUse == 1) ? numMisplaced() : manhattanDistance();
        return g + h;
    }

    public Puzzle moveLeft(){
        // Moves the empty tile to the left
        return swap(zeroX, zeroY, zeroX - 1, zeroY);
    }

    public Puzzle moveRight(){
        // Moves the empty tile to the right
        return swap(zeroX, zeroY, zeroX + 1, zeroY);
    }

    public Puzzle moveUp(){
        // Moves the empty tile up
        return swap(zeroX, zeroY, zeroX, zeroY - 1);
    }

    public Puzzle moveDown(){
        // Moves the empty tile down
        return swap(zeroX, zeroY, zeroX, zeroY + 1);
    }

    public List<Method> possibleMoves(){
        // Retrieve a list of the possible moves we can make as of current
        // These possible moves are represented by the literal methods that
        // are used to invoke the respective move (moveLeft, moveRight, ...)
        List<Method> possibleMoves = new ArrayList<>();

        Class<?> _class = this.getClass();
        try{
            if(zeroX > 0)
                possibleMoves.add(_class.getDeclaredMethod("moveLeft"));
            if(zeroX < 2)
                possibleMoves.add(_class.getDeclaredMethod("moveRight"));
            if(zeroY > 0)
                possibleMoves.add(_class.getDeclaredMethod("moveUp"));
            if(zeroY < 2)
                possibleMoves.add(_class.getDeclaredMethod("moveDown"));
        } catch (Exception e) {
            // If the requested move method somehow doesn't exist.
            // We can ensure that they all *do* exist, however, because we can
            // scroll one method up.
            e.printStackTrace();
        }

        return possibleMoves;
    }

    private Puzzle swap(int x1, int y1, int x2, int y2){

        // Out of bounds
        if(
            x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0 ||
            x1 > 3 || x2 > 3 || y1 > 3 || y2 > 3
        ){
            throw new IllegalArgumentException("Bad swap values.");
        }

        // Create a copy of the matrix (so that we don't modify the existing
        // puzzle)
        List<List<Integer>> matrixCopy = copyMatrix();

        // Retrieve the originals
        int first = this.matrix.get(y1).get(x1);
        int second = this.matrix.get(y2).get(x2);

        // Swap them in the copy
        matrixCopy.get(y1).set(x1, second);
        matrixCopy.get(y2).set(x2, first);

        // Create a Puzzle from this copy
        Puzzle swapped = new Puzzle(matrixCopy);
        swapped.setHToUse(this.hToUse);

        return swapped;
    }

    private List<List<Integer>> copyMatrix(){
        // Simply copies a matrix (a nested List)
        List<List<Integer>> matrixCopy = new ArrayList<>();
        for(List<Integer> sublist : this.matrix){
            matrixCopy.add(new ArrayList<>(sublist));
        }

        return matrixCopy;
    }

    public static boolean checkValidity(List<List<Integer>> matrix){
        // Ensures a matrix's dimensions are valid and that it's solvable

        // Length Check
        if (matrix.size() != 3){
            return false;
        }

        // All values are 0-8, not repeating
        Set<Integer> allowedValues = new HashSet<>(
            Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8})
        );
        boolean found;
        for(int i = 0; i < 3; i++){
            // Length Check
            if (matrix.get(i).size() != 3){
                return false;
            }

            // Must use all of 0-8 once
            for(int j = 0; j < 3; j++){
                found = allowedValues.remove(matrix.get(i).get(j));

                if(!found){
                    return false;
                }
            }
        }

        // Unused values
        if (allowedValues.size() != 0){
            return false;
        }

        // Inversion Check
        if(countInversions(matrix) % 2 != 0){
            return false;
        }

        return true;
    }

    public static int countInversions(List<List<Integer>> matrix){
        // Count how many times a value is greater than all the values that
        // proceed it. Solvable Puzzles will have an even number of inversions.

        int inversions = 0;

        int prev;
        int curr;
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                prev = matrix.get(i).get(j);

                // Skip the empty tile
                if (prev == 0){
                    continue;
                }

                for(int k = 0; k < 3; k++){
                    for(int l = 0; l < 3; l++){

                        // Skip before self
                        // (previous levels, or previous values on same level)
                        if (k < i || (l < j && k == i)){
                            continue;
                        }

                        curr = matrix.get(k).get(l);

                        // Skip the empty tile
                        if (curr == 0){
                            continue;
                        }

                        // Inversion found
                        if(prev > curr){
                            inversions++;
                        }
                    }
                }
            }
        }

        return inversions;
    }

    @Override
    public String toString(){
        // Print the matrix in a nicely formatted fashion
        String output = "";
        for(List<Integer> sublist : this.matrix){
            output += sublist.toString() + "\n";
        }
        return output;
    }

    @Override
    public boolean equals(Object otherPuzzle){
        // Ensures equality between two puzzles
        // (where one of them might not even be a Puzzle)
        return (
            otherPuzzle.getClass() == this.getClass() &&
            this.equals((Puzzle) otherPuzzle)
        );
    }

    @Override
    public int hashCode(){
        // Implements a hashing algorithm to allow for use in HashSets
        return Arrays.deepHashCode(matrix.toArray());
    }

    public boolean equals(Puzzle otherPuzzle){
        // Ensures equality between two puzzles' matrices
        return sameMatrix(this.matrix, otherPuzzle.matrix);
    }

    public boolean sameMatrix(
        List<List<Integer>> first,
        List<List<Integer>> second
    ){
        // Ensures equality between two matrices
        for(int i = 0; i < first.size(); i++){
            for(int j = 0; j < second.size(); j++){
                // Terminate as soon as we find a discrepency
                if (first.get(i).get(j) != second.get(i).get(j)){
                    return false;
                }
            }
        }

        // Otherwise, we're safe
        return true;
    }
}