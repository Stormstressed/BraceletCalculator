package base;

import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {

    public static List<String[]> scrapePattern(String normalUrl) {
        List<String[]> rows = new ArrayList<>();
        try {
            // Step 1: fetch normal page
            Document doc = Jsoup.connect(normalUrl).get();
            Element object = doc.selectFirst("object.pattern_svg");
            if (object == null) {
                System.err.println("No SVG object found on page.");
                return rows;
            }
            String svgUrl = object.attr("data");

            // Step 2: fetch SVG
            Document svgDoc = Jsoup.connect(svgUrl).get();

            // Step 3: parse knots
            Map<Integer, List<String>> rowMap = new TreeMap<>();
            Elements groups = svgDoc.select("g.k");
            for (Element g : groups) {
                Element ellipse = g.selectFirst("ellipse");
                if (ellipse == null) continue;
                int cy = Integer.parseInt(ellipse.attr("cy"));

                Element use = g.selectFirst("use");
                if (use == null) continue;
                String href = use.attr("xlink:href");

                String knotType = switch (href) {
                    case "#kf" -> "f";
                    case "#kb" -> "b";
                    case "#kfb" -> "fb";
                    case "#kbf" -> "bf";
                    default -> "?";
                };

                rowMap.putIfAbsent(cy, new ArrayList<>());
                rowMap.get(cy).add(knotType);
            }

            // Step 4: convert to rows
            for (List<String> row : rowMap.values()) {
                String[] arr = row.toArray(new String[0]);
                rows.add(arr);
            }

            // Step 5: print rows to console
            System.out.println("Scraped pattern rows:");
            for (String[] row : rows) {
                System.out.println(String.join(",", row));
            }

        } catch (Exception e) {
            System.err.println("Error scraping pattern: " + e.getMessage());
        }
        return rows;
    }
}


