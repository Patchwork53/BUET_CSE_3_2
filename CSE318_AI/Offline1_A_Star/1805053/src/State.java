import static java.lang.Math.abs;


public class State {
    public int[] board;
    public int num_moves;
    public int n;
    public State parent;
    public boolean manhatten;
    public int blank_index;
    public int BLANK;

    public State(int n, int[] board, boolean manhatten) {

        this.n = n;
        this.board = board.clone();
        this.num_moves = 0;
        this.parent = null;
        this.manhatten = manhatten;
        this.BLANK = board.length;

        for (int i=0;i<board.length;i++){
            if (board[i]==BLANK) {
                this.blank_index = i;
                break;
            }
        }

    }

    public State(int[] board, State parent){
        this.board = board.clone();
        this.parent = parent;
        this.num_moves = parent.num_moves+1;
        this.manhatten = parent.manhatten;
        this.BLANK = parent.BLANK;
        this.n = parent.n;

        for (int i=0;i<board.length;i++){
            if (board[i]==BLANK) {
                this.blank_index = i;
                break;
            }
        }
    }

    int getPriority(){
        if (this.manhatten){
            return this.num_moves + this.get_sum_of_manhatten_distance();
        }
        else{
            return this.num_moves + this.get_hamming_distance();
        }

    }

    int get_hamming_distance(){
        int hamming_distance = 0;

        for(int i=0;i< board.length;i++){
            if (board[i]==BLANK)
                continue;

            if(board[i]!=i+1)
                hamming_distance++;
        }

        return hamming_distance;

    }
    int get_sum_of_manhatten_distance(){

        int sum = 0;
        for(int i=0;i< board.length;i++){
            if (board[i]==BLANK)
                continue;
            sum += get_manhatten_distance(i, board[i]-1);
        }
        return sum;
    }

    int get_manhatten_distance(int index1, int index2){
        // int index1_row = index1/n;
        // int index1_col = index1%n;
        return abs(index1/n-index2/n)+abs(index1%n-index2%n);
    }

    boolean is_goal(){
        for(int i=0;i< board.length;i++){
            if(board[i]!=i+1)
                return false;
        }
        return true;
    }

    State move(DIRECTION d){

        int adjustment = 0;
        switch (d)
        {
            case UP:
                if(blank_index<n)
                    // cout<<"Invalid Move move_up()";
                    adjustment = 0;
                else
                    adjustment = -n;
                break;

            case DOWN:
                if(blank_index>=n*n-n)
                    // cout<<"Invalid Move move_down() "<<blank_index<<" "<<n;
                    adjustment = 0;
                else
                    adjustment = n;
                break;

            case LEFT:
                if(blank_index%n==0)
                    // cout<<"Invalid Move move_left()";
                    adjustment = 0;
                else
                    adjustment = -1;
                break;

            case RIGHT:
                if((blank_index+1)%n==0)
                    // cout<<"Invalid Move move_right()";
                    adjustment = 0;
                else
                    adjustment=1;
                break;

            default:
                System.out.println("Switch Statement defaulted in move() in Class State");

                break;
        }

        int[] new_state_board = this.board.clone();

        if(adjustment!=0) {
            new_state_board[blank_index] = new_state_board[blank_index + adjustment];
            new_state_board[blank_index + adjustment] = BLANK;
        }
        State s = new State(new_state_board, this);

//        if(adjustment!=0) {
//            System.out.println("Look ma, b_index: " + blank_index + "   adj: " + adjustment);
//            System.out.println("n:" + n + " d:" + d);
//            this.print();
//            s.print();
//        }
        return s;

    }



    public void print() {
        for(int i=0;i<board.length;i++){

            if (board[i]==BLANK)
                System.out.print("* ");
            else
                System.out.print(board[i]+" ");

            if((i+1)% n==0)
                System.out.println();
        }
        System.out.println();
    }
}
