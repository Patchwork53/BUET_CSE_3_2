import java.util.HashMap;

public class Backtrack{
    HashMap<Variable, Integer> assignment;
    static int depth_to_start_reporting = 1000;
    public long nodes;
    public long backtracks;
    HashMap<Variable, int[][]> vscon;

    public HashMap<Variable, Integer> backtrackStart(CSP problem, Heuristic heuristic, boolean forward_checking){
        nodes=0;
        backtracks=0;
        assignment = new HashMap<>();
        vscon = new HashMap<>();
        if (forward_checking)
            return backtrackWithForwardCheck(assignment, problem, heuristic);
        else
            return backtrack(assignment, problem, heuristic);

    }

    public HashMap<Variable, Integer> backtrack(HashMap<Variable, Integer> assignment, CSP problem, Heuristic heuristic){
        if (assignment.size() == problem.unassigned_count){
            return  assignment;
        }

        nodes++;

        Variable to_be_assigned = heuristic.heuristic(problem);
        boolean hasChildren = false;
        problem.unassigned.remove(to_be_assigned);
        try {
            for(Integer value: to_be_assigned.domain){


                assignment.put(to_be_assigned, value);
                problem.board[to_be_assigned.i][to_be_assigned.j] = value;
                //domain_reduction
                var victims = problem.reduceDomains (to_be_assigned, value);


                var result = backtrack(assignment, problem, heuristic);
                if (result.size() != 0)
                    return result;

                //failed assignment
                hasChildren = true;
                problem.reimburseDomains(victims.arr, value);
                problem.board[to_be_assigned.i][to_be_assigned.j] = 0;
                assignment.remove(to_be_assigned);


            }

        }
        catch (Exception e){
            System.out.println(to_be_assigned);
            System.exit(1);
        }


        if (assignment.size()>depth_to_start_reporting)
            System.out.println("failed at depth: "+ assignment.size());

        if (!hasChildren)
            backtracks++;

        problem.unassigned.add(to_be_assigned);
        return new HashMap<>();
    }



    public HashMap<Variable, Integer> backtrackWithForwardCheck(HashMap<Variable, Integer> assignment, CSP problem, Heuristic heuristic){
        if (assignment.size() == problem.unassigned_count){
            return  assignment;
        }

        nodes++;


        Variable to_be_assigned = heuristic.heuristic(problem);
        boolean hasChildren = false;

        if (problem.board[to_be_assigned.i][to_be_assigned.j]!=0){
            System.out.println(to_be_assigned);
            System.out.println("TAI HEN DA");
        }
//        if (vscon.get(to_be_assigned)!= null && util.deepCompare2D(vscon.get(to_be_assigned), problem.board))
//            System.out.println("here comes trouble");
//        else
//            vscon.put(to_be_assigned, util.deepCopy2D(problem.board));

        problem.unassigned.remove(to_be_assigned);
        try {
            for(Integer value: to_be_assigned.domain){
                assignment.put(to_be_assigned, value);
                problem.board[to_be_assigned.i][to_be_assigned.j] = value;
                //forward checking
                var victims = problem.reduceDomains (to_be_assigned, value);

                if ( !victims.aBoolean ) {  //no domain was reduced to 0
                    var result = backtrackWithForwardCheck(assignment, problem, heuristic);
                    if (result.size() != 0)
                        return result;

                    hasChildren = true;
                }
                //failed assignment


                problem.reimburseDomains(victims.arr, value);
                problem.board[to_be_assigned.i][to_be_assigned.j] = 0;
                assignment.remove(to_be_assigned);


            }

        }
        catch (Exception e){
            System.out.println(to_be_assigned);
            System.exit(1);
        }


        if (assignment.size()>depth_to_start_reporting)
            System.out.println("failed at depth: "+ assignment.size());

        if (!hasChildren)
            backtracks++;

        problem.unassigned.add(to_be_assigned);
        return new HashMap<>();
    }
}
