package org.example.bloodcellanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button fileChooserButton;
    @FXML
    private ImageView imageView;
    @FXML
    private ImageView imageViewRGB;
    @FXML
    private Slider saturationSlider;
    private PixelWriter pixelWriter;
    private PixelReader pixelReader;
    private PixelWriter pixelWriterRGB;
    private PixelReader pixelReaderRGB;
    private Image image;
    private WritableImage writableImage;
    private WritableImage writableImageRGB;
    @FXML
    private void initialize() {
        saturationSlider.setMin(0);
        saturationSlider.setMax(1);
        saturationSlider.setShowTickLabels(true);
        saturationSlider.setShowTickMarks(true);
        saturationSlider.setMajorTickUnit(0.1);
        saturationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            displayRGBImage(saturationSlider.getValue());
        });
    }

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
            pixelReaderRGB = image.getPixelReader();
            writableImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
            writableImageRGB = new WritableImage((int) image.getWidth(), (int) image.getHeight());
            pixelWriter = writableImage.getPixelWriter();
            pixelWriterRGB = writableImageRGB.getPixelWriter();
            imageView.setImage(writableImage);
            imageViewRGB.setImage(writableImageRGB);
            displayBothImages();
        }
    }
    @FXML
    private void displayBothImages() {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
                double red = pixelReader.getColor(x,y).getRed();
                double blue = pixelReader.getColor(x,y).getBlue();
                double white = (red+blue)/2;
                if(red<=0.85 && red>=0.3 && red>blue){
                    pixelWriterRGB.setColor(x, y, Color.RED);
                } else if (blue<=0.75 && blue >=0.3 && blue>red) {
                    pixelWriterRGB.setColor(x, y, Color.PURPLE);
                }
                else{
                    pixelWriterRGB.setColor(x, y, Color.WHITE);
                }
            }
        }
    }
    @FXML
    private void displayRGBImage(double value) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = pixelReaderRGB.getColor(x, y);

                // Adjust saturation
                Color adjustedColor = Color.rgb(
                        (int)(color.getRed()*value*255),
                        (int)color.getGreen()*255,
                        (int)color.getBlue()*255
                );

                pixelWriterRGB.setColor(x, y, adjustedColor);
            }
        }
        imageViewRGB.setImage(writableImageRGB);
    }
}