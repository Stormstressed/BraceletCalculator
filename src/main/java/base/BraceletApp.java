package base;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BraceletApp extends Application {

    private ComboBox<String> patternInput;
    private TextFlow patternFlow;
    private TextFlow resultsFlow;
    private Label statusLight;
    private TextField allowanceField;
    private TextField lengthField;

    private boolean showColors = true;
    private Pattern currentPattern;

    // keep a master list for filtering
    private List<String> allIds = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        // Inputs
        patternInput = new ComboBox<>();
        patternInput.setEditable(true);
        patternInput.setPromptText("Enter pattern ID or URL");

        Button loadBtn = new Button("Load");
        Button copyPatternBtn = new Button("Copy Pattern");
        Button copyResultsBtn = new Button("Copy Results");
        
        // TextFields for allowance and desired length
        allowanceField = new TextField();
        lengthField    = new TextField();

        allowanceField.setPrefWidth(80);
        lengthField.setPrefWidth(80);

        // Labels for clarity
        Label allowanceLabel = new Label("Extra allowance:");
        Label lengthLabel    = new Label("Length:");
        
        statusLight = new Label("● idle");
        statusLight.getStyleClass().add("status-light");
        statusLight.getStyleClass().add("idle");

        HBox topBar = new HBox(10, patternInput, loadBtn, copyPatternBtn, copyResultsBtn,allowanceLabel,allowanceField, lengthLabel, lengthField, statusLight);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Pattern area
        patternFlow = new TextFlow();
        patternFlow.getStyleClass().add("text-flow");
        patternFlow.getStyleClass().add("pattern");
        ScrollPane patternScroll = new ScrollPane(patternFlow);
        patternScroll.setFitToWidth(true);
        patternScroll.setFitToHeight(true);
        patternScroll.getStyleClass().add("flow-scroll");

        // Results area
        resultsFlow = new TextFlow();
        resultsFlow.getStyleClass().add("text-flow");
        ScrollPane resultsScroll = new ScrollPane(resultsFlow);
        resultsScroll.setFitToWidth(true);
        resultsScroll.setFitToHeight(true);
        resultsScroll.getStyleClass().add("flow-scroll");

        Button toggleColors = new Button("Toggle Colors");

        // stack vertically
        HBox flowsBox = new HBox(8, patternScroll, resultsScroll);
        flowsBox.setPadding(new Insets(10));
        HBox.setHgrow(patternScroll, Priority.ALWAYS);
        HBox.setHgrow(resultsScroll, Priority.ALWAYS);



        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(flowsBox);

        Scene scene = new Scene(root, 900, 650);

        URL cssUrl = getClass().getResource("/css/dark-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("dark-theme.css not found on classpath");
        }
        stage.setScene(scene);
        stage.setTitle("Bracelet Calculator");
        stage.show();

        refreshSavedIds();

        // ================ Events ================
        loadBtn.setOnAction(e -> handleLoad());
        toggleColors.setOnAction(e -> {
            showColors = !showColors;
            if (currentPattern != null) displayPattern(currentPattern);
        });

        // Enter in editor
        patternInput.getEditor().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String typed = patternInput.getEditor().getText().trim();
                if (!typed.isEmpty()) {
                    handleLoad();
                }
            }
        });

        // Click selection
        patternInput.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                patternInput.getEditor().setText(newVal);
                handleLoad();
            }
        });
        
        copyPatternBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();

            for (String[] row : currentPattern.getKnotRows()) {
                sb.append(String.join(",", row)).append("\n");
            }

            ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });

        copyResultsBtn.setOnAction(e -> {
            StringBuilder sb = new StringBuilder();

            // Per‑string lengths
            sb.append("Strings:\n");
            currentPattern.getStringLengths().forEach((sid, len) -> {
                String label = currentPattern.getLabels().getOrDefault(sid, "");
                sb.append("String ").append(sid)
                  .append(" [").append(label).append("]: ")
                  .append(String.format("%.1f cm", len))
                  .append("\n");
            });

            // Per‑color totals
            sb.append("\nColors:\n");
            currentPattern.getColorLengths().forEach((hex, len) -> {
                sb.append(hex).append(": ")
                  .append(String.format("%.1f cm", len))
                  .append("\n");
            });

            // Final order after one loop
            sb.append("\nFinal order after one loop:\n");
            List<Integer> order = currentPattern.getFinalOrder();
            for (int id : order) {
                String label = currentPattern.getLabels().getOrDefault(id, "");
                sb.append(id).append("[").append(label).append("] ");
            }
            sb.append("\n");

            ClipboardContent content = new ClipboardContent();
            content.putString(sb.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });
        
        allowanceField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && currentPattern != null) {
                try {
                    double allowance = Double.parseDouble(allowanceField.getText());
                    currentPattern.setAllowance(allowance);
                    PatternAnalyzer.analyze(currentPattern);
                    displayPattern(currentPattern);
                } catch (NumberFormatException ignored) { }
            }
        });

        lengthField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && currentPattern != null) {
                try {
                    double len = Double.parseDouble(lengthField.getText());
                    currentPattern.setDesiredBraceletLength(len);
                    PatternAnalyzer.analyze(currentPattern);
                    displayPattern(currentPattern);
                } catch (NumberFormatException ignored) { }
            }
        });

    }

    private void handleLoad() {
        String idOrUrl = patternInput.getEditor().getText().trim();
        if (idOrUrl.isEmpty()) {
            showError("Please enter a pattern ID or URL.");
            return;
        }

        setStatus("loading");

        Task<Pattern> task = new Task<>() {
            @Override
            protected Pattern call() throws Exception {
                Pattern p = PatternStorage.loadPattern(idOrUrl);
                if (p == null) {
                    p = Scraper.scrapePattern(idOrUrl, 15.0, 30.0);
                    PatternAnalyzer.analyze(p);
                    PatternStorage.savePattern(p, idOrUrl);
                }
                return p;
            }
        };

        task.setOnSucceeded(ev -> {
            currentPattern = task.getValue();
            displayPattern(currentPattern);
            setStatus("ok");
            refreshSavedIds();

            // also refresh the text fields with the pattern values
            allowanceField.setText(String.valueOf(currentPattern.getAllowance()));
            lengthField.setText(String.valueOf(currentPattern.getDesiredBraceletLength()));
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            setStatus("error");
            showError("Load failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        new Thread(task, "load-pattern").start();
    }


    private void displayPattern(Pattern pattern) {
    	patternFlow.getChildren().clear();
        resultsFlow.getChildren().clear();

        List<List<String[]>> knotLabelRows = pattern.getKnotLabelRows();
        int targetRows = 100;

        for (int rowIndex = 0; rowIndex < targetRows; rowIndex++) {
            List<String[]> row = knotLabelRows.get(rowIndex % knotLabelRows.size());

            for (String[] cell : row) {
                String knotType = cell[0];
                String label    = cell[1];

                // lookup color by label → string id → hex
                int sid = pattern.getLabels().entrySet().stream()
                        .filter(e -> Objects.equals(e.getValue(), label))
                        .map(Map.Entry::getKey)
                        .findFirst().orElse(-1);

                String rawHex = pattern.getColors().getOrDefault(sid, "#cccccc");
                String hex = AnsiColor.brightenIfDark(rawHex);

                // pad single-letter knot codes
                String display = (knotType.length() == 1) ? knotType + " " : knotType;

                // colored Text for display
                Text t = new Text(display + " ");
                t.setFill(showColors ? Color.web(hex) : Color.web("#dddddd"));
                patternFlow.getChildren().add(t);

            }

            patternFlow.getChildren().add(new Text("\n"));
        }


        // Results
        appendTitle(resultsFlow, "Strings:");
        pattern.getStringLengths().forEach((sid, len) -> {
            String label = pattern.getLabels().getOrDefault(sid, "");
            String rawHex = pattern.getColors().getOrDefault(sid, "#cccccc");
            String hex = AnsiColor.brightenIfDark(rawHex);
            String line = String.format("String %d [%s] %s: %.1f cm", sid, label, hex, len);
            appendLine(resultsFlow, line, showColors ? Color.web(hex) : Color.web("#dddddd"));
        });

        resultsFlow.getChildren().add(new Text("\n"));
        appendTitle(resultsFlow, "Totals per color:");
        pattern.getColorLengths().forEach((rawHex, total) -> {
            String hex = AnsiColor.brightenIfDark(rawHex);
            String line = String.format("%s: %.1f cm", hex, total);
            appendLine(resultsFlow, line, showColors ? Color.web(hex) : Color.web("#dddddd"));
        });

        resultsFlow.getChildren().add(new Text("\n"));
        appendTitle(resultsFlow, "Final order:");
        for (int sid : pattern.getFinalOrder()) {
            String label = pattern.getLabels().getOrDefault(sid, "");
            String rawHex = pattern.getColors().getOrDefault(sid, "#cccccc");
            String hex = AnsiColor.brightenIfDark(rawHex);
            String line = String.format("String %d [%s]", sid, label);
            appendLine(resultsFlow, line, showColors ? Color.web(hex) : Color.web("#dddddd"));
        }
    }
    
    private void appendTitle(TextFlow flow, String text) {
        Text t = new Text(text + "\n");
        t.getStyleClass().add("results-title");
        flow.getChildren().add(t);
    }


    private void appendLine(TextFlow flow, String text, Color color) {
        Text t = new Text(text + "\n");
        t.setFill(color);
        flow.getChildren().add(t);
    }

    private void setStatus(String state) {
        statusLight.getStyleClass().removeAll("loading", "ok", "error", "idle");
        statusLight.setStyle(null);

        switch (state) {
            case "loading":
                statusLight.setText("● loading");
                statusLight.getStyleClass().add("loading");
                break;
            case "ok":
                statusLight.setText("● loaded");
                statusLight.getStyleClass().add("ok");
                break;
            case "error":
                statusLight.setText("● error");
                statusLight.getStyleClass().add("error");
                break;
            default:
                statusLight.setText("● idle");
                statusLight.getStyleClass().add("idle");
                break;
        }
    }

    private void refreshSavedIds() {
        allIds = new ArrayList<>(PatternStorage.getSavedIds());
        // sort numerically, smallest to biggest
        allIds.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                // fallback: non-numeric entries sorted alphabetically
                return a.compareToIgnoreCase(b);
            }
        });
        patternInput.getItems().setAll(allIds);
    }


    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
