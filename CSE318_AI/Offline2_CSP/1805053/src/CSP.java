import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;



public class CSP{
    public int[][] board;
    public HashSet<Variable> unassigned;
    public int n, unassigned_count;
    public HashMap<Variable, Integer> cache;

    public CSP(int[][] board_) {
        this.n = board_.length;
        this.board = new int[n][n];

        for(int i=0;i<n;i++) {
            for (int j = 0; j < n; j++) {
                this.board[i][j]=board_[i][j];
            }
        }
        unassigned = new HashSet<>();

        unassigned_count = 0;


        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++) {
                if (board[i][j]==0) {
                    ArrayList<Integer> domain = new ArrayList<>(util.initialDomain(board, i, j));
                    unassigned.add(new Variable(domain, i, j));
                    unassigned_count ++;
                }
            }
        }

//        cache = buildMaxDegreeCache();
//        System.out.println("NUMBER OF EMPTY SPOTS: "+unassigned_count);

//        for(var v: unassigned){
//            System.out.println(v+" domain: "+v.domain);
//        }
    }



    public boolArrList reduceDomains(Variable v, int value){
        //true means domain turned to zero
        ArrayList<Variable> victims = new ArrayList<>();
        boolean domain_0 = false;

        for(var o: unassigned){
            if (o.i==v.i || o.j == v.j)
                if(o.domain.remove((Integer) value)) {
                    victims.add(o);
                    if (o.domain.size()==0) {
                        return  new boolArrList(victims, true);
                    }
                }
        }


        return new boolArrList(victims, false);
    }

    public void reimburseDomains(ArrayList<Variable> victims, Integer value){
        for (var o: victims){
            o.domain.add(value);
        }
    }


    public HashMap<Variable, Integer> buildMaxDegreeCache(){
        HashMap<Variable, Integer> cache = new HashMap<>();
        for (var v: unassigned){
            cache.put(v, (int) MRVByMaxDegree.getDegree(v, this));
        }
        return cache;
    }

    private boolean checkNeighbors(int i, int j){
        for(int row=0;row<n;row++){
            if (row == i)
                continue;
            if (board[row][j]== board[i][j])
                return false;

        }
        for(int col=0;col<n;col++){
            if (col == j)
                continue;
            if (board[i][col]== board[i][j])
                return false;
        }
        return true;
    }

    public boolean verifySolution(){
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                if (!checkNeighbors(i, j)) {
                    System.out.println(i+" "+j);
                    return false;
                }
            }
        }
        return true;
    }






}
