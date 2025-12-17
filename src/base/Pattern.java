package base;

import java.util.List;
import java.util.Map;

public class Pattern {
    private final String id;
    private final Map<Integer, String> colors;
    private final Map<Integer, String> labels;
    private final List<String[]> knotRows;

    // Computed fields (initially null/empty, filled by analyzer)
    private Map<Integer, Integer> tally;
    private int repeats;
    private int totalRows;
    private List<Integer> finalOrder;
    private Map<Integer, Double> stringLengths;
    private Map<String, Double> colorLengths;
    private double desiredBraceletLength;
    private double allowance;
    private boolean valid;

    // Nested enum for knots
    public enum Knot { F, B, FB, BF, UNKNOWN }

    // Constructor for raw scraped data
    public Pattern(String id,
                   Map<Integer, String> colors,
                   Map<Integer, String> labels,
                   List<String[]> knotRows,
                   double desiredBraceletLength,
                   double allowance) {
        this.id = id;
        this.colors = colors;
        this.labels = labels;
        this.knotRows = knotRows;
        this.desiredBraceletLength = desiredBraceletLength;
        this.allowance = allowance;
    }

    // --- Getters ---
    public String getId() { return id; }
    public Map<Integer, String> getColors() { return colors; }
    public Map<Integer, String> getLabels() { return labels; }
    public List<String[]> getKnotRows() { return knotRows; }
    public Map<Integer, Integer> getTally() { return tally; }
    public int getRepeats() { return repeats; }
    public int getTotalRows() { return totalRows; }
    public List<Integer> getFinalOrder() { return finalOrder; }
    public Map<Integer, Double> getStringLengths() { return stringLengths; }
    public Map<String, Double> getColorLengths() { return colorLengths; }
    public double getDesiredBraceletLength() { return desiredBraceletLength; }
    public double getAllowance() { return allowance; }
    public boolean isValid() { return valid; }

    // --- Setters for computed fields ---
    public void setTally(Map<Integer, Integer> tally) { this.tally = tally; }
    public void setRepeats(int repeats) { this.repeats = repeats; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public void setFinalOrder(List<Integer> finalOrder) { this.finalOrder = finalOrder; }
    public void setStringLengths(Map<Integer, Double> stringLengths) { this.stringLengths = stringLengths; }
    public void setColorLengths(Map<String, Double> colorLengths) { this.colorLengths = colorLengths; }
    public void setValid(boolean valid) { this.valid = valid; }

    @Override
    public String toString() {
        return "Pattern{id=" + id +
               ", rows=" + knotRows.size() +
               ", valid=" + valid +
               ", repeats=" + repeats + "}";
    }
}
