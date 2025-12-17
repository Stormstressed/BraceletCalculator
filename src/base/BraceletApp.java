package base;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.util.stream.Collectors;

public class BraceletApp extends Application {

    private ComboBox<String> patternInput;
    private TextFlow patternFlow;
    private TextFlow resultsFlow;
    private Label statusLight;

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
        statusLight = new Label("● idle");
        statusLight.getStyleClass().add("status-light");
        statusLight.getStyleClass().add("idle");

        HBox topBar = new HBox(10, patternInput, loadBtn, statusLight);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Pattern area
        patternFlow = new TextFlow();
        patternFlow.getStyleClass().add("text-flow");
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

        // Stylesheet loading
        addStylesheet(scene, "/css/dark-theme.css");
        addStylesheet(scene, "/src/css/dark-theme.css");
        addStylesheetFallback(scene, "css/dark-theme.css");

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

        // Events
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

        // Knot rows
        for (String[] row : pattern.getKnotRows()) {
            for (int i = 0; i < row.length; i++) {
                String knot = row[i];
                String rawHex = pattern.getColors().getOrDefault(i, "#cccccc");
                String hex = AnsiColor.brightenIfDark(rawHex);
                Text t = new Text(knot + " ");
                t.setFill(showColors ? Color.web(hex) : Color.web("#dddddd"));
                patternFlow.getChildren().add(t);
            }
            patternFlow.getChildren().add(new Text("\n"));
        }

        // Results
        appendLine(resultsFlow, "Strings:", Color.GRAY);
        pattern.getStringLengths().forEach((sid, len) -> {
            String label = pattern.getLabels().getOrDefault(sid, "");
            String rawHex = pattern.getColors().getOrDefault(sid, "#cccccc");
            String hex = AnsiColor.brightenIfDark(rawHex);
            String line = String.format("String %d [%s] %s: %.1f cm", sid, label, hex, len);
            appendLine(resultsFlow, line, showColors ? Color.web(hex) : Color.web("#dddddd"));
        });

        resultsFlow.getChildren().add(new Text("\n"));
        appendLine(resultsFlow, "Totals per color:", Color.GRAY);
        pattern.getColorLengths().forEach((rawHex, total) -> {
            String hex = AnsiColor.brightenIfDark(rawHex);
            String line = String.format("%s: %.1f cm", hex, total);
            appendLine(resultsFlow, line, showColors ? Color.web(hex) : Color.web("#dddddd"));
        });

        resultsFlow.getChildren().add(new Text("\n"));
        appendLine(resultsFlow, "Final order:", Color.GRAY);
        for (int sid : pattern.getFinalOrder()) {
            String label = pattern.getLabels().getOrDefault(sid, "");
            String rawHex = pattern.getColors().getOrDefault(sid, "#cccccc");
            String hex = AnsiColor.brightenIfDark(rawHex);
            String line = String.format("String %d [%s]", sid, label);
            appendLine(resultsFlow, line, showColors ? Color.web(hex) : Color.web("#dddddd"));
        }
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

    // Try classpath first; if not found, do nothing
    private void addStylesheet(Scene scene, String classpathPath) {
        URL url = BraceletApp.class.getResource(classpathPath);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        }
    }

    // Fallback: load from file system to avoid NPE while you adjust resources
    private void addStylesheetFallback(Scene scene, String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            scene.getStylesheets().add(f.toURI().toString());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
