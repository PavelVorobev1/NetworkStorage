package com.vorobev.client.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader authWindow = new FXMLLoader(ClientController.class.getResource("auth.fxml"));
        Scene auth = new Scene(authWindow.load());
        stage.setScene(auth);
        stage.setTitle("Авторизация");
        stage.show();

//        FXMLLoader fileManagerWindow = new FXMLLoader(ClientApplication.class.getResource("hello-view.fxml"));
//        Scene fileManager = new Scene(fileManagerWindow.load());
//        stage.setTitle("File manager");
//        stage.setScene(fileManager);
//        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}