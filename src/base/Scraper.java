package base;

import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {

    public static class ScrapeResult {
        public final List<String[]> rows;
        public final Map<Integer, String> stringColors; // index → hex
        public final Map<Integer, String> stringLabels; // index → label (A/B/…)

        public ScrapeResult(List<String[]> rows,
                            Map<Integer, String> colors,
                            Map<Integer, String> labels) {
            this.rows = rows;
            this.stringColors = colors;
            this.stringLabels = labels;
        }
    }

    public static ScrapeResult scrapePattern(String normalUrl) {
        List<String[]> rows = new ArrayList<>();
        Map<Integer, String> stringColors = new HashMap<>();
        Map<Integer, String> stringLabels = new HashMap<>();

        try {
            // Step 1: fetch normal page
            Document doc = Jsoup.connect(normalUrl).get();
            Element object = doc.selectFirst("object.pattern_svg");
            if (object == null) {
                System.err.println("No SVG object found on page.");
                return new ScrapeResult(rows, stringColors, stringLabels);
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

            for (List<String> row : rowMap.values()) {
                rows.add(row.toArray(new String[0]));
            }

            // Step 4: parse palette from <style>
            Map<String, String> palette = parsePalette(svgDoc);

            // Step 5: parse strings with labels and colors
            Elements stringGroups = svgDoc.select("g.s");
            class SItem { double x; String label; String hex; }
            List<SItem> items = new ArrayList<>();

            for (Element g : stringGroups) {
                // find stroke class
                Element use = g.selectFirst("use.s1");
                String hex = "#000000";
                if (use != null) {
                    for (String cls : use.classNames()) {
                        if (cls.startsWith("s1-") && cls.length() > 3) {
                            String suf = cls.substring(3);
                            hex = palette.getOrDefault(suf, "#000000");
                            break;
                        }
                    }
                }

                // find top label text (smallest y)
                String label = "";
                double minY = Double.MAX_VALUE;
                for (Element t : g.select("text")) {
                    if (t.hasAttr("y")) {
                        try {
                            double y = Double.parseDouble(t.attr("y"));
                            if (y < minY) {
                                String txt = t.text().trim();
                                if (!txt.isEmpty()) {
                                    minY = y;
                                    label = txt;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }


                // x-position for ordering
                double x = 0.0;
                Element t = g.selectFirst("text");
                if (t != null && t.hasAttr("x")) {
                    try { x = Double.parseDouble(t.attr("x")); } catch (Exception ignored) {}
                }

                SItem item = new SItem();
                item.x = x; item.label = label; item.hex = hex;
                items.add(item);
            }

            items.sort(Comparator.comparingDouble(i -> i.x));
            int nStrings = items.size(); // use the actual count of <g class="s"> groups
            for (int i = 0; i < nStrings; i++) {
                stringColors.put(i + 1, items.get(i).hex);
                stringLabels.put(i + 1, items.get(i).label);
            }

        } catch (Exception e) {
            System.err.println("Error scraping pattern: " + e.getMessage());
        }
        return new ScrapeResult(rows, stringColors, stringLabels);
    }

    private static Map<String, String> parsePalette(Document svgDoc) {
        Map<String, String> out = new HashMap<>();
        for (Element style : svgDoc.select("style")) {
            String css = style.data();
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\\.s1-([a-z])\\s*\\{[^}]*stroke:\\s*(#[0-9a-fA-F]{3,6})"
            );
            java.util.regex.Matcher m = p.matcher(css);
            while (m.find()) {
                String suf = m.group(1);
                String hex = normalizeHex(m.group(2));
                out.put(suf, hex);
            }
        }
        System.out.println("Palette entries: " + out);
        return out;
    }

    private static String normalizeHex(String v) {
        v = v.toLowerCase();
        if (v.length() == 7) return v;
        if (v.length() == 4) { // #rgb
            char r = v.charAt(1), g = v.charAt(2), b = v.charAt(3);
            return ("#" + r + r + g + g + b + b).toLowerCase();
        }
        return v;
    }
}
