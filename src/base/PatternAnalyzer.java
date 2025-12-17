package base;

import java.util.*;

public class PatternAnalyzer {

    public static void analyze(Pattern pattern) {
        List<String[]> rows = pattern.getKnotRows();
        int numKnotsFirstRow = rows.get(0).length;
        int numStrings = numKnotsFirstRow * 2;

        Map<Integer,Integer> tally = new LinkedHashMap<>();
        for (int i = 1; i <= numStrings; i++) tally.put(i, 0);

        List<Integer> initialOrder = new ArrayList<>();
        for (int i = 1; i <= numStrings; i++) initialOrder.add(i);
        List<Integer> order = new ArrayList<>(initialOrder);
        List<Integer> orderAfterOneLoop = null;

        int repeats = 0;
        while (true) {
            repeats++;
         // simulate one full pattern
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
                    switch (knot) {
                        case "f" -> { tally.put(leftId, tally.get(leftId) + 1); Collections.swap(order,posLeft,posRight); }
                        case "b" -> { tally.put(rightId, tally.get(rightId) + 1); Collections.swap(order,posLeft,posRight); }
                        case "fb" -> tally.put(leftId, tally.get(leftId) + 1);
                        case "bf" -> tally.put(rightId, tally.get(rightId) + 1);
                    }
                }
            }
            // capture order after the first loop
            if (repeats == 1) {
                orderAfterOneLoop = new ArrayList<>(order);
            }
            if (order.equals(initialOrder)) break;
        }

        int totalRows = rows.size() * repeats;

        // Calculate lengths
        Map<Integer,Double> stringLengths = new LinkedHashMap<>();
        for (var e : tally.entrySet()) {
            int id = e.getKey();
            int nKnot = e.getValue();
            int nBase = totalRows - nKnot;
            double len = (pattern.getDesiredBraceletLength() / totalRows) *
                         (8.1 * nKnot + 1.4 * nBase) + pattern.getAllowance();
            stringLengths.put(id, len);
        }

        Map<String,Double> colorLengths = new HashMap<>();
        stringLengths.forEach((id,len) -> {
            String hex = pattern.getColors().getOrDefault(id, "#000000");
            colorLengths.merge(hex, len, Double::sum);
        });

        boolean valid = order.equals(initialOrder);

        // Populate Pattern fields
        pattern.setTally(tally);
        pattern.setRepeats(repeats);
        pattern.setTotalRows(totalRows);
        pattern.setFinalOrder(orderAfterOneLoop);
        pattern.setStringLengths(stringLengths);
        pattern.setColorLengths(colorLengths);
        pattern.setValid(valid);
    }
}
