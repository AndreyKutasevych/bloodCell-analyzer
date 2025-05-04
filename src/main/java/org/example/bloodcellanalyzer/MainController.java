package org.example.bloodcellanalyzer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainController {

    @FXML
    private Slider cellSizeSlider;
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
    private WritableImage writableImageRGB;
    private int[] pixelArray;
    private double redFilterValue;
    private double blueFilterValue;
    private int minimalCellSize=20;

    @FXML
    private void initialize() {
        redFilter.setMin(0);
        redFilter.setMax(1);
        blueFilter.setMin(0);
        blueFilter.setMax(1);
        cellSizeSlider.setMin(20);
        cellSizeSlider.setMax(500);

        //adding listeners to the sliders
        redFilter.valueProperty().addListener((_, _, _) -> {
            redFilterValue=redFilter.getValue();
            displayBothImages();
        });
        blueFilter.valueProperty().addListener((_, _, _) -> {
            blueFilterValue=blueFilter.getValue();
            displayBothImages();
        });
        cellSizeSlider.valueProperty().addListener((_, _, _) -> {
            minimalCellSize=(int)cellSizeSlider.getValue();
            displayBothImages();
        });
    }

    @FXML
    private void fileChoosing() { // file selection and
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            image = new Image(file.toURI().toString(),imageView.getFitWidth(),imageView.getFitHeight(),false,true);
            pixelReader = image.getPixelReader();
            WritableImage writableImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());// setting the actual image
            writableImageRGB = new WritableImage((int) image.getWidth(), (int) image.getHeight());//setting the tricolor converted image
            pixelWriter = writableImage.getPixelWriter();//setting corresponding pixel writers
            pixelWriterRGB = writableImageRGB.getPixelWriter();
            imageView.setImage(writableImage);
            imageViewRGB.setImage(writableImageRGB);
            displayBothImages();//straight away calling a method to display images
        }
    }

    @FXML
    private void displayBothImages() {
        rectangleClear();
        for (int x = 0; x < image.getWidth(); x++) {//looping by width and then height pixel by pixel
            for (int y = 0; y < image.getHeight(); y++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));//setting actual pixel colors to the normal image
                double red = pixelReader.getColor(x,y).getRed();
                double blue = pixelReader.getColor(x,y).getBlue();//getting red and blue channels for the blood cells
                if(red<=redFilterValue  && red>blue){//comparing thresholds to the red or blue channels of image(you can tweak thresholds by using sliders
                    pixelWriterRGB.setColor(x, y, Color.RED);
                } else if (blue<=blueFilterValue  && blue>red) {
                    pixelWriterRGB.setColor(x, y, Color.PURPLE);
                }
                else{
                    pixelWriterRGB.setColor(x, y, Color.WHITE);
                }
            }
        }
        pixelArrayCreation();//calling method to assign all the pixels to an array(initial values are -1 - those are going to be used for making disjoint sets from a list)
        union();
    }

    public void pixelArrayCreation(){
        pixelArray=new int[(int)writableImageRGB.getWidth()*(int)writableImageRGB.getHeight()];
        Arrays.fill(pixelArray, -1);//simply iterating through every pixel of an image and assigning it to an array
    }

    @FXML
    public void union() {
        pixelArrayCreation();

        int position = 0; // temporary index of pixel(for union-find)

        // Looping through every pixel in the image, row by row
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color current = writableImageRGB.getPixelReader().getColor(x, y);

                //  Process only blood cells -red or blue - (red or purple pixels)
                if (current.equals(Color.RED) || current.equals(Color.PURPLE)) {

                    // Check right neighbor (x+1) if it's within bounds
                    if (x + 1 < image.getWidth()) {
                        Color right = writableImageRGB.getPixelReader().getColor(x + 1, y);
                        if (current.equals(right)) {
                            // Union current and right pixel if they are the same
                            DisjointSet.union(pixelArray, position, position + 1);
                        }
                    }

                    // Checking bottom neighbor if it's within bounds
                    if (y + 1 < image.getHeight()) {
                        Color down = writableImageRGB.getPixelReader().getColor(x, y + 1);
                        if (current.equals(down)) {
                            // union current and bottom pixel if they are the same
                            DisjointSet.union(pixelArray, position, position + (int) image.getWidth());
                        }
                    }

                } else {
                    // If the pixel is background (not a cell), it is marked as -2
                    // This ensures it's excluded from cell grouping logic
                    pixelArray[position] = -2;
                }

                position++; // Move to the next pixel's index
            }
        }

        rectangleDraw();
    }


    @FXML
    private void rectangleDraw() {
        rectangleClear();
        List<Integer> rootList = new ArrayList<>();// making a list of roots, as each cell has its own root

        for (int i = 0; i < pixelArray.length; i++) {
            if (pixelArray[i] < -minimalCellSize) {//comparing each pixel to the minimum cell size threshold, as  each root is a negative number with abs being the size of a cell
                rootList.add(i);
            }
        }

        int cellIndex = 1;//cell counter for counting all cells on the image
        for (int root : rootList) {
            int[] outerBounds = getOuterBoundOfCell(root);

            int minX = outerBounds[0];
            int maxX = outerBounds[1];
            int minY = outerBounds[2];
            int maxY = outerBounds[3];

            // Determining color type from a root pixel
            int x = root % (int) writableImageRGB.getWidth();
            int y = root / (int) writableImageRGB.getWidth();
            Color rootColor = writableImageRGB.getPixelReader().getColor(x, y);

            Color cellColor = rootColor.equals(Color.RED) ? Color.GREEN : Color.BLUE;

            Rectangle rect = new Rectangle(//making a rectangle of coordinates for each cell
                    minX,
                    minY,
                    maxX - minX,
                    maxY - minY
            );
            rect.setStroke(cellColor);
            rect.setFill(Color.TRANSPARENT);

            Text label = new Text(String.valueOf(cellIndex));//also adding label with cell index to each rectangle
            label.setFill(cellColor);
            label.setX(minX + 5);//setting the text within the rectangle
            label.setY(minY + 15);

            mainPane.getChildren().addAll(rect, label);
            cellIndex++;
        }

        cellLabel.setText("Total amount of cells: " + (cellIndex - 1));
    }


    private void rectangleClear(){//simple way of clearing everything once image is updated
        mainPane.getChildren().removeIf(node -> node instanceof Rectangle);
        mainPane.getChildren().removeIf(node -> node instanceof Text);
    }

    private int[] getOuterBoundOfCell(int root){//providing a method with root pixel
        int minX = (int) writableImageRGB.getWidth();//initially this is just a one big rectangle
        int maxX = 0;
        int minY = (int) writableImageRGB.getHeight();
        int maxY = 0;

        for (int i = 0; i < pixelArray.length; i++) {//looping again
            if (DisjointSet.find(pixelArray, i) == root) {
                int x = i % (int) writableImageRGB.getWidth();//root can also be on the bounds
                int y = i / (int) writableImageRGB.getWidth();

                if (x < minX) minX = x;//setting the width and height of rectangle depending on its position in terms of root pixel
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
            }
        }

        return new int[] { minX+14, maxX+14, minY+64, maxY+64 };//adding difference from start of pane and start of image
    }

}