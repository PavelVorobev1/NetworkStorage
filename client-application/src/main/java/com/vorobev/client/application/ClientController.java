package com.vorobev.client.application;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientController implements Initializable {

    @FXML
    public TableView<FileInfoClient> clientTable;
    @FXML
    public TableView serverTable;
    @FXML
    public Button uploadButton;
    @FXML
    public Button downloadButton;

    String homeDir = "client-files";

    private byte[] buf;

    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        network = new Network(8189);

        createListClient();

        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();
    }

    private void readLoop() {
        createListServer();
        try {
            while (true) {
                String command = network.readCommand();
                if (command.equals("/list-server-files")) {
                    serverTable.getItems().clear();
                    FileInfoServer[] fileInfoServer = new FileInfoServer[network.readInt()];
                    for (int i = 0; i < fileInfoServer.length; i++) {
                        fileInfoServer[i] = new FileInfoServer(network.readCommand(), network.readLong());
                    }

                    serverTable.getItems().addAll(fileInfoServer);
                }
            }

        } catch (IOException e) {
            System.err.println("Connection lost");
            e.printStackTrace();
        }

    }

    private void createListServer() {
        TableColumn<FileInfoServer, String> filenameColumnServer = new TableColumn<>("Имя файла");
        filenameColumnServer.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filenameColumnServer.setPrefWidth(150);

        TableColumn<FileInfoClient, Long> fileSizeColumnServer = new TableColumn<>("Размер файла");
        fileSizeColumnServer.setCellValueFactory(new PropertyValueFactory<>("sizeFile"));
        fileSizeColumnServer.setPrefWidth(100);

        fileSizeColumnServer.setCellFactory(column -> {
            return new TableCell<FileInfoClient, Long>() {
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

        serverTable.getColumns().addAll(filenameColumnServer, fileSizeColumnServer);
    }


    private void createListClient() {
        TableColumn<FileInfoClient, String> filenameColumnClient = new TableColumn<>("Имя файла");
        filenameColumnClient.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        filenameColumnClient.setPrefWidth(150);

        TableColumn<FileInfoClient, Long> fileSizeColumnClient = new TableColumn<>("Размер файла");
        fileSizeColumnClient.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSizeFile()));
        fileSizeColumnClient.setPrefWidth(100);

        fileSizeColumnClient.setCellFactory(column -> {
            return new TableCell<FileInfoClient, Long>() {
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

        clientTable.getColumns().addAll(filenameColumnClient, fileSizeColumnClient);
        getFileClient(Paths.get(".", "client-files"));
    }

    private void getFileClient(Path path) {
        try {
            buf = new byte[256];
            clientTable.getItems().clear();
            clientTable.getItems().addAll(Files.list(path).map(FileInfoClient::new).collect(Collectors.toList()));
            clientTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось считать файлы", ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    public void buttonDownloadAction(ActionEvent actionEvent) {

    }

    public void buttonUploadAction(ActionEvent actionEvent) {

        try {
            network.getOutputStream().writeUTF("/upload-file");
            String file = clientTable.getSelectionModel().getSelectedItem().getFileName();
            network.getOutputStream().writeUTF(file);
            File toSend = Path.of(homeDir).resolve(file).toFile();
            network.getOutputStream().writeLong(toSend.length());
            try (FileInputStream fis = new FileInputStream(toSend)) {
                while (fis.available() > 0) {
                    int read = fis.read(buf);
                    network.getOutputStream().write(buf, 0, read);
                }
            }
            network.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}