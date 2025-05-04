package org.example.bloodcellanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button fileChooserButton;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private ImageView imageView;
    @FXML
    private ImageView imageViewRGB;
    @FXML
    private Slider redFilter;
    @FXML
    private Slider blueFilter;
    @FXML
    private Label cellLabel;
    private PixelWriter pixelWriter;
    private PixelReader pixelReader;
    private PixelWriter pixelWriterRGB;
    private Image image;
    private WritableImage writableImage;
    private WritableImage writableImageRGB;
    private int[] pixelArray;
    private double redFilterValue;
    private double blueFilterValue;

    @FXML
    private void initialize() {
        redFilter.setMin(0);
        redFilter.setMax(1);
        blueFilter.setMin(0);
        blueFilter.setMax(1);

        redFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            redFilterValue=redFilter.getValue();
            displayBothImages();
        });
        blueFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            blueFilterValue=blueFilter.getValue();
            displayBothImages();
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
                if(red<=redFilterValue  && red>blue){
                    pixelWriterRGB.setColor(x, y, Color.RED);
                } else if (blue<=blueFilterValue  && blue>red) {
                    pixelWriterRGB.setColor(x, y, Color.PURPLE);
                }
                else{
                    pixelWriterRGB.setColor(x, y, Color.WHITE);
                }
            }
        }
        pixelArrayCreation();
        union();
    }

    public void pixelArrayCreation(){
        pixelArray=new int[(int)writableImageRGB.getWidth()*(int)writableImageRGB.getHeight()];
        for (int i = 0; i < pixelArray.length; i++) {
            pixelArray[i]=-1;
        }
    }

    @FXML
    public void union() {
        pixelArrayCreation();
        int position = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color current = writableImageRGB.getPixelReader().getColor(x, y);
                if (current.equals(Color.RED) || current.equals(Color.PURPLE)) {
                    if (x + 1 < image.getWidth()) {
                        Color right = writableImageRGB.getPixelReader().getColor(x + 1, y);
                        if (current.equals(right)) {
                            DisjointSet.union(pixelArray, position, position + 1);
                        }
                    }
                    if (y + 1 < image.getHeight()) {
                        Color down = writableImageRGB.getPixelReader().getColor(x, y + 1);
                        if (current.equals(down)) {
                            DisjointSet.union(pixelArray, position, position + (int) image.getWidth());
                        }
                    }
                } else {
                    pixelArray[position] = -2; // White background
                }
                position++;
            }
        }
        rectangleDraw();
    }
    @FXML
    private void rectangleDraw(){
        rectangleClear();
        List<Integer> rootList = new ArrayList<>();
        int minCellSize = 100;

        for (int i = 0; i < pixelArray.length; i++) {
            if (pixelArray[i] < -minCellSize) {
                rootList.add(i);
            }
        }

        int cellIndex = 1;
        for (int root : rootList) {
            int[] outerBounds = getOuterBoundOfCell(root);

            int minX = outerBounds[0];
            int maxX = outerBounds[1];
            int minY = outerBounds[2];
            int maxY = outerBounds[3];

            // Create rectangle
            Rectangle rect = new Rectangle(
                    minX,
                    minY,
                    maxX - minX,
                    maxY - minY
            );
            rect.setStroke(Color.BLACK);
            rect.setFill(Color.TRANSPARENT);

            // Create cell number label (Text)
            Text label = new Text(String.valueOf(cellIndex));
            label.setFill(Color.BLACK);
            label.setX(minX + 5);              // small offset inside rectangle
            label.setY(minY + 15);             // small offset from top

            // Add both to the pane
            mainPane.getChildren().addAll(rect, label);

            cellIndex++;
        }
        cellLabel.setText("Total amount of cells: "+cellIndex);

    }
    private void rectangleClear(){
        mainPane.getChildren().removeIf(node -> node instanceof Rectangle);
        mainPane.getChildren().removeIf(node -> node instanceof Text);
    }

    private int[] getOuterBoundOfCell(int root){
        int minX = (int) writableImageRGB.getWidth();
        int maxX = 0;
        int minY = (int) writableImageRGB.getHeight();
        int maxY = 0;

        for (int i = 0; i < pixelArray.length; i++) {
            if (DisjointSet.find(pixelArray, i) == root) {
                int x = i % (int) writableImageRGB.getWidth();
                int y = i / (int) writableImageRGB.getWidth();

                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }

        return new int[] { minX+14, maxX+14, minY+64, maxY+64 };
    }

}