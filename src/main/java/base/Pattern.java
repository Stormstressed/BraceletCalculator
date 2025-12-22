package base;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pattern {

    private final String id;
    private final String url;
    private double desiredBraceletLength;
    private double allowance;
    private List<StringInfo> strings;
    private Map<String, String> labelToColor;
    private List<List<KnotCell>> rows;

    // Computed fields
    private Map<Integer, Integer> tally;
    private Map<Integer, Double> stringLengths;
    private Map<String, Double> colorLengths;
    private List<Integer> finalOrder;             // after one loop
    private int repeats;
    private int totalRows;
    private boolean valid;

    // Records
    public record StringInfo( int id, String label) {}

    public record KnotCell(KnotType knot, String label) {}

    public enum KnotType {F, B, FB, BF, BLANK, UNKNOWN}
    
    //pre analysis
	public Pattern(String id,
                   String url,
                   Map<String, String> labelToColor,
                   List<StringInfo> strings,
                   List<List<KnotCell>> rows,
                   double desiredLength,
                   double allowance) {

        this.id = id;
        this.url = url;
        this.labelToColor = labelToColor;
        this.strings = strings;
        this.rows = rows;

        this.desiredBraceletLength = desiredLength;
        this.allowance = allowance;

        this.tally = new LinkedHashMap<>();
        this.stringLengths = new LinkedHashMap<>();
        this.colorLengths = new LinkedHashMap<>();
        this.finalOrder = new ArrayList<>();
        this.valid = false;
    }
	
	@JsonCreator
	public Pattern(
	        @JsonProperty("id") String id,
	        @JsonProperty("url") String url,
	        @JsonProperty("desiredBraceletLength") double desiredBraceletLength,
	        @JsonProperty("allowance") double allowance,
	        @JsonProperty("strings") List<StringInfo> strings,
	        @JsonProperty("labelToColor") Map<String, String> labelToColor,
	        @JsonProperty("rows") List<List<KnotCell>> rows,
	        @JsonProperty("tally") Map<Integer, Integer> tally,
	        @JsonProperty("stringLengths") Map<Integer, Double> stringLengths,
	        @JsonProperty("colorLengths") Map<String, Double> colorLengths,
	        @JsonProperty("finalOrder") List<Integer> finalOrder,
	        @JsonProperty("repeats") int repeats,
	        @JsonProperty("totalRows") int totalRows,
	        @JsonProperty("valid") boolean valid
	) {
	    this.id = id;
	    this.url = url;
	    this.desiredBraceletLength = desiredBraceletLength;
	    this.allowance = allowance;
	    this.strings = strings;
	    this.labelToColor = labelToColor;
	    this.rows = rows;
	    this.tally = tally;
	    this.stringLengths = stringLengths;
	    this.colorLengths = colorLengths;
	    this.finalOrder = finalOrder;
	    this.repeats = repeats;
	    this.totalRows = totalRows;
	    this.valid = valid;
	}

	// Getters
    public String getId() { return id; }
    public String getUrl() { return url; }
    public List<StringInfo> getStrings() { return strings; }
    public Map<String, String> getLabelToColor() { return labelToColor; }
    public List<List<KnotCell>> getRows() { return rows; }
    public Map<Integer, Integer> getTally() { return tally; }
    public Map<Integer, Double> getStringLengths() { return stringLengths; }
    public Map<String, Double> getColorLengths() { return colorLengths; }
    public List<Integer> getFinalOrder() { return finalOrder; }
    public int getRepeats() { return repeats; }
    public int getTotalRows() { return totalRows; }
    public boolean isValid() { return valid; }
    public double getDesiredBraceletLength() { return desiredBraceletLength; }
    public double getAllowance() { return allowance; }

    // Setters for computed fields
    public void setTally(Map<Integer, Integer> tally) { this.tally = tally; }
    public void setStringLengths(Map<Integer, Double> stringLengths) { this.stringLengths = stringLengths; }
    public void setColorLengths(Map<String, Double> colorLengths) { this.colorLengths = colorLengths; }
    public void setFinalOrder(List<Integer> finalOrder) { this.finalOrder = finalOrder; }
    public void setRepeats(int repeats) { this.repeats = repeats; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public void setValid(boolean valid) { this.valid = valid; }

    // User input setters
    public void setDesiredBraceletLength(double len) { this.desiredBraceletLength = len; }
    public void setAllowance(double allowance) { this.allowance = allowance; }
}
