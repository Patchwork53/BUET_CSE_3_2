import javax.sound.midi.Soundbank;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;


public class Main {
    public static boolean is_n_puzzle_solvable(int n, int arr[]){

        int inversion_count = 0;
        int BLANK = n*n;
        int BLANK_INDEX = -1;
        for(int i=0;i<n*n;i++){
            if (arr[i]==BLANK){
                BLANK_INDEX = i;
                continue;
            }
            for(int j=i+1;j<n*n;j++){
                if (arr[j]==BLANK)
                    continue;

                if(arr[i]>arr[j])
                    inversion_count++;

            }
        }

        int BLANK_ROW_FROM_BOTTOM = n - BLANK_INDEX/n;

        if (n%2==0)
            return (inversion_count%2==0 && BLANK_ROW_FROM_BOTTOM%2==1) || (inversion_count%2==1 && BLANK_ROW_FROM_BOTTOM%2==0);

        else
            return inversion_count%2==0;

    }

    public static void a_star_search(State start_state){
        Queue<State> pq = new PriorityQueue<>(new Comparator<State>()
        {
            @Override
            public int compare(State a, State b)
            {
                return a.getPriority()-b.getPriority();
            }
        });


        pq.add(start_state);

        pq.add(start_state.move(DIRECTION.DOWN));

        HashSet<List<Integer>> visited = new HashSet<>();

        State s = null;
        while (!pq.isEmpty()){
            s=pq.remove();

            if (s.is_goal())
                break;

            List<Integer> temp_list = Arrays.stream(s.board).boxed().collect(Collectors.toList());

            if (visited.contains(temp_list))
                continue;
//            System.out.println("POPPED WITH PRIORITY: "+s.getPriority());
//            s.print();
            visited.add(temp_list);

            for(DIRECTION d: DIRECTION.values()){
                State new_state = s.move(d);

                if (Arrays.equals(new_state.board, s.board)){
                    continue;
                }
                List<Integer> temp_list2 = Arrays.stream(new_state.board).boxed().collect(Collectors.toList());

                if(!visited.contains(temp_list2)) {
//                    System.out.println("PUSHED WITH PRIORITY: "+new_state.getPriority());
//                    new_state.print();
                    pq.add(new_state);
                }
            }

//            sleep(1000);
        }

        System.out.println("Number of moves: "+ s.num_moves);
        System.out.println("Expanded Nodes: "+visited.size());
        System.out.println("Explored(Reached) Nodes: "+(pq.size()+visited.size()));

        System.out.println("Print path?:[y/n]");
        Scanner scanner = new Scanner(System.in);
        String c = scanner.next();

        if (Objects.equals(c, "n"))
            return;

        Stack<State> print_stack = new Stack<>();
        State x = s;
        while (x!=null){
            print_stack.push(x);
            x = x.parent;
        }

        while(!print_stack.isEmpty())
            print_stack.pop().print();

    }

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        File myObj = new File("input.txt");
        Scanner myReader = new Scanner(myObj);
        int n = myReader.nextInt();
//        System.out.println(n);
        int[] board = new int[n*n];


        for(int i=0;i<n*n;i++){
            String s = myReader.next();
            if (Objects.equals(s, "*"))
                board[i]=n*n;
            else
                board[i]=Integer.parseInt(s);
        }

//        System.out.println(Arrays.toString(board));

        if(!is_n_puzzle_solvable(n, board)){
            System.out.println("PUZZLE NOT SOLVABLE");
            System.exit(0);
        }



        State start_state = new State(n, board, true);

        System.out.println("____MANHATTEN____");
        a_star_search(start_state);

        start_state.manhatten = false;

        System.out.println("_____HAMMING_____");
        a_star_search(start_state);

    }


}
