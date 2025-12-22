package base;

import java.io.IOException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {

    private static final String NORMAL_BASE = "https://www.braceletbook.com/patterns/normal/";
    private static final String ALPHA_BASE  = "https://www.braceletbook.com/patterns/alpha/";

    public static Pattern scrapePattern(String idOrUrl,
                                        double desiredLength,
                                        double allowance) {
        try {
            String id;
            String normalUrl;

            Debug.log("Scrape start: " + idOrUrl);

            /*
			* +------------------------------------------------------------------------------------
			* |          INPUT SANITATION
			* +------------------------------------------------------------------------------------
			*/

            if (idOrUrl.matches("\\d+")) {
                id = idOrUrl;
                normalUrl = NORMAL_BASE + id + "/";
            } else {
                if (!idOrUrl.matches("^https://www\\.braceletbook\\.com/patterns/normal/\\d+/+$")) {
                    throw new IllegalArgumentException("Invalid URL format. Only normal pattern URLs are accepted.");
                }
                id = idOrUrl.replaceAll("\\D+", "");
                normalUrl = idOrUrl;
            }

            Debug.log("Pattern ID = " + id);
            Debug.log("Normal URL = " + normalUrl);

            /*
			* +------------------------------------------------------------------------------------
			* |          FETCH NORMAL PAGE
			* +------------------------------------------------------------------------------------
			*/

            Document pageDoc;
            try {
                pageDoc = Jsoup.connect(normalUrl).get();
            } catch (org.jsoup.HttpStatusException e) {
                if (e.getStatusCode() != 404) {
                    throw new RuntimeException("HTTP error fetching normal pattern: " + e.getMessage(), e);
                }
                pageDoc = null;
            }

            /*
			* +------------------------------------------------------------------------------------
			* |          FALLBACK TO ALPHA
			* +------------------------------------------------------------------------------------
			*/

            if (pageDoc == null) {
                Debug.log("Normal pattern not found, checking alpha…");
                String alphaUrl = ALPHA_BASE + id + "/";
                try {
                    Jsoup.connect(alphaUrl).get();
                    throw new IllegalArgumentException("Alpha patterns are not supported");
                } catch (org.jsoup.HttpStatusException e) {
                    if (e.getStatusCode() == 404) {
                        throw new IllegalArgumentException("Pattern not found");
                    }
                    throw new RuntimeException("HTTP error fetching alpha pattern: " + e.getMessage(), e);
                }
            }

            /*
			* +------------------------------------------------------------------------------------
			* |          SCRAPE SVG
			* +------------------------------------------------------------------------------------
			*/

            Document svgDoc = fetchSvg(pageDoc);
            Debug.log("SVG fetched");

            // 1. Knot rows + knot labels
            List<Pattern.KnotType[]> knotRows = parseKnotRows(svgDoc);
            List<String[]> knotLabels = parseKnotLabels(svgDoc);
            Debug.log("Parsed knot rows: " + knotRows.size());

            // 2. String labels + string colors
            Map<Integer, String> labels = parseLabels(svgDoc);
            Map<Integer, String> colors = parseColors(svgDoc);

            Debug.log("Parsed labels: " + labels);
            Debug.log("Parsed colors: " + colors);

            //BUILD label > color map
            Map<String, String> labelToColor = new LinkedHashMap<>();
            for (int i = 1; i <= labels.size(); i++) {
                labelToColor.put(labels.get(i), colors.get(i));
            }
            Debug.log("Label→Color map: " + labelToColor);

            
            //BUILD per‑string list
            
            List<Pattern.StringInfo> strings = new ArrayList<>();
            for (int i = 1; i <= labels.size(); i++) {
                strings.add(new Pattern.StringInfo(
                    i,
                    labels.get(i),
                    0,
                    0.0
                ));
            }
            Debug.log("String list size: " + strings.size());

            //BUILD KnotCell rows
            List<List<Pattern.KnotCell>> rows = new ArrayList<>();
            for (int r = 0; r < knotRows.size(); r++) {
                Pattern.KnotType[] types = knotRows.get(r);
                String[] labelsRow = knotLabels.get(r);

                List<Pattern.KnotCell> list = new ArrayList<>();
                for (int i = 0; i < types.length; i++) {
                    list.add(new Pattern.KnotCell(types[i], labelsRow[i]));
                }
                rows.add(list);
            }
            Debug.log("Built KnotCell rows: " + rows.size());

            //CONSTRUCT PATTERN
            Debug.log("Scrape complete.");
            return new Pattern(id, normalUrl, labelToColor, strings, rows, desiredLength, allowance);

        } catch (Exception e) {
            Debug.log("Scrape error: " + e.getMessage());
            throw new RuntimeException("Error scraping pattern: " + e.getMessage(), e);
        }
    }

    private static Document fetchSvg(Document pageDoc) throws IOException {
        Element object = pageDoc.selectFirst("object.pattern_svg");
        if (object == null) throw new IllegalStateException("No SVG object found");
        Debug.log("SVG URL = " + object.attr("data"));
        return Jsoup.connect(object.attr("data")).get();
    }

    //PARSE KNOT TYPES
    private static List<Pattern.KnotType[]> parseKnotRows(Document svgDoc) {
        Map<Integer, List<Pattern.KnotType>> rowMap = new TreeMap<>();

        Elements groups = svgDoc.select("g.k");
        Debug.log("Found g.k groups: " + groups.size());

        for (Element g : groups) {
            Element shape = g.selectFirst("ellipse");
            if (shape == null) continue;

            int cy = Integer.parseInt(shape.attr("cy"));

            Pattern.KnotType knot;
            Element use = g.selectFirst("use");

            if (use != null) {
                String href = use.attr("xlink:href");
                knot = switch (href) {
                    case "#kf"  -> Pattern.KnotType.F;
                    case "#kb"  -> Pattern.KnotType.B;
                    case "#kfb" -> Pattern.KnotType.FB;
                    case "#kbf" -> Pattern.KnotType.BF;
                    default     -> Pattern.KnotType.UNKNOWN;
                };
            } else {
                knot = Pattern.KnotType.BLANK;
            }

            rowMap.computeIfAbsent(cy, k -> new ArrayList<>()).add(knot);
        }

        List<Pattern.KnotType[]> rows = new ArrayList<>();
        for (List<Pattern.KnotType> row : rowMap.values()) {
            rows.add(row.toArray(new Pattern.KnotType[0]));
        }

        Debug.log("parseKnotRows: built " + rows.size() + " rows");
        return rows;
    }

    //PARSE KNOT LABELS (kk-X)
    private static List<String[]> parseKnotLabels(Document svgDoc) {
        Map<Integer, List<String>> rowMap = new TreeMap<>();

        Elements groups = svgDoc.select("g.k");

        for (Element g : groups) {
            Element shape = g.selectFirst("ellipse");
            if (shape == null) continue;

            int cy = Integer.parseInt(shape.attr("cy"));

            String knotLabel = "0"; // default for blank

            for (String cls : shape.classNames()) {
                if (cls.startsWith("kk-") && cls.length() > 3) {
                    knotLabel = cls.substring(3).toUpperCase(Locale.ROOT); // <-- FIX HERE
                    break;
                }
            }

            rowMap.computeIfAbsent(cy, k -> new ArrayList<>()).add(knotLabel);
        }

        List<String[]> rows = new ArrayList<>();
        for (List<String> row : rowMap.values()) {
            rows.add(row.toArray(new String[0]));
        }

        return rows;
    }

    //PARSE STRING COLORS
    private static Map<Integer, String> parseColors(Document svgDoc) {
        Map<String, String> palette = parsePalette(svgDoc);
        Map<Integer, String> colors = new HashMap<>();

        Elements stringGroups = svgDoc.select("g.s");
        Debug.log("Found g.s groups: " + stringGroups.size());

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

        Debug.log("parseColors: " + colors);
        return colors;
    }

    //PARSE STRING LABELS (A, B, C…)
    private static Map<Integer, String> parseLabels(Document svgDoc) {
        Map<Integer, String> labels = new HashMap<>();
        Elements stringGroups = svgDoc.select("g.s");

        Debug.log("parseLabels: g.s count = " + stringGroups.size());

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

        Debug.log("parseLabels: " + labels);
        return labels;
    }

    //PARSE PALETTE (s1-a → #hex)
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

        Debug.log("parsePalette: " + out);
        return out;
    }

    private static String normalizeHex(String v) {
        v = v.toLowerCase();
        if (v.length() == 7) return v;
        if (v.length() == 4) {
            char r = v.charAt(1), g = v.charAt(2), b = v.charAt(3);
            return ("#" + r + r + g + g + b + b);
        }
        return v;
    }
}
