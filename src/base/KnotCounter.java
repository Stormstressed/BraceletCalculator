package base;

import java.util.*;

public class KnotCounter {

	public static class Result {
	    Map<Integer, Integer> tally;
	    int repeats;
	    int totalRows;
	    List<Integer> finalOrder;
	}

	public static Result countUntilCycle(List<String[]> rows) {
	    int numKnotsFirstRow = rows.get(0).length;
	    int numStrings = numKnotsFirstRow * 2;

	    Map<Integer, Integer> tally = new LinkedHashMap<>();
	    for (int i = 1; i <= numStrings; i++) tally.put(i, 0);

	    List<Integer> initialOrder = new ArrayList<>();
	    for (int i = 1; i <= numStrings; i++) initialOrder.add(i);

	    List<Integer> order = new ArrayList<>(initialOrder);

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
	                    case "f":
	                        tally.put(leftId, tally.get(leftId) + 1);
	                        Collections.swap(order, posLeft, posRight);
	                        break;
	                    case "b":
	                        tally.put(rightId, tally.get(rightId) + 1);
	                        Collections.swap(order, posLeft, posRight);
	                        break;
	                    case "fb":
	                        tally.put(leftId, tally.get(leftId) + 1);
	                        break;
	                    case "bf":
	                        tally.put(rightId, tally.get(rightId) + 1);
	                        break;
	                }
	            }
	        }

	        if (order.equals(initialOrder)) {
	            Result result = new Result();
	            result.tally = tally;
	            result.repeats = repeats;
	            result.totalRows = rows.size() * repeats;
	            result.finalOrder = new ArrayList<>(order);
	            return result;
	        }
	    }
	}

}

