module com.example.clientapplication {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.vorobev.clientapplication to javafx.fxml;
    exports com.vorobev.clientapplication;
}