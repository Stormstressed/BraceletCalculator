package base;

import java.util.*;
import base.KnotCounter.Result;

public class BraceletKnots {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\nChoose input method: 1 = Pattern, 2 = Webpage (default 2)");
        String mode = scanner.nextLine().trim();
        if (mode.isEmpty()) mode = "2";
        
        List<String[]> rows = new ArrayList<>();
        Map<Integer, String> stringColors = null;
        Map<Integer, String> stringLabels = null;

        if (mode.equals("1")) {
            System.out.println("\nPaste the entire pattern (rows separated by newlines). Press Enter once when done:");
            StringBuilder allInput = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isEmpty()) break;
                allInput.append(line).append("\n");
            }
            String[] rawRows = allInput.toString().trim().split("\n");
            for (String row : rawRows) {
                rows.add(row.split(","));
            }
        } else if (mode.equals("2")) {
            System.out.println("\nPaste the webpage URL or just the pattern number:");
            String input = scanner.nextLine().trim();
            Scraper.ScrapeResult scrape = PatternInput.fromWebpage(input);
            rows = scrape.rows;
            stringColors = scrape.stringColors;
            stringLabels = scrape.stringLabels;
            System.out.println("\nScraped " + rows.size() + " rows.");
            ResultPrinter.printScrapedRows(rows);
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        Result result = KnotCounter.countUntilCycle(rows);
        ResultPrinter.printTally(result.tally);
        System.out.println("\nPattern closed after " + result.repeats + " repeats.");
        System.out.println("\nTotal rows used: " + result.totalRows);

        System.out.print("\nEnter desired bracelet length in cm (default 15): ");
        String braceletInput = scanner.nextLine().trim();
        double lBracelet = braceletInput.isEmpty() ? 15.0 : Double.parseDouble(braceletInput);

        System.out.print("\nEnter extra allowance in cm (default 30): ");
        String extraInput = scanner.nextLine().trim();
        double lExtra = extraInput.isEmpty() ? 30.0 : Double.parseDouble(extraInput);

        Map<Integer, Double> lengths = StringLengthCalculator.calculateLengths(
                result.tally, result.totalRows, lBracelet, lExtra);

        ResultPrinter.printLengthsWithColors(lengths, stringColors, stringLabels);

        double maxLen = lengths.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        System.out.printf("\nLongest string required: %.1f cm%n", maxLen);

        boolean valid = Verifier.verifyPattern(rows);
        System.out.println("\nPattern verification: " + (valid ? "VALID" : "INVALID"));
    }
}
