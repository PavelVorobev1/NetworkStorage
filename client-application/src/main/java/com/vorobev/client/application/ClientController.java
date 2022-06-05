package com.vorobev.client.application;

import com.vorobev.cloud.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientController implements Initializable {

    @FXML
    public TableView<FileInfo> clientTable;
    @FXML
    public TableView serverTable;
    @FXML
    public Button uploadButton;
    @FXML
    public Button downloadButton;

    //    String homeDir = "client-files";
    Path homeDir = Path.of("client-files");

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
                CloudMessage command = network.read();
                if (command instanceof ListFiles) {
                    ListFiles listFiles = (ListFiles) command;
                    serverTable.getItems().clear();
                    List<String> fileInfoServer = listFiles.getFiles();
                    ArrayList<FileInfo> fileInfoArray = new ArrayList<>();
                    for (String file : fileInfoServer) {
                        fileInfoArray.add(new FileInfo(file));
                    }
                    serverTable.getItems().addAll(fileInfoArray);
                } else if (command instanceof FileMessage) {
                    FileMessage fileMessage = (FileMessage) command;
                    Path current = homeDir.resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    getFileClient(homeDir);
                }
            }

        } catch (IOException e) {
            System.err.println("Connection lost");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    private void createListServer() {
        TableColumn<FileInfo, String> filenameColumnServer = new TableColumn<>("Имя файла");
        filenameColumnServer.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filenameColumnServer.setPrefWidth(150);

//        TableColumn<FileInfo, Long> fileSizeColumnServer = new TableColumn<>("Размер файла");
//        fileSizeColumnServer.setCellValueFactory(new PropertyValueFactory<>("sizeFile"));
//        fileSizeColumnServer.setPrefWidth(100);
//
//        fileSizeColumnServer.setCellFactory(column -> {
//            return new TableCell<FileInfo, Long>() {
//                @Override
//                protected void updateItem(Long item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (item == null || empty) {
//                        setText(null);
//                        setStyle("");
//                    } else {
//                        String text = String.format("%,d bytes", item);
//                        setText(text);
//                    }
//                }
//            };
//        });

        serverTable.getColumns().addAll(filenameColumnServer);
    }


    private void createListClient() {
        TableColumn<FileInfo, String> filenameColumnClient = new TableColumn<>("Имя файла");
        filenameColumnClient.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        filenameColumnClient.setPrefWidth(150);

        TableColumn<FileInfo, Long> fileSizeColumnClient = new TableColumn<>("Размер файла");
        fileSizeColumnClient.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSizeFile()));
        fileSizeColumnClient.setPrefWidth(100);

        fileSizeColumnClient.setCellFactory(column -> {
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

        clientTable.getColumns().addAll(filenameColumnClient, fileSizeColumnClient);
        getFileClient(homeDir);
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
        try {
            String file = serverTable.getSelectionModel().getSelectedItem().toString();
            network.writeCommand(new FileRequest(file));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось скачать файл.", ButtonType.OK);
            alert.showAndWait();
            System.err.println("Не удалось скачать файл.");
            e.printStackTrace();
        }
    }

    public void buttonUploadAction(ActionEvent actionEvent) {
        if (clientTable.getSelectionModel().getSelectedItem().isDir()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Нельзя отправить папку. Выберите файл", ButtonType.OK);
            alert.showAndWait();
        } else {
            try {
                String file = clientTable.getSelectionModel().getSelectedItem().getFileName();
                network.writeCommand(new FileMessage(homeDir.resolve(file)));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось отправить файл.", ButtonType.OK);
                alert.showAndWait();
                System.err.println("Не удалось отправить файл.");
                e.printStackTrace();
            }
        }
    }

    public void buttonClientPathIn(ActionEvent actionEvent) {
        if (!clientTable.getSelectionModel().getSelectedItem().isDir()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Нельзя открыть файл. Выберите папку", ButtonType.OK);
            alert.showAndWait();
        } else {
            homeDir = homeDir.resolve(clientTable.getSelectionModel().getSelectedItem().getFileName());
            getFileClient(homeDir);
        }
    }

    public void buttonClientPathUp(ActionEvent actionEvent) {
        homeDir = homeDir.resolve("..");
        getFileClient(homeDir);
    }

    public void buttonServerPathUp(ActionEvent actionEvent) {
        try {
            String path = "..";
            network.writeCommand(new PathUpRequest(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buttonServerPathIn(ActionEvent actionEvent) {
//        FileInfo info = new FileInfo("1");
//        if (serverTable.getSelectionModel().getSelectedItem().getClass().isInstance(info)) {
//            Alert alert = new Alert(Alert.AlertType.WARNING, "Нельзя открыть файл. Выберите папку", ButtonType.OK);
//            alert.showAndWait();
//        } else {
        try {
            String path = serverTable.getSelectionModel().getSelectedItem().toString();
            network.writeCommand(new PathInRequest(path));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Нужно выбрать папку", ButtonType.OK);
            alert.showAndWait();
            System.err.println("Нужно выбрать папку");
            e.printStackTrace();
        }
//        }

    }
}