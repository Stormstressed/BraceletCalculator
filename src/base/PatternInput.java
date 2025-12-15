package base;

import java.util.*;

public class PatternInput {
    public static List<String[]> fromConsole(Scanner scanner) {
        List<String[]> rows = new ArrayList<>();
        System.out.println("Paste the entire pattern, row by row. End with an empty line:");
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) break;
            rows.add(line.split(","));
        }
        return rows;
    }

    public static Scraper.ScrapeResult fromWebpage(String input) {
        String url = input.matches("\\d+")
            ? "https://www.braceletbook.com/patterns/normal/" + input + "/"
            : input;
        return Scraper.scrapePattern(url);
    }
}