package base;

import java.util.List;

public class StringAnalyzer {

    public static int[] countActiveKnots(List<String[]> rows) {
        int strings = rows.get(0).length + 1; // normal patterns have n+1 strings
        int[] counts = new int[strings];
        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                String knot = row[i];
                if (knot.equals("f") || knot.equals("b") || knot.equals("fb") || knot.equals("bf")) {
                    counts[i]++;
                    counts[i+1]++;
                }
            }
        }
        return counts;
    }

    public static double[] estimateLengths(int[] counts, int totalRows, double desiredLength, double allowance) {
        double[] lengths = new double[counts.length];
        for (int i = 0; i < counts.length; i++) {
            lengths[i] = (desiredLength / totalRows) * counts[i] + allowance;
        }
        return lengths;
    }
}
