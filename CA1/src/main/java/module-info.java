module org.example.ca1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.ca1 to javafx.fxml;
    exports org.example.ca1;
}