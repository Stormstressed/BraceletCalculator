package base;


import java.util.Scanner;

public class BraceletKnots {
	/*
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== Bracelet Calculator ===");
                System.out.println("Choose input method: 1 = Manual, 2 = Web scrape, 3 = Exit");
                String mode = scanner.nextLine().trim();
                if (mode.isEmpty()) mode = "2";

                if (mode.equals("3")) {
                    System.out.println("Exiting.");
                    break;
                }

                Pattern pattern;
                if (mode.equals("1")) {
                    // Manual input via PatternInput helper
                    System.out.print("\nEnter desired bracelet length in cm (default 15): ");
                    String braceletInput = scanner.nextLine().trim();
                    double lBracelet = braceletInput.isEmpty() ? 15.0 : Double.parseDouble(braceletInput);

                    System.out.print("Enter extra allowance in cm (default 30): ");
                    String extraInput = scanner.nextLine().trim();
                    double lExtra = extraInput.isEmpty() ? 30.0 : Double.parseDouble(extraInput);

                    pattern = PatternInput.fromConsole(scanner, lBracelet, lExtra);

                } else if (mode.equals("2")) {
                    // Web scrape
                    System.out.print("\nEnter the webpage URL or just the pattern number: ");
                    String input = scanner.nextLine().trim();

                    System.out.print("Enter desired bracelet length in cm (default 15): ");
                    String braceletInput = scanner.nextLine().trim();
                    double lBracelet = braceletInput.isEmpty() ? 15.0 : Double.parseDouble(braceletInput);

                    System.out.print("Enter extra allowance in cm (default 30): ");
                    String extraInput = scanner.nextLine().trim();
                    double lExtra = extraInput.isEmpty() ? 30.0 : Double.parseDouble(extraInput);

                    System.out.println("\nWorking... fetching pattern data from the web...");
                    pattern = Scraper.scrapePattern(input, lBracelet, lExtra);
                } else {
                    System.out.println("Invalid choice.");
                    continue;
                }

                // Analyze
                PatternAnalyzer.analyze(pattern);

                // Print results
                System.out.println("\n--- Pattern Summary ---");
                System.out.println(pattern);

                System.out.println("\nKnot tally:");
                pattern.getTally().forEach((id, count) -> {
                    String hex = pattern.getColors().getOrDefault(id, "#000000");
                    String label = pattern.getLabels().getOrDefault(id, "");
                    String colored = ansiColor(hex, "String " + id + (label.isEmpty() ? "" : " [" + label + "]"));
                    System.out.printf("%s (%s): %d knots%n", colored, hex, count);
                });

                System.out.println("\nKnot pattern rows:");
                final int COL_W = 4;            // fixed column width per knot
                final int OFFSET = COL_W / 2;   // half-column offset

                for (int rowIndex = 0; rowIndex < pattern.getKnotRows().size(); rowIndex++) {
                    String[] row = pattern.getKnotRows().get(rowIndex);

                    if (rowIndex % 2 == 1) {
                        System.out.print(" ".repeat(OFFSET)); // 1.5-ish relative to 3, precise half of COL_W
                    }
                    for (String knot : row) {
                        String token = knot.trim();
                        System.out.print(String.format("%-" + COL_W + "s", token));
                    }
                    System.out.println();
                }


                System.out.println("\nString lengths:");
                pattern.getStringLengths().forEach((id, len) -> {
                    String colored = formatColoredString(id, pattern);
                    System.out.printf("%s: %.1f cm%n", colored, len);
                });

                System.out.println("\nTotals per color:");
                pattern.getColorLengths().forEach((hex, total) -> {
                    String brightHex = brightenIfDark(hex);
                    String colored = ansiColor(brightHex, hex);
                    System.out.printf("%s: %.1f cm%n", colored, total);
                });

                System.out.println("\nFinal string order after one loop:");
                for (int id : pattern.getFinalOrder()) {
                    System.out.println(formatColoredString(id, pattern));
                }

                System.out.printf("\nLongest string required: %.1f cm%n",
                        pattern.getStringLengths().values().stream().mapToDouble(Double::doubleValue).max().orElse(0));

                System.out.println("\nPattern verification: " + (pattern.isValid() ? "VALID" : "INVALID"));
                System.out.println("Pattern closed after " + pattern.getRepeats() + " repeats.");
                System.out.println("Total rows used: " + pattern.getTotalRows());

                // Run again prompt
                System.out.print("\nRun again? (y/n): ");
                String again = scanner.nextLine().trim().toLowerCase();
                if (again.isEmpty() || again.equals("y")) {
                    continue;
                } else {
                    System.out.println("Exiting.");
                    break;
                }
            }
        }
    }

    // --- Helpers ---
    private static String formatColoredString(int id, Pattern pattern) {
        String hex = pattern.getColors().getOrDefault(id, "#000000");
        hex = brightenIfDark(hex);
        String label = pattern.getLabels().getOrDefault(id, "");
        String text = "String " + id + (label.isEmpty() ? "" : " [" + label + "]");
        return ansiColor(hex, text);
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

    private static String brightenIfDark(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);

            // Perceived brightness
            double brightness = (0.299 * r + 0.587 * g + 0.114 * b);

            if (brightness < 100) {
                // Target brightness ~160 (softer than full white)
                double factor = 160.0 / Math.max(1.0, brightness);

                r = (int)Math.min(255, r * factor);
                g = (int)Math.min(255, g * factor);
                b = (int)Math.min(255, b * factor);
            }

            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hex;
        }
    }

*/
}

