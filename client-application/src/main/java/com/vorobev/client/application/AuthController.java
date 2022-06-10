package com.vorobev.client.application;

import com.vorobev.cloud.AuthStatusClass;
import com.vorobev.cloud.UserInfo;
import com.vorobev.cloud.CloudMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;

    Network network;


    public void authButton(ActionEvent actionEvent) {
        if(loginField.getText().isEmpty() || passwordField.getText().isEmpty()){
            alertWindow("Введите логин и пароль");
        } else {
            try {
                network.writeCommand(new UserInfo(loginField.getText(),passwordField.getText()));
            } catch (IOException e) {
                System.err.println("Не удалось отправить логин и пароль");
                e.printStackTrace();
            }
        }
    }

    public void regButton(ActionEvent actionEvent) {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(8189);
        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();

    }

    private void readLoop(){
        boolean authStatus = false;
        try {
            while (!authStatus){
                CloudMessage command = network.read();
                AuthStatusClass auth = (AuthStatusClass) command;
                authStatus = auth.isStatus();
            }
            network.close();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        openWindow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void alertWindow(String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR, contentText, ButtonType.OK);
        alert.showAndWait();
    }

    private void openWindow() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fileManagerWindow = new FXMLLoader(ClientApplication.class.getResource("hello-view.fxml"));
        Scene fileManager = new Scene(fileManagerWindow.load());
        stage.setTitle("File manager");
        stage.setScene(fileManager);
        stage.show();
    }
}
