package base;

import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Scraper {

    public static Pattern scrapePattern(String idOrUrl,
                                        double desiredLength,
                                        double allowance) {
        try {
            String url = idOrUrl.matches("\\d+")
                    ? "https://www.braceletbook.com/patterns/normal/" + idOrUrl + "/"
                    : idOrUrl;

            Document pageDoc = Jsoup.connect(url).get();
            Document svgDoc = fetchSvg(pageDoc);

            List<String[]> rows = parseRows(svgDoc);
            Map<Integer, String> colors = parseColors(svgDoc);
            Map<Integer, String> labels = parseLabels(svgDoc);

            // Build Pattern with raw data only
            return new Pattern(url, colors, labels, rows, desiredLength, allowance);

        } catch (Exception e) {
            throw new RuntimeException("Error scraping pattern: " + e.getMessage(), e);
        }
    }

    private static Document fetchSvg(Document pageDoc) throws IOException {
        Element object = pageDoc.selectFirst("object.pattern_svg");
        if (object == null) throw new IllegalStateException("No SVG object found");
        return Jsoup.connect(object.attr("data")).get();
    }

    private static List<String[]> parseRows(Document svgDoc) {
        Map<Integer, List<String>> rowMap = new TreeMap<>();
        for (Element g : svgDoc.select("g.k")) {
            Element ellipse = g.selectFirst("ellipse");
            if (ellipse == null) continue;
            int cy = Integer.parseInt(ellipse.attr("cy"));

            Element use = g.selectFirst("use");
            if (use == null) continue;
            String href = use.attr("xlink:href");

            String knot = switch (href) {
                case "#kf" -> "f";
                case "#kb" -> "b";
                case "#kfb" -> "fb";
                case "#kbf" -> "bf";
                default -> "?";
            };

            rowMap.putIfAbsent(cy, new ArrayList<>());
            rowMap.get(cy).add(knot);
        }
        List<String[]> rows = new ArrayList<>();
        for (List<String> row : rowMap.values()) {
            rows.add(row.toArray(new String[0]));
        }
        return rows;
    }

    private static Map<Integer, String> parseColors(Document svgDoc) {
        Map<String, String> palette = parsePalette(svgDoc);
        Map<Integer, String> colors = new HashMap<>();

        Elements stringGroups = svgDoc.select("g.s");
        List<String> hexList = new ArrayList<>();

        for (Element g : stringGroups) {
            String hex = "#000000";
            Element use = g.selectFirst("use.s1");
            if (use != null) {
                for (String cls : use.classNames()) {
                    if (cls.startsWith("s1-") && cls.length() > 3) {
                        String suf = cls.substring(3);
                        hex = palette.getOrDefault(suf, "#000000");
                        break;
                    }
                }
            }
            hexList.add(hex);
        }

        for (int i = 0; i < hexList.size(); i++) {
            colors.put(i + 1, hexList.get(i));
        }
        return colors;
    }

    private static Map<Integer, String> parseLabels(Document svgDoc) {
        Map<Integer, String> labels = new HashMap<>();
        Elements stringGroups = svgDoc.select("g.s");
        List<String> labelList = new ArrayList<>();

        for (Element g : stringGroups) {
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
            labelList.add(label);
        }

        for (int i = 0; i < labelList.size(); i++) {
            labels.put(i + 1, labelList.get(i));
        }
        return labels;
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
