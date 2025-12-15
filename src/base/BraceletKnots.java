package base;

import java.util.*;

import base.KnotCounter.Result;

public class BraceletKnots {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose input method: 1 = Pattern, 2 = Webpage");
        String mode = scanner.nextLine().trim();

        List<String[]> rows = new ArrayList<>();

        if (mode.equals("1")) {
            System.out.println("Paste the entire pattern (rows separated by newlines). Press Enter once when done:");
            StringBuilder allInput = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty()) break; // stop on single Enter
                allInput.append(line).append("\n");
            }
            // Split into rows
            String[] rawRows = allInput.toString().trim().split("\n");
            for (String row : rawRows) {
                rows.add(row.split(","));
            }
        } else if (mode.equals("2")) {
        	System.out.println("Paste the webpage URL or just the pattern number:");
            String input = scanner.nextLine().trim();
            rows = PatternInput.fromWebpage(input);
            System.out.println("Scraped " + rows.size() + " rows.");
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        Result result = KnotCounter.countUntilCycle(rows);
        ResultPrinter.printTally(result.tally);
        System.out.println("Pattern closed after " + result.repeats + " repeats.");
        System.out.println("Total rows used: " + result.totalRows);

        // Ask user for bracelet length and extra allowance, with defaults
        System.out.print("Enter desired bracelet length in cm (default 15): ");
        String braceletInput = scanner.nextLine().trim();
        double lBracelet = braceletInput.isEmpty() ? 15.0 : Double.parseDouble(braceletInput);

        System.out.print("Enter extra allowance in cm (default 30): ");
        String extraInput = scanner.nextLine().trim();
        double lExtra = extraInput.isEmpty() ? 30.0 : Double.parseDouble(extraInput);

        // Calculate string lengths
        Map<Integer, Double> lengths = StringLengthCalculator.calculateLengths(
                result.tally, result.totalRows, lBracelet, lExtra);

        System.out.println("\nEstimated string lengths:");
        lengths.forEach((id, len) ->
                System.out.printf("String %d: %.1f cm%n", id, len));

        // Optional: print max length needed
        double maxLen = lengths.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        System.out.printf("\nLongest string required: %.1f cm%n", maxLen);

        // Verification
        boolean valid = Verifier.verifyPattern(rows);
        System.out.println("Pattern verification: " + (valid ? "VALID" : "INVALID"));
    }
}



