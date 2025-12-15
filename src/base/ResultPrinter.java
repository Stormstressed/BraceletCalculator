package base;

import java.util.*;

public class ResultPrinter {
    public static void printTally(Map<Integer, Integer> tally) {
        tally.forEach((id, count) -> System.out.println("String " + id + ": " + count + " active knots"));
    }
}
