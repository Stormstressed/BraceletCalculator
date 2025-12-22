package base;

import java.util.*;

public class Pattern {

    // Raw inputs
    private String id; 
    private String url;
    private Map<Integer, String> colors = new HashMap<>();
    private Map<Integer, String> labels = new HashMap<>();
    private List<String[]> knotRows = new ArrayList<>();
    private List<List<String[]>> knotLabelRows = new ArrayList<>();

    // Parameters
    private double desiredBraceletLength;
    private double allowance;

    // Computed outputs
    private Map<Integer, Integer> tally = new HashMap<>();
    private int repeats;
    private int totalRows;
    private List<Integer> finalOrder = new ArrayList<>();
    private Map<Integer, Double> stringLengths = new HashMap<>();
    private Map<String, Double> colorLengths = new HashMap<>();
    private boolean valid;

    public enum Knot { F, B, FB, BF, UNKNOWN }

    // --- Constructors ---
    public Pattern() { }

    public Pattern(String id,
    			   String url,
                   Map<Integer, String> colors,
                   Map<Integer, String> labels,
                   List<String[]> knotRows,
                   double desiredBraceletLength,
                   double allowance) {
        this.id = id;
        this.url = url;
        this.colors = colors;
        this.labels = labels;
        this.knotRows = knotRows;
        this.desiredBraceletLength = desiredBraceletLength;
        this.allowance = allowance;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getUrl() { return url; }
    public Map<Integer, String> getColors() { return colors; }
    public Map<Integer, String> getLabels() { return labels; }
    public List<String[]> getKnotRows() { return knotRows; }
    public List<List<String[]>> getKnotLabelRows() { return knotLabelRows; }
    public double getDesiredBraceletLength() { return desiredBraceletLength; }
    public double getAllowance() { return allowance; }
    public Map<Integer, Integer> getTally() { return tally; }
    public int getRepeats() { return repeats; }
    public int getTotalRows() { return totalRows; }
    public List<Integer> getFinalOrder() { return finalOrder; }
    public Map<Integer, Double> getStringLengths() { return stringLengths; }
    public Map<String, Double> getColorLengths() { return colorLengths; }
    public boolean isValid() { return valid; }

    // --- Setters (only for inputs/parameters) ---
    public void setId(String id) { this.id = id; }
    public void setUrl(String url) { this.url = url; }
    public void setColors(Map<Integer, String> colors) { this.colors = colors; }
    public void setLabels(Map<Integer, String> labels) { this.labels = labels; }
    public void setKnotRows(List<String[]> knotRows) { this.knotRows = knotRows; }
    public void setKnotLabelRows(List<List<String[]>> knotLabelRows) { this.knotLabelRows = knotLabelRows; }
    public void setDesiredBraceletLength(double desiredBraceletLength) { this.desiredBraceletLength = desiredBraceletLength; }
    public void setAllowance(double allowance) { this.allowance = allowance; }

    // Computed fields should be set only by the analyzer
    void setTally(Map<Integer, Integer> tally) { this.tally = tally; }
    void setRepeats(int repeats) { this.repeats = repeats; }
    void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    void setFinalOrder(List<Integer> finalOrder) { this.finalOrder = finalOrder; }
    void setStringLengths(Map<Integer, Double> stringLengths) { this.stringLengths = stringLengths; }
    void setColorLengths(Map<String, Double> colorLengths) { this.colorLengths = colorLengths; }
    void setValid(boolean valid) { this.valid = valid; }

    @Override
    public String toString() {
        return String.format("Pattern{id=%s, rows=%d, valid=%s, repeats=%d}",
                id, knotRows != null ? knotRows.size() : 0, valid, repeats);
    }
}
