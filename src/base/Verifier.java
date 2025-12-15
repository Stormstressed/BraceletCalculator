package base;

import java.util.*;

public class Verifier {
    public static boolean verifyPattern(List<String[]> rows) {
        if (rows.isEmpty()) return false;

        int numKnotsFirstRow = rows.get(0).length;
        int numStrings = numKnotsFirstRow * 2;

        List<Integer> order = new ArrayList<>();
        for (int i = 1; i <= numStrings; i++) order.add(i);

        for (String[] row : rows) {
            int pairsThisRow = row.length;
            int fullPairs = numStrings / 2;
            int startPos = (pairsThisRow == fullPairs) ? 0 : 1;

            for (int k = 0; k < pairsThisRow; k++) {
                int posLeft = startPos + 2 * k;
                int posRight = posLeft + 1;

                int leftId = order.get(posLeft);
                int rightId = order.get(posRight);

                String knot = row[k].trim().toLowerCase();

                if (knot.equals("f") || knot.equals("b")) {
                    Collections.swap(order, posLeft, posRight);
                }
                // fb/bf → no swap
            }
        }

        // Print final order for debugging
        System.out.println("Final string order: " + order);

        boolean valid = true;
        for (int i = 0; i < numStrings; i++) {
            if (order.get(i) != i + 1) {
                valid = false;
                break;
            }
        }

        if (!valid) {
            System.out.println("Pattern did not return to initial order. It may still be correct if it cycles over multiple repeats.");
        }

        return valid;
    }
}

