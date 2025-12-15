package base;

import java.util.*;

public class StringLengthCalculator {

    public static Map<Integer, Double> calculateLengths(
            Map<Integer, Integer> tally,
            int totalRows,
            double lBracelet,
            double lExtra) {

        Map<Integer, Double> lengths = new LinkedHashMap<>();

        for (Map.Entry<Integer, Integer> e : tally.entrySet()) {
            int stringId = e.getKey();
            int nKnot = e.getValue();
            int nBase = totalRows - nKnot;

            double lString = (lBracelet / totalRows) * (8.1 * nKnot + 1.4 * nBase) + lExtra;
            lengths.put(stringId, lString);
        }

        return lengths;
    }
}
