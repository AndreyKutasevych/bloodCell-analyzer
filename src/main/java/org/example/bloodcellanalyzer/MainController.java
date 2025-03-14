package org.example.bloodcellanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import javafx.scene.input.*;
import javafx.scene.paint.*;

import java.io.File;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button fileChooserButton;
    @FXML
    private ImageView imageView;
    private PixelWriter pixelWriter;
    private PixelReader pixelReader;
    private Image image;
    private WritableImage writableImage;

    @FXML
    private void fileChoosing() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            image = new Image(file.toURI().toString(),imageView.getFitWidth(),imageView.getFitHeight(),false,true);
            pixelReader = image.getPixelReader();
            writableImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
            pixelWriter = writableImage.getPixelWriter();
            imageView.setImage(writableImage);
            displayImage();
        }
    }
    @FXML
    private void displayImage() {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
            }
        }

    }
}