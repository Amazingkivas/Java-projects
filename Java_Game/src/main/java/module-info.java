module com.example.java_game {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.example.java_game to javafx.fxml;
    exports com.example.java_game;
}