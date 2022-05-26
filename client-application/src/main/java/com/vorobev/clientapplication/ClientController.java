package com.vorobev.clientapplication;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class ClientController {
    @FXML
    private Label welcomeText;
    @FXML
    private ListView clientList;
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}