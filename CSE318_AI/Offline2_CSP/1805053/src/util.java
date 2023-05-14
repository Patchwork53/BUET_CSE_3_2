import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class util{
    public static HashSet<Integer> initialDomain(int[][] board, int row, int col ){
        HashSet<Integer> domain = new HashSet<>();

        for(int k=1; k<=board.length;k++){
            domain.add(k);
        }

        for(int i=0;i<board.length;i++){
            domain.remove(board[row][i]);
            domain.remove(board[i][col]);
        }
        return domain;
    }
    public static boolean consistencyCheck(HashMap<Variable, Integer> assignment, Variable to_be_assigned, int value, CSP problem){

        ArrayList<Variable> to_check = new ArrayList<>();
        ArrayList<Integer> dummy = new ArrayList<>();

        int row = to_be_assigned.i;
        int col = to_be_assigned.j;

        for(int i=0; i<problem.n;i++){
            to_check.add(new Variable(dummy, row, i));
            to_check.add(new Variable(dummy, i, col));
        }


        for(var assigned: to_check){

            if (problem.board[assigned.i][assigned.j]==value)
                return false;

            Integer e = assignment.get(assigned);
            if (e == null)
                continue;

            if (e == value)
                return false;


        }

        return true;
    }

    public static int[][] deepCopy2D(int[][] b){
        int[][] b2 = new int[b.length][b.length];
        for (int i=0; i<b.length;i++){
            for (int j=0; j<b.length;j++){
                b2[i][j] = b[i][j];
            }
        }
        return b2;
    }

    public static boolean deepCompare2D(int[][] b, int[][] b2){
        for (int i=0; i<b.length;i++){
            for (int j=0; j<b.length;j++){
                if (b2[i][j] != b[i][j])
                    return false;
            }
        }
        return true;
    }

    public static void printBoard(int[][] b){
        for (int[] ints : b) {
            for (int j = 0; j < b.length; j++) {
                System.out.printf("%2d  ", ints[j]);
            }
            System.out.println();
        }
    }

    public static void printSolution(int[][] initial_board, HashMap<Variable, Integer> assignments){
        int n = initial_board.length;
        int [][] board = new int[n][n];

        for(int i=0;i<n;i++) {
            for (int j = 0; j < n; j++) {
                board[i][j]=initial_board[i][j];
            }
        }

        for (var key: assignments.keySet()){
            board[key.i][key.j]=assignments.get(key);
        }

        for (int[] ints : board) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%2d  ", ints[j]);
            }
            System.out.println();
        }



    }

}
