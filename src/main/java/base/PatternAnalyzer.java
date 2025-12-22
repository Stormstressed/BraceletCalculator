package base;

import java.util.*;

public class PatternAnalyzer {

    public static void analyze(Pattern pattern) {

        List<List<Pattern.KnotCell>> rows = pattern.getRows();
        if (rows == null || rows.isEmpty()) {
            pattern.setValid(false);
            pattern.setRepeats(0);
            pattern.setTotalRows(0);
            return;
        }

        int numStrings = pattern.getStrings().size();

        Debug.log("Analyzer: knotsFirstRow=" + rows.get(0).size() +
                  " → numStrings=" + numStrings +
                  " rows=" + rows.size());

        // initial order 1..N
        List<Integer> initialOrder = new ArrayList<>(numStrings);
        for (int i = 1; i <= numStrings; i++) initialOrder.add(i);
        List<Integer> order = new ArrayList<>(initialOrder);

        // per-string tally
        Map<Integer,Integer> tally = new LinkedHashMap<>();
        for (int i = 1; i <= numStrings; i++) tally.put(i, 0);

        int repeats = 0;
        List<Integer> orderAfterOneLoop = null;

        do {
            repeats++;

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {

                List<Pattern.KnotCell> row = rows.get(rowIndex);

                boolean unusedLeft = false;
                boolean unusedRight = false;

                if (numStrings % 2 == 0) {
                    // EVEN number of strings
                    if (rowIndex % 2 == 1) {
                        unusedLeft = true;
                        unusedRight = true;
                    }
                } else {
                    // ODD number of strings
                    unusedRight = (rowIndex % 2 == 0);
                    unusedLeft  = !unusedRight;
                }

                int startPos = unusedLeft ? 1 : 0;
                int usableStrings = numStrings
                        - (unusedLeft ? 1 : 0)
                        - (unusedRight ? 1 : 0);
                int pairsThisRow = usableStrings / 2;

                Debug.log("Row " + rowIndex +
                          " unusedLeft=" + unusedLeft +
                          " unusedRight=" + unusedRight +
                          " pairs=" + pairsThisRow);

                for (int k = 0; k < pairsThisRow; k++) {

                    int posLeft = startPos + 2 * k;
                    int posRight = posLeft + 1;

                    int leftId = order.get(posLeft);
                    int rightId = order.get(posRight);

                    Pattern.KnotType knot = row.get(k).knot();

                    if (knot == Pattern.KnotType.BLANK ||
                        knot == Pattern.KnotType.UNKNOWN) {
                        continue;
                    }

                    switch (knot) {
                        case F -> {
                            tally.put(leftId, tally.get(leftId) + 1);
                            Collections.swap(order, posLeft, posRight);
                        }
                        case B -> {
                            tally.put(rightId, tally.get(rightId) + 1);
                            Collections.swap(order, posLeft, posRight);
                        }
                        case FB -> tally.put(leftId, tally.get(leftId) + 1);
                        case BF -> tally.put(rightId, tally.get(rightId) + 1);
                    }
                }
            }

            if (repeats == 1) {
                orderAfterOneLoop = new ArrayList<>(order);
            }

        } while (!order.equals(initialOrder));

        Debug.log("Analyzer: repeats=" + repeats +
                  " finalOrder=" + orderAfterOneLoop);

        int totalRows = rows.size() * repeats;
        boolean valid = order.equals(initialOrder);

        // compute string lengths
        Map<Integer,Double> stringLengths = new LinkedHashMap<>();
        for (var e : tally.entrySet()) {
            int id = e.getKey();
            int nKnot = e.getValue();
            int nBase = totalRows - nKnot;

            double len = (pattern.getDesiredBraceletLength() / totalRows) *
                         (8.1 * nKnot + 1.4 * nBase) +
                         pattern.getAllowance();

            stringLengths.put(id, len);
        }

        // compute color totals
        Map<String,Double> colorLengths = new LinkedHashMap<>();
        for (var e : stringLengths.entrySet()) {
            int id = e.getKey();
            String label = pattern.getStrings().get(id - 1).label();
            String color = pattern.getLabelToColor().get(label);
            colorLengths.merge(color, e.getValue(), Double::sum);
        }

        // save results
        pattern.setTally(tally);
        pattern.setRepeats(repeats);
        pattern.setTotalRows(totalRows);
        pattern.setFinalOrder(orderAfterOneLoop);
        pattern.setStringLengths(stringLengths);
        pattern.setColorLengths(colorLengths);
        pattern.setValid(valid);
    }
}
