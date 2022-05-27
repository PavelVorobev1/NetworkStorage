package com.vorobev.client.application;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientController implements Initializable {
    @FXML
    public TableView<FileInfo> clientTable;
    @FXML
    public TableView serverTable;
    @FXML
    public Button sendButton;
    @FXML
    public Button downloadButton;

    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        network = new Network(8189);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя файла");
        filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        filenameColumn.setPrefWidth(250);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер файла");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSizeFile()));
        fileSizeColumn.setPrefWidth(200);
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        setText(text);
                    }
                }
            };
        });

        clientTable.getColumns().addAll(filenameColumn, fileSizeColumn);
        getFileClient(Paths.get(".", "client-files"));

    }

    private void readLoop() {
        try {
            while (true) {
                String command = network.readCommand();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getFileClient(Path path) {
        try {
            clientTable.getItems().clear();
            clientTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось считать файлы", ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    public void buttonDownloadAction(ActionEvent actionEvent) {

    }

    public void buttonSendAction(ActionEvent actionEvent) {

    }
}