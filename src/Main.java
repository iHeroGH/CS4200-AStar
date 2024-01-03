package src;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main{

    public static void main(String[] args){

        // Welcome.
        System.out.println("CS4200 Project 1");

        // Why create a scanner for every function when we can just make one here?
        Scanner scan = new Scanner(System.in);

        int runType = askForRunType(scan);
        switch (runType){
            case 1:
                // Uses a random or manual input for the puzzle
                singleRun(scan);
                break;
            case 2:
                // Uses a file for input
                Map<Integer, List<AStarSolver>> infoTable = multiRun(scan);
                dealWithInfo(infoTable);
                break;
            default:
                // How does one even get here...
                throw new IllegalArgumentException("Impossible run type.");
        }

        scan.close();
    }

    public static int askForRunType(Scanner scan){

        // Present the question
        System.out.println("Select Run Type:");
        System.out.println("[1] Single (random or manual input)");
        System.out.println("[2] Multi (requires filepath)");

        // Retrieve the answer
        String chosenRunType = scan.nextLine();
        switch(chosenRunType){
            case "1":
                return 1;
            case "2":
                return 2;
            default:
                // Continuously ask until they listen
                System.out.println("Please choose one of the options.");
                return askForRunType(scan);
        }
    }

    public static Map<Integer, List<AStarSolver>> multiRun(Scanner scan){
        // Retrieve the file to read
        Scanner fileScanner = askForFile(scan);

        // Need to store our statistics somewhere...
        Map<Integer, List<AStarSolver>> infoTable = new HashMap<>();

        try{
            String curr = "";
            String puzzleString = "";
            String hToUseString = "";
            int hToUse;
            Puzzle inputPuzzle;
            AStarSolver solver;
            while (fileScanner.hasNextLine()){
                curr = fileScanner.nextLine();

                // Comment
                if (curr.startsWith("//")){
                    continue;
                }

                // Empty line
                if (curr.length() == 0){
                    continue;
                }

                // Illegal line
                if (curr.length() != 11){
                    System.out.println("Illegal line " + curr + ". Skipping.");
                    continue;
                }

                // Parse the given string. If it is not in the correct format,
                // it will later be skipped
                puzzleString = curr.substring(0, 9);
                hToUseString = curr.substring(10, 11);

                try{
                    // Hence, the nested try-catch
                    inputPuzzle = new Puzzle(puzzleString);
                    hToUse = Integer.parseInt(hToUseString);

                    if(hToUse != 1 && hToUse != 2){
                        throw new RuntimeException("Illegal hToUse");
                    }

                    // Solve and store
                    System.out.println("Puzzle:\n" + inputPuzzle);
                    solver = new AStarSolver(inputPuzzle, hToUse);

                    infoTable.putIfAbsent(solver.numSteps, new ArrayList<>());
                    infoTable.get(solver.numSteps).add(solver);

                } catch (Exception e){
                    System.out.println("Illegal line " + curr + ". Skipping.");
                }

            }

            fileScanner.close();
            return infoTable;
        } catch (Exception e){
            System.out.println("Something went wrong reading this file.");
        }

        fileScanner.close();
        return null;
    }

    public static Scanner askForFile(Scanner scan){
        // Present the question
        System.out.println(
            "Files should contain lines in the following format: " +
            "(the numbers 0-8 in any order) (1 or 2)."
        );
        System.out.println(
            "Empty lines and lines starting with // will be ignored."
        );
        System.out.println(
            "Enter a filepath: "
        );

        // Retrieve the answer (a Scanner for the requested file)
        String filepath = scan.nextLine();
        try{
            File file = new File(filepath);
            return new Scanner(file);
        } catch (FileNotFoundException e){
            System.out.println(
                "The provided input file (" + filepath + ") was not found."
            );
            return askForFile(scan);
        }
    }

    public static void singleRun(Scanner scan){
        // Simply gets the input and solves the problem. As single as it can get.

        String inputString = askForPuzzle(scan);

        Puzzle puzzle = new Puzzle(inputString);
        System.out.println("Puzzle:\n" + puzzle);

        int hToUse = askForH(scan);

        new AStarSolver(puzzle, hToUse);
    }

    public static String askForPuzzle(Scanner scan){
        // Present the question
        System.out.println("Select Input Method:");
        System.out.println("[1] Random");
        System.out.println("[2] Input");

        // Retrieve the answer
        String chosenInputMethod = scan.nextLine();
        switch(chosenInputMethod){
            case "1":
                return randomPuzzle();
            case "2":
                return inputPuzzle(scan);
            default:
                // Continuously ask until they listen
                System.out.println("Please choose one of the options.");
                return askForPuzzle(scan);
        }
    }

    public static int askForH(Scanner scan){
        // Present the question
        System.out.println("Select H Function:");
        System.out.println("[1] H1 (number of misplaced tiles)");
        System.out.println("[2] H2 (manhattan distance)");

        // Retrieve the answer
        String chosenH = scan.nextLine();
        switch(chosenH){
            case "1":
                return 1;
            case "2":
                return 2;
            default:
                // Continuously ask until they listen
                System.out.println("Please choose one of the options");
                return askForH(scan);
        }
    }

    public static String randomPuzzle(){
        List<Character> characters = Arrays.asList(
            new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8'}
        );

        try{
            // Shuffle it
            // A permutation
            Collections.shuffle(characters);
            String generated = "";
            for(Character curr : characters){
                generated += Character.toString(curr);
            }
            new Puzzle(generated);

            return generated;
        } catch (Exception e){
            // We created an unsolvable puzzle.
            // Retry till we stumble upon a valid puzzle
            return randomPuzzle();
        }

    }

    public static String inputPuzzle(Scanner scan){
        // Present the question
        System.out.println(
            "Enter the 9 numbers consequtively, " +
            "including every number from 0-8 only once. (EX: 012345678)"
        );

        // Retrieve the answer
        String inputted = scan.nextLine();
        try{
            new Puzzle(inputted);
            return inputted;
        } catch (Exception e){
            System.out.println(
                "Follow the directions for custom input." +
                " Your input must be solvable."
            );
            return inputPuzzle(scan);
        }

    }

    public static void dealWithInfo(Map<Integer, List<AStarSolver>> infoTable){

        // Theoretically, this should never be the case. Best make sure, though.
        if (infoTable == null){
            throw new IllegalArgumentException(
                "Something went wrong with data retrieval."
            );
        }

        // We will store only the averages found
        Map<Integer, double[]> condensedInfo = new HashMap<>();

        // Running totals
        double h1Cost = 0;
        double h1Duration = 0;
        double h2Cost = 0;
        double h2Duration = 0;
        double numTests = 0;
        double[] extractedInfo;
        for(Integer key : infoTable.keySet()){
            // Sum up all the solver's statistics
            for(AStarSolver solver : infoTable.get(key)){
                h1Cost += solver.h1Cost;
                h1Duration += solver.h1Duration;
                h2Cost += solver.h2Cost;
                h2Duration += solver.h2Duration;
                // System.out.println(key + " " + h2Duration);
                numTests += 1;
            }

            // Find the average
            h1Cost /= numTests;
            h1Duration /= numTests;
            h2Cost /= numTests;
            h2Duration /= numTests;

            // Store it
            extractedInfo = new double[]{
                h1Cost, h1Duration, h2Cost, h2Duration, numTests
            };

            condensedInfo.put(key, extractedInfo);

            // Reset it
            h1Cost = 0;
            h1Duration = 0;
            h2Cost = 0;
            h2Duration = 0;
            numTests = 0;
        }

        showCondensedInfo(condensedInfo);
    }

    public static void showCondensedInfo(Map<Integer, double[]> condensedInfo){

        double[] currentInfo;
        for(Integer key : condensedInfo.keySet()){
            currentInfo = condensedInfo.get(key);

            // Simply print the info array's data
            System.out.println();
            System.out.print("Depth: " + key + ", ");
            System.out.print("numTests: " + currentInfo[4] + ", ");
            System.out.print("h1Cost: " + currentInfo[0] + ", ");
            System.out.print("h1Duration: " + currentInfo[1] + ", ");
            System.out.print("h2Cost: " + currentInfo[2] + ", ");
            System.out.print("h2Duration: " + currentInfo[3] + ", ");
            System.out.println();

        }
    }

}