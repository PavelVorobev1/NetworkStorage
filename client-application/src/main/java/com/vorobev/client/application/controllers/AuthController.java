package com.vorobev.client.application.controllers;

import com.vorobev.client.application.ClientApplication;
import com.vorobev.client.application.network.Network;
import com.vorobev.cloud.AuthStatusClass;
import com.vorobev.cloud.CloudMessage;
import com.vorobev.cloud.UserInfo;
import com.vorobev.cloud.WarningServerClass;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

// Не разобрался как сделать одно подключение к серверу для всех окон.

public class AuthController {
    @FXML
    public AnchorPane authWindow;
    @FXML
    public TextField loginAuthField;
    @FXML
    public PasswordField passwordAuthField;

    private Network network;

    private boolean authStatus = false;

    public void setNetwork(Network network) {
        this.network = network;
    }
    public void createAuthWin(){
        try {
            Stage stage = new Stage();
            Scene authWindow = new Scene(new FXMLLoader(ClientApplication.class.getResource("auth.fxml")).load());
            stage.setTitle("Авторизация");
            stage.setScene(authWindow);;
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(this::readLoop);
        thread.setDaemon(true);
        thread.start();
    }

    private void readLoop() {

        try {
            while (!authStatus) {
                CloudMessage command = network.read();
                if (command instanceof WarningServerClass) {
                    WarningServerClass warning = (WarningServerClass) command;
                    Platform.runLater(() -> alertWindow(warning.getWarning()));
                } else if (command instanceof AuthStatusClass) {
                    AuthStatusClass auth = (AuthStatusClass) command;
                    authStatus = auth.isStatus();
                }
            }
            Stage stage = (Stage) authWindow.getScene().getWindow();
            stage.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isAuthStatus() {
        return authStatus;
    }

    private void alertWindow(String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING, contentText, ButtonType.OK);
        alert.showAndWait();
    }

//    public void authButton() {
//        if (loginAuthField.getText().isEmpty() || passwordAuthField.getText().isEmpty()) {
//            alertWindow("Введите логин и пароль");
//        } else {
//            try {
//                network.writeCommand(new UserInfo(loginAuthField.getText().trim(), passwordAuthField.getText().trim()));
//            } catch (IOException e) {
//                System.err.println("Не удалось отправить логин и пароль");
//                e.printStackTrace();
//            }
//        }
//    }

//    public void regButton() {
//        Platform.runLater(() -> {
//            try {
//                Stage stage = new Stage();
//                FXMLLoader fileManagerWindow = new FXMLLoader(ClientApplication.class.getResource("registration.fxml"));
//                Scene fileManager = new Scene(fileManagerWindow.load());
//                stage.setTitle("File manager");
//                stage.setScene(fileManager);
//                stage.show();
//            } catch (IOException e) {
//                System.err.println("Не удалось открыть окно регистраци");
//                e.printStackTrace();
//            }
//        });
//    }

}
