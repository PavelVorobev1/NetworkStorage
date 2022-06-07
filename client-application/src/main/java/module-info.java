module com.example.clientapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires com.vorobev.cloud;

    opens com.vorobev.client.application to javafx.fxml;
    exports com.vorobev.client.application;
}