
public interface Heuristic{
    Variable heuristic(CSP problem);
    String   getName();
}


class MRV implements Heuristic{

    public static final String name = "MRV";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Variable heuristic(CSP problem) {
        Variable v = null;

        int num_domain = Integer.MAX_VALUE;
        for(Variable obj: problem.unassigned){
            if (obj.domain.size()<num_domain){
                v = obj;
                num_domain = obj.domain.size();
            }
        }

        return v;
    }
}
class MAXDegree implements Heuristic{

    public static final String name = "Max Degree";
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Variable heuristic(CSP problem) {

        int max_count = Integer.MIN_VALUE;

        Variable v = null;
        for (Variable obj: problem.unassigned){

            int count = 0;
            for(int i=0; i<problem.board.length;i++){
                if (problem.board[i][obj.j] == 0)
                    count++;
                if (problem.board[obj.i][i] == 0)
                    count++;
            }
            count-=2;
            if (count == 0)
                return obj;

            if (count > max_count) {
                max_count = count;
                v = obj;
            }

        }
//        System.out.println(v+" - "+ max_count);
        return v;

    }

}

class RandomSelection implements Heuristic{

    public static final String name = "Random";
    @Override
    public String getName() {
        return name;
    }
    @Override
    public Variable heuristic(CSP problem) {

        return problem.unassigned.iterator().next();
//            int size = problem.unassigned.size();
//            int item = new Random().nextInt(size);
//            int i = 0;
//            Variable to_return = null;
//            for(Variable obj : problem.unassigned){
//                if (i == item)
//                    to_return = obj;
//                i++;
//            }
//
//            return to_return;

    }
}

class MRVMaxDegreeTieBreaks implements Heuristic{


    public static final String name = "MRV with Max Degree Tie Breaks";
    @Override
    public String getName() {
        return name;
    }
    @Override
    public Variable heuristic(CSP problem) {
        Variable v = null;
        int num_domain = Integer.MAX_VALUE;

        for(Variable obj: problem.unassigned){
            if (obj.domain.size() < num_domain){
                v = obj;
                num_domain = obj.domain.size();

            }
            else if (obj.domain.size() == num_domain){
                v = degreeCompare(v, obj, problem);
            }
        }

        return v;
    }

    public static Variable degreeCompare(Variable v1, Variable v2, CSP problem) {

        int count1 = 0;
        for(int i=0; i<problem.board.length;i++){
            if (problem.board[i][v1.j] == 0)
                count1++;
            if (problem.board[v1.i][i] == 0)
                count1++;
        }

        int count2 = 0;
        for(int i=0; i<problem.board.length;i++){
            if (problem.board[i][v2.j] == 0)
                count2++;
            if (problem.board[v2.i][i] == 0)
                count2++;
        }

        if (count1 > count2)
            return v1;
        else
            return v2;

    }
}


class MRVByMaxDegree implements Heuristic{


    public static final String name = "MRV/MaxDegree";
    @Override
    public String getName() {
        return name;
    }
    @Override
    public Variable heuristic(CSP problem) {

        Double min = Double.MAX_VALUE;
        Variable v = null;
        for (var obj: problem.unassigned){
            if (getDegree(obj, problem)==0 || obj.domain.size()/getDegree(obj, problem)<min){
                v = obj;
                min = obj.domain.size()/getDegree(obj, problem);
            }
        }
        return v;
    }

    public static double getDegree(Variable v, CSP problem){
        int count = 0;
        for(int i=0; i<problem.board.length;i++){
            if (problem.board[i][v.j] == 0)
                count++;
            if (problem.board[v.i][i] == 0)
                count++;
        }
        count-=2;
        return count;
    }
}
