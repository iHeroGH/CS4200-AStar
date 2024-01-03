package src;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;

public class AStarSolver {

    Puzzle input;
    int hToUse;
    Puzzle solution;

    int h1Cost;
    double h1Duration;
    int h2Cost;
    double h2Duration;

    int numSteps;

    public AStarSolver(Puzzle input, int requestedH){
        this.input = input;
        this.hToUse = requestedH;

        this.solution = null;

        // Run both heuristics, only displaying the path of the chosen, but
        // the statistics of both
        aStarCompare();

        // Print some statistics
        System.out.println("Num Steps: " + numSteps);
        System.out.println("H1 Search Cost: " + h1Cost);
        System.out.println("H1 Duration: " + h1Duration + "ms");
        System.out.println("H2 Search Cost: " + h2Cost);
        System.out.println("H2 Duration: " + h2Duration + "ms");
        System.out.println();
    }

    public void aStarCompare(){
        // Execute AStar with the chosen heuristic
        long start = System.nanoTime();
        int cost = aStar(hToUse);
        setCost(hToUse, cost);
        setTime(hToUse, start);
        printPath();

        // Execute it again with the *other* heuristic 1->2, 2->1
        hToUse ^= 3;
        start = System.nanoTime();
        cost = aStar(hToUse);
        setCost(hToUse, cost);
        setTime(hToUse, start);
    }

    public void printPath(){
        List<Puzzle> path = getPath();
        int numSteps = 1;
        Puzzle currStep;
        // The path is technically reversed, so we read it from right to left
        for(int i = path.size() - 1; i >= 0; i--){
            System.out.println("Step: " + numSteps++);
            currStep = path.get(i);

            // int g = currStep.getG();
            // int f = currStep.f();
            // int h = f - g;
            // System.out.println("g = " + g + " h = " + h + " f = " + f);
            System.out.println(currStep);
        }
    }

    public int aStar(int hToUse){
        // f(n) = g(n) + h(n)
        // g(n) = true cost so far
        // h(n) = estimated cost to solution
        input.setHToUse(hToUse);

        // The search tree as a PriorityQueue that compares the values of f(n)
        Queue<Puzzle> frontier = new PriorityQueue<>(
            new PuzzleComparator()
        );
        frontier.add(input);

        // Maintain a cache of previously explored puzzles to prevent re-adding
        Set<Puzzle> explored = new HashSet<>();

        Puzzle currPuzzle;
        Puzzle childPuzzle;
        int searchCost = 0;
        while(!frontier.isEmpty()){
            currPuzzle = frontier.poll();
            explored.add(currPuzzle);

            // Goal Found
            if (currPuzzle.isGoal()){
                this.solution = currPuzzle;
                break;
            }

            // Come forth, my children
            for(Method possibleMethod : currPuzzle.possibleMoves()){
                try{
                    // Create a child of every possible move that can be made
                    childPuzzle = (Puzzle) possibleMethod.invoke(currPuzzle);

                    // If this child has already been explored, why explore it again?
                    if (explored.contains(childPuzzle)){
                        continue;
                    }

                    childPuzzle.setHToUse(currPuzzle.getHToUse());
                    childPuzzle.setG(currPuzzle.getG() + 1); // Depth
                    childPuzzle.setParent(currPuzzle);

                    // Add a new child to the frontier, therby increasing the
                    // cost of this run
                    frontier.add(childPuzzle);
                    searchCost++;
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        }

        return searchCost;
    }

    public List<Puzzle> getPath(){

        // Ensure a solution has already been found (aStar was called)
        if(solution == null){
            throw new IllegalArgumentException("No solution found.");
        }

        // As a LinkedList does, so will a LinkedList do
        List<Puzzle> path = new ArrayList<>();
        Puzzle currPuzzle = this.solution;
        while(currPuzzle.hasParent()){
            // Adding to the path in this way technically means that the path
            // is reversed
            // No matter; it is easy to traverse a List backwards
            path.add(currPuzzle);
            currPuzzle = currPuzzle.getParent();
        }
        this.numSteps = path.size();

        return path;
    }

    public void setCost(int hToUse, int cost){
        // A glorified setter method.
        // Just chooses a variable to fill with the cost
        if(hToUse == 1){
            this.h1Cost = cost;
        }
        if(hToUse == 2){
            this.h2Cost = cost;
        }
    }

    public void setTime(int hToUse, long startTime){
        // A glorified setter method.
        // Just chooses a variable to fill with the duration
        if(hToUse == 1){
            this.h1Duration = (System.nanoTime() - startTime) / 1_000_000.0;
        }
        if(hToUse == 2){
            this.h2Duration = (System.nanoTime() - startTime) / 1_000_000.0;
        }
    }
}
