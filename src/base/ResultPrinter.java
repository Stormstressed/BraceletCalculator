package base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultPrinter {

    // Original tally printer
    public static void printTally(Map<Integer, Integer> tally) {
    	System.out.println(""); //empty line
        tally.forEach((id, count) ->
            System.out.println("String " + id + ": " + count + " active knots"));
    }

    // New color-aware length printer
    public static void printLengthsWithColors(Map<Integer, Double> lengths,
                                              Map<Integer, String> stringColors,
                                              Map<Integer, String> stringLabels) {
        System.out.println("\nEstimated string lengths:");
        lengths.forEach((id, len) -> {
            String hex = safeHex(stringColors, id);
            String labelTxt = (stringLabels != null && stringLabels.get(id) != null)
                    ? stringLabels.get(id) : "";
            String name = "String " + id + (labelTxt.isEmpty() ? "" : " [" + labelTxt + "]");
            String colored = ansiColor(hex, name);
            System.out.printf("%s (%s): %.1f cm%n", colored, hex, len);
        });

        // Totals per color
        Map<String, Double> totals = new HashMap<>();
        lengths.forEach((id, len) -> {
            String hex = safeHex(stringColors, id);
            totals.merge(hex, len, Double::sum);
        });
        System.out.println("\nTotals per color:");
        totals.forEach((hex, total) -> {
            // find any string that uses this hex to get its label
            String label = "";
            if (stringLabels != null) {
                for (Map.Entry<Integer, String> e : stringLabels.entrySet()) {
                    String h = safeHex(stringColors, e.getKey());
                    if (h.equals(hex)) {
                        label = e.getValue();
                        break;
                    }
                }
            }
            String display = hex + (label.isEmpty() ? "" : " [" + label + "]");
            String colored = ansiColor(hex, display);
            System.out.printf("%s: %.1f cm%n", colored, total);
        });

    }
    
    public static void printScrapedRows(List<String[]> rows) {
        System.out.println("\nScraped pattern rows:");
        for (String[] row : rows) {
            System.out.println(String.join(",", row));
        }
    }

    // Helpers
    private static String safeHex(Map<Integer, String> stringColors, int id) {
        if (stringColors == null) return "#000000";
        String hex = stringColors.get(id);
        if (hex == null || hex.isBlank()) return "#000000";
        return hex;
    }

    private static String ansiColor(String hex, String text) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return String.format("\u001B[38;2;%d;%d;%dm%s\u001B[0m", r, g, b, text);
        } catch (Exception e) {
            return text;
        }
    }
}
