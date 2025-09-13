package org.example.ca1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.ca1.models.BloodCell;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
public class Controller implements Initializable {
    private final FileChooser fileChooser = new FileChooser();

    @FXML
    private Label welcomeText;
    @FXML
    private ImageView imageOriginal;
    @FXML
    private ImageView imageAnalysed;
    @FXML
    private Pane overlayPane; // should be layered on top of imageOriginal
    @FXML
    private Slider redHueMinSlider;
    @FXML
    private Slider redHueMaxSlider;
    @FXML
    private Slider whiteHueMinSlider;
    @FXML
    private Slider whiteHueMaxSlider;
    @FXML
    private Slider redSaturationSlider;
    @FXML
    private Slider whiteSaturationSlider;

    private Image originalImage;
    private Image tricolorImage;

    private Stage stage;
    private Scene scene;
    private FXMLLoader loader;

    @FXML private Label numberofRedLabel;
    @FXML private Label numberofWhiteLabel;
//    @FXML private Label numberofClustersLabel;


    public void switchtoAnaylserScene(ActionEvent event) throws IOException {
        loader = new FXMLLoader(Application.class.getResource("analyser.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void OpenImage(ActionEvent event) {
        File initialDirectory = new File("src/main/resources/Images/");
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png")
        );

        Window window = imageOriginal.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            try {
                originalImage = new Image(file.toURI().toString());
                if (originalImage.isError()) {
                    throw new IOException(originalImage.getException());
                }

                // Show original image
                imageOriginal.setImage(originalImage);

                // Reset sliders to defaults
                resetSliders();

                // Immediately process and show results
                convertToTricolorAndAnalyze();

            } catch (Exception e) {
                showErrorAlert("Image Loading Error", "Failed to load image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void resetSliders() {
        if (redHueMinSlider != null) redHueMinSlider.setValue(330);
        if (redHueMaxSlider != null) redHueMaxSlider.setValue(30);
        if (redSaturationSlider != null) redSaturationSlider.setValue(0.2);
        if (whiteHueMinSlider != null) whiteHueMinSlider.setValue(260);
        if (whiteHueMaxSlider != null) whiteHueMaxSlider.setValue(300);
        if (whiteSaturationSlider != null) whiteSaturationSlider.setValue(0.3);
    }

    @FXML
    private void convertToTricolorAndAnalyze() {
        if (originalImage == null) {
            showErrorAlert("Error", "Please load an image first");
            return;
        }

        double redHueMin = redHueMinSlider.getValue();
        double redHueMax = redHueMaxSlider.getValue();
        double whiteHueMin = whiteHueMinSlider.getValue();
        double whiteHueMax = whiteHueMaxSlider.getValue();
        double redSaturationThreshold = redSaturationSlider.getValue();
        double whiteSaturationThreshold = whiteSaturationSlider.getValue();

        try {
            // Generate tricolor image
            tricolorImage = ImageProcessor.convertToTricolor(
                    originalImage,
                    redHueMin, redHueMax,
                    whiteHueMin, whiteHueMax,
                    redSaturationThreshold, whiteSaturationThreshold
            );

            imageAnalysed.setImage(tricolorImage);

            // Analyze image and get blood cells
            List<BloodCell> cells = BloodCellAnalyser.analyzeImage(tricolorImage);

            // Assign IDs
            int id = 1;
            for (BloodCell cell : cells) {
                cell.setId(id++);
            }

            // Count cells by type
            int redCellCount = 0;
            int whiteCellCount = 0;
            int clusterCount = 0;

            for (BloodCell cell : cells) {
                switch (cell.getType()) {
                    case RED:
                        redCellCount++;
                        break;
                    case CLUSTER:
                        clusterCount++;
                        break;
                    case WHITE:
                        whiteCellCount++;
                        break;
                }
            }

            // Update labels
            numberofRedLabel.setText(String.valueOf(redCellCount));
            numberofWhiteLabel.setText(String.valueOf(whiteCellCount));
//            numberofClustersLabel.setText(String.valueOf(clusterCount));

            // Draw rectangles on overlay
            drawOverlay(cells);

        } catch (Exception e) {
            showErrorAlert("Processing Error", "Failed to analyze image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void drawOverlay(List<BloodCell> cells) {
        overlayPane.getChildren().clear();

        Image image = imageOriginal.getImage();
        if (image == null) return;

        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        double viewWidth = imageOriginal.getFitWidth();
        double viewHeight = imageOriginal.getFitHeight();

        // Maintain aspect ratio
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
        double displayedWidth = imageWidth * scale;
        double displayedHeight = imageHeight * scale;

        // Image might be centered, so compute offsets within ImageView
        double offsetX = (viewWidth - displayedWidth) / 2;
        double offsetY = (viewHeight - displayedHeight) / 2;

        // Get imageOriginal's location within its parent (StackPane)
        Bounds imageViewBounds = imageOriginal.localToScene(imageOriginal.getBoundsInLocal());
        Bounds overlayBounds = overlayPane.localToScene(overlayPane.getBoundsInLocal());

        double sceneOffsetX = imageViewBounds.getMinX() - overlayBounds.getMinX() + offsetX;
        double sceneOffsetY = imageViewBounds.getMinY() - overlayBounds.getMinY() + offsetY;

        for (BloodCell cell : cells) {
            Rectangle boundingBox = cell.getBoundingBox();

            double x = boundingBox.getX() * scale + sceneOffsetX;
            double y = boundingBox.getY() * scale + sceneOffsetY;
            double width = boundingBox.getWidth() * scale;
            double height = boundingBox.getHeight() * scale;

            javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(x, y, width, height);
            rect.setStroke(cell.getDisplayColor());
            rect.setFill(Color.TRANSPARENT);
            rect.setStrokeWidth(1.5);

            // Label placement just above the box
            String labelText = "" + cell.getId();
            if (cell.isCluster()) {
                int approxCount = (int) Math.round(cell.calculateApproxCellCount());
                labelText += " (~" + approxCount + ")";
            }

            Label label = new Label(labelText);
            label.setTextFill(Color.BLACK);
            label.setStyle("-fx-background-color: rgba(255,255,255,0.7); -fx-font-size: 11;");
            label.setLayoutX(x);
            label.setLayoutY(y - 15);

            overlayPane.getChildren().addAll(rect, label);
        }
    }

    @FXML
    private void onSliderChanged() {
        if (originalImage != null) {
            convertToTricolorAndAnalyze();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Add null checks
        if (imageOriginal != null && overlayPane != null) {
            // Bind overlay to imageOriginal size
            overlayPane.prefWidthProperty().bind(imageOriginal.fitWidthProperty());
            overlayPane.prefHeightProperty().bind(imageOriginal.fitHeightProperty());
        }

        setupSliderListeners();

        // Default file chooser directory
        String userHome = System.getProperty("user.home");
        File picturesDir = new File(userHome, "Pictures");
        fileChooser.setInitialDirectory(picturesDir.exists() ? picturesDir : new File(userHome));
    }

    private void setupSliderListeners() {
        // Add null checks for all sliders
        if (redHueMinSlider != null) {
            redHueMinSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
                if (!isNowChanging) convertToTricolorAndAnalyze();
            });
        }
        if (redHueMaxSlider != null) {
            redHueMaxSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
                if (!isNowChanging) convertToTricolorAndAnalyze();
            });
        }
        if (whiteHueMinSlider != null) {
            whiteHueMinSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
                if (!isNowChanging) convertToTricolorAndAnalyze();
            });
        }
        if (whiteHueMaxSlider != null) {
            whiteHueMaxSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
                if (!isNowChanging) convertToTricolorAndAnalyze();
            });
        }
        if (redSaturationSlider != null) {
            redSaturationSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
                if (!isNowChanging) convertToTricolorAndAnalyze();
            });
        }
        if (whiteSaturationSlider != null) {
            whiteSaturationSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
                if (!isNowChanging) convertToTricolorAndAnalyze();
            });
        }
    }
}