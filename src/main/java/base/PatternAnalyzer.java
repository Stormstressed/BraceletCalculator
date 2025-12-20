package base;

import java.util.*;

public class PatternAnalyzer {

    public static void analyze(Pattern pattern) {
        List<String[]> rows = pattern.getKnotRows();
        if (rows == null || rows.isEmpty()) {
            pattern.setValid(false);
            pattern.setRepeats(0);
            pattern.setTotalRows(0);
            pattern.setKnotLabelRows(Collections.emptyList());
            return;
        }

        int numKnotsFirstRow = rows.get(0).length;
        int numStrings = numKnotsFirstRow * 2;

        // initial order 1..N
        List<Integer> initialOrder = new ArrayList<>(numStrings);
        for (int i = 1; i <= numStrings; i++) initialOrder.add(i);
        List<Integer> order = new ArrayList<>(initialOrder);

        // per-string tally
        Map<Integer,Integer> tally = new LinkedHashMap<>();
        for (int i = 1; i <= numStrings; i++) tally.put(i, 0);

        // enriched rows for ONE loop (type + label)
        List<List<String[]>> knotLabelRows = new ArrayList<>();

        // repeat tracking
        int repeats = 0;
        List<Integer> orderAfterOneLoop = null;

        // simulate until we return to initial order
        do {
            repeats++;

            for (int r = 0; r < rows.size(); r++) {
                String[] row = rows.get(r);
                int pairsThisRow = row.length;           // may be fullPairs or fullPairs-1
                int fullPairs = numStrings / 2;
                int startPos = (pairsThisRow == fullPairs) ? 0 : 1; // odd rows idle ends

                // capture enriched row only for the first loop
                List<String[]> enrichedRow = (repeats == 1) ? new ArrayList<>() : null;

                for (int k = 0; k < pairsThisRow; k++) {
                    int posLeft = startPos + 2 * k;
                    int posRight = posLeft + 1;

                    int leftId = order.get(posLeft);
                    int rightId = order.get(posRight);

                    String knot = row[k].trim().toLowerCase(Locale.ROOT);

                    // main owner BEFORE swap
                    int mainId = switch (knot) {
                        case "f", "fb" -> leftId;
                        case "b", "bf" -> rightId;
                        default -> leftId;
                    };

                    // record enriched pair for the first loop
                    if (enrichedRow != null) {
                        String label = pattern.getLabels().getOrDefault(mainId, String.valueOf(mainId));
                        enrichedRow.add(new String[]{knot, label});
                    }

                    // tally + swap behavior
                    switch (knot) {
                        case "f" -> {
                            tally.put(leftId, tally.get(leftId) + 1);
                            Collections.swap(order, posLeft, posRight);
                        }
                        case "b" -> {
                            tally.put(rightId, tally.get(rightId) + 1);
                            Collections.swap(order, posLeft, posRight);
                        }
                        case "fb" -> {
                            tally.put(leftId, tally.get(leftId) + 1);
                            // no swap
                        }
                        case "bf" -> {
                            tally.put(rightId, tally.get(rightId) + 1);
                            // no swap
                        }
                        default -> {
                            // treat as no-op; no tally, no swap
                        }
                    }
                }

                if (enrichedRow != null) knotLabelRows.add(enrichedRow);
            }

            if (repeats == 1) {
                orderAfterOneLoop = new ArrayList<>(order);
            }
        } while (!order.equals(initialOrder));

        int totalRows = rows.size() * repeats;
        boolean valid = order.equals(initialOrder);

        // lengths (generic proportional model; adjust constants to your calibration)
        Map<Integer,Double> stringLengths = new LinkedHashMap<>();
        for (var e : tally.entrySet()) {
            int id = e.getKey();
            int nKnot = e.getValue();
            int nBase = totalRows - nKnot;
            double len = (pattern.getDesiredBraceletLength() / totalRows) *
                         (8.1 * nKnot + 1.4 * nBase) + pattern.getAllowance();
            stringLengths.put(id, len);
        }

        Map<String,Double> colorLengths = new LinkedHashMap<>();
        stringLengths.forEach((id, len) -> {
            String hex = pattern.getColors().getOrDefault(id, "#000000");
            colorLengths.merge(hex, len, Double::sum);
        });

        // save results back into Pattern
        pattern.setTally(tally);
        pattern.setRepeats(repeats);
        pattern.setTotalRows(totalRows);
        pattern.setFinalOrder(orderAfterOneLoop != null ? orderAfterOneLoop : Collections.emptyList());
        pattern.setStringLengths(stringLengths);
        pattern.setColorLengths(colorLengths);
        pattern.setValid(valid);
        pattern.setKnotLabelRows(knotLabelRows);
    }
}
