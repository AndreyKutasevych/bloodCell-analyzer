module org.example.bloodcellanalyzer {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.bloodcellanalyzer to javafx.fxml;
    exports org.example.bloodcellanalyzer;
}