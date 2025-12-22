package base;

import java.util.*;

public class PatternInput {
    public static Pattern fromConsole(Scanner scanner, double desiredLength, double allowance) {
        List<String[]> rows = new ArrayList<>();
        System.out.println("\nPaste the pattern row by row (comma separated knots). End with empty line:");
        while (true) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) break;
            // Normalize row tokens to avoid spacing issues from monospaced paste
            String[] raw = line.trim().split("[,\\s]+");
            for (int i = 0; i < raw.length; i++) {
                raw[i] = raw[i].toLowerCase();
            }
            rows.add(raw);
        }

        int numStrings = rows.get(0).length * 2;

        Map<Integer, String> labels = new HashMap<>();
        Map<Integer, String> colors = new HashMap<>();

        // Collect labels
        Set<String> uniqueLabels = new LinkedHashSet<>();
        for (int i = 1; i <= numStrings; i++) {
            System.out.print("Label for string " + i + ": ");
            String labelInput = scanner.nextLine();
            String normalized = normalizeLabel(labelInput);
            if (normalized.isEmpty()) normalized = "S" + i;
            labels.put(i, normalized);
            uniqueLabels.add(normalized);
        }

        // Assign distinct colors per label
        Map<String,String> labelColorMap = generateDistinctColors(uniqueLabels);
        for (int i = 1; i <= numStrings; i++) {
            String label = labels.get(i);
            colors.put(i, labelColorMap.get(label));
        }

        return new Pattern("manual","null", colors, labels, rows, desiredLength, allowance);
    }

    private static String normalizeLabel(String s) {
        if (s == null) return "";
        // Trim, collapse multiple spaces to single, then remove spaces entirely (if you prefer keep single spaces, drop .replace(" ", ""))
        String collapsed = s.trim().replaceAll("\\s+", " ");
        // If you want to remove spaces to match monospaced paste quirks:
        collapsed = collapsed.replace(" ", "");
        return collapsed;
    }

    private static String hsvToHex(double h, double s, double v) {
        double c = v * s;
        double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
        double m = v - c;

        double rPrime, gPrime, bPrime;
        if (h < 60)      { rPrime = c; gPrime = x; bPrime = 0; }
        else if (h < 120){ rPrime = x; gPrime = c; bPrime = 0; }
        else if (h < 180){ rPrime = 0; gPrime = c; bPrime = x; }
        else if (h < 240){ rPrime = 0; gPrime = x; bPrime = c; }
        else if (h < 300){ rPrime = x; gPrime = 0; bPrime = c; }
        else             { rPrime = c; gPrime = 0; bPrime = x; }

        int r = (int)Math.round((rPrime + m) * 255);
        int g = (int)Math.round((gPrime + m) * 255);
        int b = (int)Math.round((bPrime + m) * 255);

        return String.format("#%02x%02x%02x", r, g, b);
    }

    private static Map<String,String> generateDistinctColors(Set<String> labels) {
        Map<String,String> colorMap = new HashMap<>();
        int n = labels.size();
        int i = 0;
        for (String label : labels) {
            double hue = (360.0 / n) * i; // evenly spaced hues
            String hex = hsvToHex(hue, 0.8, 0.8); // softer saturation & brightness
            colorMap.put(label, hex);
            i++;
        }
        return colorMap;
    }

}