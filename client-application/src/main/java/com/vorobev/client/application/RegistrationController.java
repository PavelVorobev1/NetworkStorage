package com.vorobev.client.application;

import com.vorobev.client.application.network.Network;
import com.vorobev.cloud.AuthStatusClass;
import com.vorobev.cloud.CloudMessage;
import com.vorobev.cloud.RegUser;
import com.vorobev.cloud.WarningServerClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// Не засунул в папку контроллеров, так окон открывается только из этой папки. С проблемой разберусь

public class RegistrationController implements Initializable {

    @FXML
    public PasswordField passwordRegField;
    @FXML
    public TextField loginRegField;
    @FXML
    public Button registrationButton;

    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(8189);
        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();
    }

    private void readLoop() {
        boolean regStatus = false;
        try {
            while (!regStatus) {
                CloudMessage command = network.read();
                if (command instanceof WarningServerClass) {
                    WarningServerClass warning = (WarningServerClass) command;
                    Platform.runLater(() -> alertWindow(warning.getWarning()));
                } else if (command instanceof AuthStatusClass) {
                    regStatus = true;
                }
            }
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Регистрация прошла успешно",ButtonType.APPLY);
                alert.showAndWait();
                Stage window = (Stage) registrationButton.getScene().getWindow();
                window.close();
            });
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка авторизации.");
            e.printStackTrace();
        }
    }

    public void regButton() {
        try {
            if (loginRegField.getText().isEmpty() || passwordRegField.getText().isEmpty()) {
                alertWindow("Введите логин и пароль");
            } else {
                network.writeCommand(new RegUser(loginRegField.getText().trim(), passwordRegField.getText().trim()));
            }
        } catch (IOException e) {
            System.err.println("Не удалось отправить данные на сервер.");
            e.printStackTrace();
        }
    }


    private void alertWindow(String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR, contentText, ButtonType.OK);
        alert.showAndWait();
    }
}
