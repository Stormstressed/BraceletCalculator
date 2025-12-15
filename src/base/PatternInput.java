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

    public static List<String[]> fromWebpage(String urlOrId) {
        String url = urlOrId.trim();

        // If the user just typed a number, build the full URL
        if (url.matches("\\d+")) {
            url = "https://www.braceletbook.com/patterns/normal/" + url + "/";
        }

        return Scraper.scrapePattern(url);
    }
}