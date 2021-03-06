package com.vorobev.client.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fileManagerWindow = new FXMLLoader(ClientApplication.class.getResource("main.fxml"));
        Scene fileManager = new Scene(fileManagerWindow.load());
        stage.setTitle("File manager");
        stage.setScene(fileManager);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}