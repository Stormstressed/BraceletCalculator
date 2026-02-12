package base;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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

public class BraceletApp extends Application {

	private static final int WINDOW_WIDTH  = 1200;
	private static final int WINDOW_HEIGHT = 800;
	private static final int TOAST_TOP_MARGIN = 45;
	private static final int DEFAULT_KNOT_ROWS = 115;
	private static final int MAX_KNOT_ROWS = 300;
    private static final Color DEFAULT_COLOR = Color.web("#dddddd");
    
    private static final Map<Pattern.KnotType, String> TEXT_SYMBOLS = Map.of(
            Pattern.KnotType.F,      "F ",
            Pattern.KnotType.B,      "B ",
            Pattern.KnotType.FB,     "FB",
            Pattern.KnotType.BF,     "BF",
            Pattern.KnotType.BLANK,  "··"
    );

    private static final Map<Pattern.KnotType, String> DIAMOND_SYMBOLS = Map.of(
            Pattern.KnotType.F,      "◆",
            Pattern.KnotType.B,      "◆",
            Pattern.KnotType.FB,     "◆",
            Pattern.KnotType.BF,     "◆",
            Pattern.KnotType.BLANK,  "◇"
    );

    private static final Map<Pattern.KnotType, String> ARROW_SYMBOLS = Map.of(
            Pattern.KnotType.F,      "↘",
            Pattern.KnotType.B,      "↙",
            Pattern.KnotType.FB,     "⤸",
            Pattern.KnotType.BF,     "⤹",
            Pattern.KnotType.BLANK,  "┆"
    );

	private Stage stage;
	private Scene scene;
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
    Button toggleSymbolsBtn;
    private int knotSymbolType = 0;
    
    //search id
    private TextField searchField;
    private PopupControl searchPopup;
    private ListView<String> searchList;
    private List<String> allIds;

    private boolean showColors = true;
    private Pattern currentPattern;

    @Override
    public void start(Stage stage) {
    	//Debug.enabled = true;
        this.stage = stage;

        Parent ui = buildUI();
        StackPane stack = new StackPane(ui);  // overlay layer for toast

        scene = new Scene(stack, WINDOW_WIDTH, WINDOW_HEIGHT);

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
    	//search
    	searchField = new TextField();
    	searchField.setPromptText("Enter pattern ID or URL");
    	searchField.setPrefWidth(150);

    	searchList = new ListView<>();
    	searchList.setPrefHeight(150);

    	searchPopup = new PopupControl();
    	searchPopup.setAutoHide(false);
    	searchPopup.getScene().setRoot(searchList);

    	// Inputs
        loadBtn = new Button("Load");
        copyPatternBtn = new Button("Copy Pattern");
        copyResultsBtn = new Button("Copy Results");
        deleteBtn = new Button("Delete");
        toggleSymbolsBtn = new Button("Diamonds");

        allowanceField = new TextField();
        lengthField    = new TextField();
        knotCountField = new TextField();

        allowanceField.setPrefWidth(80);
        lengthField.setPrefWidth(80);
        knotCountField.setPrefWidth(80);
        deleteBtn.setPrefWidth(80);
        toggleSymbolsBtn.setPrefWidth(80);

        Label allowanceLabel = new Label("Extra allowance:");
        Label lengthLabel    = new Label("Length:");
        Label knotCountLabel = new Label("Knot rows:");

        statusLight = new Label("● idle");
        statusLight.getStyleClass().addAll("status-light", "idle");

        HBox topBar = new HBox(
        	    10,
        	    searchField,
        	    loadBtn,
        	    toggleSymbolsBtn,
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
	    
	    toggleSymbolsBtn.setOnAction(e -> {
	        knotSymbolType = (knotSymbolType + 1) % 3;

            switch(knotSymbolType) {
                case 0 -> toggleSymbolsBtn.setText("Diamonds");
                case 1 -> toggleSymbolsBtn.setText("Text");
                case 2 -> toggleSymbolsBtn.setText("Arrows");
                default -> toggleSymbolsBtn.setText("Ya done messed up");
            }

	        if (currentPattern != null) {
	            displayPattern(currentPattern);
	        }
	    });
	    
	    scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

	        if (!searchField.isFocused()) {
	            return;
	        }

	        // DOWN: move selection inside the list, but keep focus on the field
	        if (e.getCode() == KeyCode.DOWN) {
	            if (!searchList.getItems().isEmpty() && searchPopup.isShowing()) {
	                int size = searchList.getItems().size();
	                int idx = searchList.getSelectionModel().getSelectedIndex();

	                if (idx < 0) {
	                    idx = 0;
	                } else if (idx < size - 1) {
	                    idx = idx + 1;
	                }

	                searchList.getSelectionModel().clearAndSelect(idx);
	                e.consume();
	            }
	            return;
	        }

	        // ENTER: prefer selected item; fall back to typed value
	        if (e.getCode() == KeyCode.ENTER) {
	            String chosen = null;

	            if (searchPopup.isShowing()) {
	                chosen = searchList.getSelectionModel().getSelectedItem();
	            }

	            if (chosen != null) {
	                searchField.setText(chosen);
	            }

	            String typed = searchField.getText().trim();
	            if (!typed.isEmpty()) {
	                handleLoad();
	            }

	            searchPopup.hide();
	            e.consume();
	        }
	    });
	    
	    searchField.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
	        if (e.getClickCount() == 2) {
	            // Reset selection state so ENTER works again
	            Platform.runLater(() -> {
	                searchField.selectEnd();   // collapse selection
	                searchField.positionCaret(searchField.getText().length());
	                searchField.requestFocus();
	            });
	        }
	    });

	    searchField.textProperty().addListener((obs, old, text) -> {
	        if (text == null || text.isBlank()) {
	            searchList.getItems().setAll(allIds);
	            showSearchPopup();
	            return;
	        }

	        List<String> matches = allIds.stream()
	                .filter(id -> id.contains(text))
	                .toList();

	        if (!matches.isEmpty()) {
	            searchList.getItems().setAll(matches);
	            showSearchPopup();
	        } else {
	            searchPopup.hide();
	        }
	    });
	    
	    searchField.focusedProperty().addListener((obs, old, focused) -> {
	        if (!focused) {
	            searchPopup.hide();
	        }
	    });
	    
	    searchField.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
	        if (!searchPopup.isShowing()) {
	            searchList.getItems().setAll(allIds);
	            showSearchPopup();
	        }
	    });
	    
	    searchList.setOnMouseClicked(e -> {
	        String selected = searchList.getSelectionModel().getSelectedItem();
	        if (selected != null) {
	            searchField.setText(selected);
	            handleLoad();
	        }
	        searchPopup.hide();
	    });

	    copyPatternBtn.setOnAction(e -> {
	        if (currentPattern == null) {
	        	showToast("Select a pattern first");
	        	return;
	        }

	        StringBuilder sb = new StringBuilder();
	        for (List<Pattern.KnotCell> row : currentPattern.getRows()) {
	            for (Pattern.KnotCell cell : row) {
	                sb.append(cell.knot().name()).append(",");
	            }
	            sb.append("\n");
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

	        String text = buildResultsText(currentPattern);

	        ClipboardContent content = new ClipboardContent();
	        content.putString(text);
	        Clipboard.getSystemClipboard().setContent(content);

	        showToast("Strings length copied!");
	    });
	    
	    deleteBtn.setOnAction(e -> {
	        String id = searchField.getText();
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
        String idOrUrl = searchField.getText();
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
                    PatternStorage.savePattern(p);
                }
                return p;
            }
        };

        task.setOnSucceeded(ev -> {
            currentPattern = task.getValue();
            knotCountField.setText(String.valueOf(DEFAULT_KNOT_ROWS));
            displayPattern(currentPattern);
            setStatus("ok");
            refreshSavedIds();

            allowanceField.setText(String.valueOf(currentPattern.getAllowance()));
            lengthField.setText(String.valueOf(currentPattern.getDesiredBraceletLength()));
            searchField.setText(currentPattern.getId());
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            setStatus("error");
            showError("Load failed: " + (ex != null ? ex.getMessage() : "unknown error"));
        });

        new Thread(task, "load-pattern").start();
    }

    //changes both textflows
    private void displayPattern(Pattern pattern) {
        patternFlow.getChildren().clear();
        resultsFlow.getChildren().clear();

        int targetRows;
        try {
            targetRows = Integer.parseInt(knotCountField.getText());
            if (targetRows < 1) targetRows = 1;
            if (targetRows > MAX_KNOT_ROWS) targetRows = MAX_KNOT_ROWS;
        } catch (NumberFormatException e) {
            targetRows = 100;
        }

        int baseRows = pattern.getRows().size();
        int loops = (int) Math.ceil((double) targetRows / baseRows);

        List<List<Pattern.KnotCell>> expanded = PatternAnalyzer.expandPatternRows(pattern, loops);

        int numStrings = pattern.getStrings().size();

        // patternflow loop
        for (int rowIndex = 0; rowIndex < targetRows; rowIndex++) {

            List<Pattern.KnotCell> row = expanded.get(rowIndex);

            boolean oddStrings = (numStrings % 2 == 1);

            /*
			* +------------------------------------------------------------------------------------
			* |          ODD STRING COUNT > STAGGERING
			* +------------------------------------------------------------------------------------
			*/

            if (oddStrings) {

                if (rowIndex % 2 == 1) {
                    patternFlow.getChildren().add(new Text("  "));
                }

                // Render knots
                for (Pattern.KnotCell cell : row) {

                    String label = cell.label();
                    String hex = (label == null) ? null : label;
                    if (hex == null || hex.equals("0")) hex = "#dddddd";

                    Color fxColor = showColors
                            ? Color.web(AnsiColor.brightenIfDark(hex))
                            : DEFAULT_COLOR;

                    // choose symbol type
                    Map<Pattern.KnotType, String> activeMap = switch(knotSymbolType) {
                        case 1 -> TEXT_SYMBOLS;
                        case 2 -> ARROW_SYMBOLS;
                        default -> DIAMOND_SYMBOLS;
                    };

                    String display = activeMap.getOrDefault(cell.knot(), "?") + " ";

                    Text t = new Text(display + " ");
                    t.setFill(fxColor);
                    patternFlow.getChildren().add(t);
                }

                if (rowIndex % 2 == 0) {
                    patternFlow.getChildren().add(new Text("  "));
                }

                patternFlow.getChildren().add(new Text("\n"));
                continue;
            }

            /*
			* +------------------------------------------------------------------------------------
			* |          EVEN STRING COUNT > NO OFFSET
			* +------------------------------------------------------------------------------------
			*/

            for (Pattern.KnotCell cell : row) {

                String label = cell.label();

                String hex = (label == null) ? null : label;
                if (hex == null || hex.equals("0")) hex = "#dddddd";

                Color fxColor = showColors
                        ? Color.web(AnsiColor.brightenIfDark(hex))
                        : DEFAULT_COLOR;

                // choose symbol type
                Map<Pattern.KnotType, String> activeMap = switch(knotSymbolType) {
                    case 1 -> TEXT_SYMBOLS;
                    case 2 -> ARROW_SYMBOLS;
                    default -> DIAMOND_SYMBOLS;
                };

                String display = activeMap.getOrDefault(cell.knot(), "?") + " ";

                Text t = new Text(display + " ");
                t.setFill(fxColor);
                patternFlow.getChildren().add(t);
            }

            patternFlow.getChildren().add(new Text("\n"));
        }

        /*
		* +------------------------------------------------------------------------------------
		* |          RESULTS
		* +------------------------------------------------------------------------------------
		*/

        String results = buildResultsText(pattern);

        for (String line : results.split("\n")) {
            Text t = new Text(line + "\n");

            String trimmed = line.trim();
            Color fxColor = DEFAULT_COLOR;

            int idx = trimmed.indexOf('#');
            if (idx != -1) {
                String hex = trimmed.substring(idx, Math.min(idx + 7, trimmed.length()));
                if (hex.matches("#[0-9a-fA-F]{6}")) {
                    fxColor = Color.web(AnsiColor.brightenIfDark(hex));
                }
            }

            t.setFill(fxColor);
            resultsFlow.getChildren().add(t);
        }
    }



    private String buildResultsText(Pattern p) {
        StringBuilder sb = new StringBuilder();

        sb.append("Strings:\n");
        p.getStringLengths().forEach((sid, len) -> {
            String label = p.getStrings().get(sid - 1).label();
            String hex   = p.getLabelToColor().get(label);

            sb.append("String ").append(sid)
              .append(" [").append(label).append("] ");

            if (hex != null) {
                sb.append(hex).append(" ");
            }

            sb.append(String.format("%.1f cm", len)).append("\n");
        });

        sb.append("\nColors:\n");
        p.getColorLengths().forEach((hex, len) -> {
            sb.append(hex).append(": ")
              .append(String.format("%.1f cm", len))
              .append("\n");
        });
        
        int rowsPerLoop = (p.getRepeats() > 0)
                ? p.getTotalRows() / p.getRepeats()
                : p.getTotalRows();
        sb.append("\nPattern info:\n");
        sb.append("Number of rows: ").append(rowsPerLoop).append("\n");
        sb.append("Number of repeats needed for a perfect pattern: ").append(p.getRepeats()).append("\n");
        sb.append("Number of knot rows until repeat: ").append(p.getTotalRows()).append("\n");

        sb.append("\nFinal order after one loop:\n");

        int count = 0;
        for (int sid : p.getFinalOrder()) {
            String label = p.getStrings().get(sid - 1).label();
            sb.append(sid).append("[").append(label).append("] ");

            count++;
            if (count % 5 == 0) {
                sb.append("\n");
            }
        }
        //break the line
        if (count % 5 != 0) {
            sb.append("\n");
        }
        
        return sb.toString();
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
    
    //for the number of knots input
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

    private void refreshSavedIds() {
        allIds = new ArrayList<>(PatternStorage.getSavedIds());

        allIds.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareToIgnoreCase(b);
            }
        });
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
    
    private void showSearchPopup() {
        if (allIds == null || allIds.isEmpty()) {
            searchPopup.hide();
            return;
        }

        Bounds b = searchField.localToScreen(searchField.getBoundsInLocal());
        searchPopup.show(searchField, b.getMinX(), b.getMaxY());
        searchField.requestFocus();
    }

    private void resetUI() {
        currentPattern = null;
        patternFlow.getChildren().clear();
        resultsFlow.getChildren().clear();
        allowanceField.clear();
        lengthField.clear();
        knotCountField.clear();
        setStatus("idle");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setGraphic(null);
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