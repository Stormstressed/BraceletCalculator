package base;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BraceletApp extends Application {

	private static final int WINDOW_WIDTH  = 1150;
	private static final int WINDOW_HEIGHT = 800;
	private static final int TOAST_TOP_MARGIN = 45;
	private static final int DEFAULT_KNOT_ROWS = 115;
	private static final int MAX_KNOT_ROWS = 300;

	private Stage stage;
    private ComboBox<String> patternInput;
    private TextFlow patternFlow;
    private TextFlow resultsFlow;
    private Label statusLight;
    private TextField allowanceField;
    private TextField lengthField;
    private TextField knotCountField;
    private Button loadBtn;
    private Button copyPatternBtn;
    private Button copyResultsBtn;
    private Button deleteBtn;

    private boolean suppressComboEvents = false;
    private boolean showColors = true;
    private Pattern currentPattern;

    // keep a master list for filtering
    private List<String> allIds = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        Parent ui = buildUI();
        StackPane stack = new StackPane(ui);  // overlay layer for toast

        Scene scene = new Scene(stack, WINDOW_WIDTH, WINDOW_HEIGHT);

        URL cssUrl = getClass().getResource("/css/dark-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("Bracelet Calculator");
        stage.show();

        wireEvents();
        refreshSavedIds();
    }
	
    private Parent buildUI() {
        // Inputs
        patternInput = new ComboBox<>();
        patternInput.setEditable(true);
        patternInput.setPromptText("Enter pattern ID or URL");

        loadBtn = new Button("Load");
        copyPatternBtn = new Button("Copy Pattern");
        copyResultsBtn = new Button("Copy Results");
        deleteBtn = new Button("Delete");

        allowanceField = new TextField();
        lengthField    = new TextField();
        knotCountField = new TextField();

        allowanceField.setPrefWidth(80);
        lengthField.setPrefWidth(80);
        knotCountField.setPrefWidth(80);
        deleteBtn.setPrefWidth(80);

        Label allowanceLabel = new Label("Extra allowance:");
        Label lengthLabel    = new Label("Length:");
        Label knotCountLabel = new Label("Knot rows:");

        statusLight = new Label("● idle");
        statusLight.getStyleClass().addAll("status-light", "idle");

        HBox topBar = new HBox(
            10,
            patternInput,
            loadBtn,
            copyPatternBtn,
            copyResultsBtn,
            deleteBtn,
            allowanceLabel, allowanceField,
            lengthLabel, lengthField,
            knotCountLabel, knotCountField,
            statusLight
        );
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        patternFlow = new TextFlow();
        patternFlow.getStyleClass().addAll("text-flow", "pattern");

        ScrollPane patternScroll = new ScrollPane(patternFlow);
        patternScroll.setFitToWidth(true);
        patternScroll.setFitToHeight(true);
        patternScroll.getStyleClass().add("flow-scroll");

        resultsFlow = new TextFlow();
        resultsFlow.getStyleClass().add("text-flow");

        ScrollPane resultsScroll = new ScrollPane(resultsFlow);
        resultsScroll.setFitToWidth(true);
        resultsScroll.setFitToHeight(true);
        resultsScroll.getStyleClass().add("flow-scroll");

        HBox flowsBox = new HBox(8, patternScroll, resultsScroll);
        flowsBox.setPadding(new Insets(10));
        HBox.setHgrow(patternScroll, Priority.ALWAYS);
        HBox.setHgrow(resultsScroll, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(flowsBox);

        return root;
    }

	private void wireEvents() {

	    loadBtn.setOnAction(e -> handleLoad());

	    // Enter key in ComboBox editor
	    patternInput.getEditor().setOnKeyPressed(event -> {
	        if (event.getCode() == KeyCode.ENTER) {
	            String typed = patternInput.getEditor().getText().trim();
	            if (!typed.isEmpty()) handleLoad();
	        }
	    });

	    // Selecting from dropdown
	    patternInput.valueProperty().addListener((obs, oldVal, newVal) -> {
	        if (suppressComboEvents) return;

	        if (newVal != null && !newVal.isBlank() && !newVal.equals(oldVal)) {
	            patternInput.getEditor().setText(newVal);
	            handleLoad();
	        }
	    });

	    copyPatternBtn.setOnAction(e -> {
	        if (currentPattern == null) {
	        	showToast("Select a pattern first");
	        	return;
	        }

	        StringBuilder sb = new StringBuilder();
	        for (String[] row : currentPattern.getKnotRows()) {
	            sb.append(String.join(",", row)).append("\n");
	        }

	        ClipboardContent content = new ClipboardContent();
	        content.putString(sb.toString());
	        Clipboard.getSystemClipboard().setContent(content);
	        showToast("Original pattern copied!");
	    });

	    copyResultsBtn.setOnAction(e -> {
	        if (currentPattern == null) {
	        	showToast("Select a pattern first");
	        	return;
	        }

	        StringBuilder sb = new StringBuilder();

	        sb.append("Strings:\n");
	        currentPattern.getStringLengths().forEach((sid, len) -> {
	            String label = currentPattern.getLabels().getOrDefault(sid, "");
	            sb.append("String ").append(sid)
	              .append(" [").append(label).append("]: ")
	              .append(String.format("%.1f cm", len))
	              .append("\n");
	        });

	        sb.append("\nColors:\n");
	        currentPattern.getColorLengths().forEach((hex, len) -> {
	            sb.append(hex).append(": ")
	              .append(String.format("%.1f cm", len))
	              .append("\n");
	        });

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
	        showToast("Strings length copied!");
	    });
	    
	    deleteBtn.setOnAction(e -> {
	        String id = patternInput.getEditor().getText().trim();
	        if (id.isEmpty()) {
	        	showToast("Select a pattern first");
	            return;
	        }

	        try {
	            PatternStorage.deletePattern(id);
	        } catch (IOException ex) {
	            showToast("Could not delete pattern " + id);
	            return;
	        }

	        refreshSavedIds();
	        resetUI();

	        showToast("Deleted pattern " + id);
	    });

	    allowanceField.focusedProperty().addListener((obs, was, is) -> {
	        if (!is && currentPattern != null) {
	            try {
	                double allowance = Double.parseDouble(allowanceField.getText());
	                currentPattern.setAllowance(allowance);
	                PatternAnalyzer.analyze(currentPattern);
	                displayPattern(currentPattern);
	            } catch (NumberFormatException ignored) {}
	        }
	    });

	    lengthField.focusedProperty().addListener((obs, was, is) -> {
	        if (!is && currentPattern != null) {
	            try {
	                double len = Double.parseDouble(lengthField.getText());
	                currentPattern.setDesiredBraceletLength(len);
	                PatternAnalyzer.analyze(currentPattern);
	                displayPattern(currentPattern);
	            } catch (NumberFormatException ignored) {}
	        }
	    });
	    
	    knotCountField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
	        if (!isFocused) applyKnotCount();
	    });
	    
	    knotCountField.setOnKeyPressed(event -> {
	        if (event.getCode() == KeyCode.ENTER) applyKnotCount();
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

            knotCountField.setText(String.valueOf(DEFAULT_KNOT_ROWS));
            
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
        
        int targetRows;
        try {
            targetRows = Integer.parseInt(knotCountField.getText());
            if (targetRows < 1) targetRows = 1;
            if (targetRows > MAX_KNOT_ROWS) targetRows = MAX_KNOT_ROWS;
        } catch (NumberFormatException e) {
            targetRows = 100;
        }

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
    
    private void applyKnotCount() {
        if (currentPattern == null) return;

        int count;
        try {
            count = Integer.parseInt(knotCountField.getText());
        } catch (NumberFormatException e) {
            count = DEFAULT_KNOT_ROWS;
        }

        if (count < 1) count = 1;
        if (count > MAX_KNOT_ROWS) count = MAX_KNOT_ROWS;

        knotCountField.setText(String.valueOf(count));
        displayPattern(currentPattern);
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
        String typed = patternInput.getEditor().getText();

        suppressComboEvents = true;
        try {
            allIds = new ArrayList<>(PatternStorage.getSavedIds());

            allIds.sort((a, b) -> {
                try {
                    return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                } catch (NumberFormatException e) {
                    return a.compareToIgnoreCase(b);
                }
            });

            patternInput.getItems().setAll(allIds);

            patternInput.getEditor().setText(typed);
            patternInput.setValue(typed);

        } finally {
            suppressComboEvents = false;
        }
    }
    
    private void showToast(String message) {
        Label toast = new Label(message);
        toast.getStyleClass().add("toast");

        StackPane root = (StackPane) stage.getScene().getRoot();
        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(TOAST_TOP_MARGIN, 0, 0, 0));
        root.getChildren().add(toast);

        FadeTransition fade = new FadeTransition(Duration.seconds(2), toast);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(1));
        fade.setOnFinished(ev -> root.getChildren().remove(toast));
        fade.play();
    }

    private void resetUI() {
        currentPattern = null;

        // Clear TextFlows
        patternFlow.getChildren().clear();
        resultsFlow.getChildren().clear();

        // Clear all text fields
        patternInput.getEditor().clear();
        allowanceField.clear();
        lengthField.clear();
        knotCountField.clear();

        // Reset status
        setStatus("idle");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setGraphic(null);
        // Apply dark theme to the alert
        DialogPane pane = alert.getDialogPane();
        URL cssUrl = getClass().getResource("/css/dark-theme.css");
        if (cssUrl != null) {
            pane.getStylesheets().add(cssUrl.toExternalForm());
            pane.getStyleClass().add("dark-alert");
        }

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
