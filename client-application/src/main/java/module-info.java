module com.example.clientapplication {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.vorobev.client.application to javafx.fxml;
    exports com.vorobev.client.application;
}