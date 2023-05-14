import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.hash;

public class test {
    public static void main(String[] args) {
        int[] a = {1,2,3};

        List<Integer> list_a = Arrays.stream(a).boxed().collect(Collectors.toList());

        System.out.println(hash(list_a));

        int[] b = {1,2,3};

        List<Integer> list_b = Arrays.stream(b).boxed().collect(Collectors.toList());

        System.out.println(hash(list_b));



    }
}

