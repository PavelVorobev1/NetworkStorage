package com.vorobev.client.application;

import com.vorobev.cloud.AuthStatusClass;
import com.vorobev.cloud.CloudMessage;
import com.vorobev.cloud.UserInfo;
import com.vorobev.cloud.WarningServerClass;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField loginField;
    @FXML
    public Button logButton;
    @FXML
    public Button registrationButton;

    private Network network;

    public void authButton(ActionEvent actionEvent) {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            alertWindow("Введите логин и пароль");
        } else {
            try {
                network.writeCommand(new UserInfo(loginField.getText().trim(), passwordField.getText().trim()));
            } catch (IOException e) {
                System.err.println("Не удалось отправить логин и пароль");
                e.printStackTrace();
            }
        }
    }

    public void regButton(ActionEvent actionEvent) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Stage stage = new Stage();
                    FXMLLoader regWindow = new FXMLLoader(RegistrationController.class.getResource("registration.fxml"));
                    Scene regScene = new Scene(regWindow.load());
                    stage.setTitle("Регистрация");
                    stage.setScene(regScene);
                    stage.show();
                } catch (IOException e) {
                    System.err.println("Не удалось открыть окно регистраци");
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(8189);
        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();
    }

    private void readLoop() {
        boolean authStatus = false;
        try {
            while (!authStatus) {
                CloudMessage command = network.read();
                if (command instanceof WarningServerClass) {
                    WarningServerClass warning = (WarningServerClass) command;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            alertWindow(warning.getWarning());
                        }
                    });
                } else if (command instanceof AuthStatusClass) {
                    AuthStatusClass auth = (AuthStatusClass) command;
                    authStatus = auth.isStatus();
                }
            }
            Stage window = (Stage) logButton.getScene().getWindow();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        window.close();
                        Stage stage = new Stage();
                        FXMLLoader fileManagerWindow = new FXMLLoader(ClientApplication.class.getResource("hello-view.fxml"));
                        Scene fileManager = new Scene(fileManagerWindow.load());
                        stage.setTitle("File manager");
                        stage.setScene(fileManager);
                        stage.show();
                    } catch (IOException e) {
                        alertWindow("Не удалось запустить приложение");
                        System.err.println("Не удалось запустить приложение");
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка авторизации.");
            e.printStackTrace();
        }
    }

    private void alertWindow(String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR, contentText, ButtonType.OK);
        alert.showAndWait();
    }

    private void openApplicationWindow() throws IOException {

    }


}
