
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class boolArrList{
    public ArrayList<Variable> arr;
    public boolean aBoolean;
    boolArrList(ArrayList<Variable> arr, boolean aBoolean){
        this.arr = arr;
        this.aBoolean = aBoolean;
    }
}

class Variable{
    ArrayList<Integer> domain;
    int i;
    int j;

    public Variable(ArrayList<Integer> domain, int i, int j) {
        this.domain = new ArrayList<>(domain);
        this.i = i;
        this.j = j;
    }

    @Override
    public String toString() {
        return "("+i+", "+j+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return i == variable.i && j == variable.j;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, j);
    }
}



public class Main {

    public static int[][] readProblem(String path) throws FileNotFoundException {
        File myObj = new File(path);
        Scanner myReader = new Scanner(myObj);
        int n = Integer.parseInt(myReader.next().split("=")[1].split(";")[0]);

        int[][] board = new int[n][n];

        myReader.next();
        myReader.next();

        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++) {
                String temp = myReader.next();
                if (Objects.equals(temp, "|")){
                    j--;
                    continue;
                }
                try {
                    board[i][j] = Integer.parseInt(temp.split(",")[0]);
                }
                catch (Exception e){
                    System.out.print("FORMATTING ISSUE at: ");
                    System.out.println(i+" "+j);
                    System.out.println(temp);
                }
            }
        }
        return board;
    }
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {


        Backtrack backtrack = new Backtrack();

        Heuristic MRV = new MRV();
        Heuristic maxDegree = new MAXDegree();
        Heuristic tieBreak = new MRVMaxDegreeTieBreaks();
        Heuristic mrvByDegree = new MRVByMaxDegree();
        Heuristic random = new RandomSelection();

        Heuristic[] heuristics = new Heuristic[]{MRV, tieBreak, mrvByDegree, random};






        long start, end, elapsed;
        String[] paths = new String[]{"data/d-10-01.txt","data/d-10-06.txt", "data/d-10-07.txt","data/d-10-08.txt", "data/d-10-09.txt","data/d-15-01.txt"};
//        paths = new String[]{"data/d-15-01.txt"};

        for(var path: paths){
            System.out.println("--------------------------------------");
            System.out.println(path);
            System.out.println("----Time--------Nodes-----Backtrack---");


            System.out.println("SIMPLE:");
            for (var heu: heuristics){
                System.out.println(heu.getName());
                var board = readProblem(path);
                start = System.nanoTime();
                backtrack.backtrackStart(new CSP(board), heu, false);
                end = System.nanoTime();
                elapsed = (end - start);
                System.out.printf("%10f %10d %10d\n", elapsed/10e6, backtrack.nodes,  backtrack.backtracks);
            }

            System.out.println("FC:");
            for (var heu: heuristics){
                System.out.println(heu.getName());
                var board = readProblem(path);
                start = System.nanoTime();
                var problem = new CSP(board);
                backtrack.backtrackStart(problem, heu, true);
                end = System.nanoTime();
                elapsed = (end - start);
                System.out.printf("%10f %10d %10d\n", elapsed/10e6, backtrack.nodes,  backtrack.backtracks);
            }



        }

    }
}
