package com.vorobev.client.application.controllers;

import com.vorobev.client.application.ClientApplication;
import com.vorobev.client.application.network.Network;
import com.vorobev.cloud.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

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
    public TableView<FileInfo> serverTable;
    @FXML
    public HBox mainHBox;
    @FXML
    public TextField loginAuthField;
    @FXML
    public AnchorPane authPane;
    @FXML
    public PasswordField passwordAuthField;
    @FXML
    public TextField newDirNameFieldClient;
    @FXML
    public ToolBar openNewDirToolBarClient;
    @FXML
    public ToolBar openNewDirToolBarServer;
    @FXML
    public TextField newDirNameFieldServer;

    private final Path homeDir = Path.of("client-files");
    private Path currentDir = homeDir;

    private boolean authStatus = false;

    private Network network;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(!Files.exists(homeDir)){
            try {
                Files.createDirectories(homeDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        network = new Network(8189);
        createListClient();
        Thread readThread = new Thread(this::readLoop);
        readThread.setDaemon(true);
        readThread.start();
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
            authPane.setVisible(!authStatus);
            mainHBox.setVisible(authStatus);
            network.writeCommand(new AuthStatusClass(true));
            Platform.runLater(this::createListServer);
            while (true) {
                CloudMessage command = network.read();
                if (command instanceof ListFiles) {
                    ListFiles listFiles = (ListFiles) command;
                    List<FileInfo> fileInfoServer = listFiles.getFiles();
                    ArrayList<FileInfo> arrayList = new ArrayList<>(fileInfoServer);
                    getFileServer(arrayList);
                } else if (command instanceof FileMessage) {
                    FileMessage fileMessage = (FileMessage) command;
                    Path current = currentDir.resolve(fileMessage.getName());
                    Files.write(current, fileMessage.getData());
                    getFileClient(currentDir);
                } else if (command instanceof WarningServerClass) {
                    WarningServerClass warning = (WarningServerClass) command;
                    Platform.runLater(() -> alertWindow(warning.getWarning()));
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
        TableColumn<FileInfo, String> filenameColumnServer = new TableColumn<>("?????? ??????????");
        filenameColumnServer.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filenameColumnServer.setPrefWidth(200);

        TableColumn<FileInfo, Long> fileSizeColumnServer = new TableColumn<>("???????????? ??????????");
        fileSizeColumnServer.setCellValueFactory(new PropertyValueFactory<>("sizeFile"));
        fileSizeColumnServer.setPrefWidth(100);

        fileSizeColumnServer.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                String text;
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else if (item <= -1L) {
                    text = "[DIR]";
                    setText(text);
                } else {
                    text = String.format("%,d bytes", item);
                    setText(text);
                }
            }
        });

        serverTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                try {
                    String path = serverTable.getSelectionModel().getSelectedItem().toString();
                    network.writeCommand(new PathInRequest(path));
                } catch (IOException e) {
                    alertWindow("?????????? ?????????????? ??????????");
                    System.err.println("?????????? ?????????????? ??????????");
                    e.printStackTrace();
                }
            }
        });
        serverTable.getColumns().addAll(filenameColumnServer, fileSizeColumnServer);
    }

    private void getFileServer(ArrayList<FileInfo> list) {
        serverTable.getItems().clear();
        serverTable.getItems().addAll(list);
        serverTable.getItems().sort((o1, o2) -> Long.valueOf(o1.getSizeFile() - o2.getSizeFile()).intValue());
    }


    private void createListClient() {
        TableColumn<FileInfo, String> filenameColumnClient = new TableColumn<>("?????? ??????????");
        filenameColumnClient.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        filenameColumnClient.setPrefWidth(200);

        TableColumn<FileInfo, Long> fileSizeColumnClient = new TableColumn<>("???????????? ??????????");
        fileSizeColumnClient.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSizeFile()));
        fileSizeColumnClient.setPrefWidth(100);

        fileSizeColumnClient.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                String text;
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else if (item <= -1L) {
                    text = "[DIR]";
                    setText(text);
                } else {
                    text = String.format("%,d bytes", item);
                    setText(text);
                }
            }
        });
        clientTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                if (!clientTable.getSelectionModel().getSelectedItem().isDir()) {
                    alertWindow("???????????? ?????????????? ????????. ???????????????? ??????????");
                } else {
                    currentDir = currentDir.resolve(clientTable.getSelectionModel().getSelectedItem().getFileName());
                    getFileClient(currentDir);
                }
            }
        });
        clientTable.getColumns().addAll(filenameColumnClient, fileSizeColumnClient);
        getFileClient(currentDir);
    }

    private void getFileClient(Path path) {
        try {
            clientTable.getItems().clear();
            clientTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientTable.getItems().sort((o1, o2) -> Long.valueOf(o1.getSizeFile() - o2.getSizeFile()).intValue());
        } catch (IOException e) {
            alertWindow("???? ?????????????? ?????????????? ??????????");
            e.printStackTrace();
        }
    }

    public void buttonDownloadAction() {
        try {
            if (serverTable.getSelectionModel().getSelectedItem() == null) {
                alertWindow("???????????????? ???????? ?????????????? ???????????? ?????????????? ?? ??????????????.");
            } else {
                String file = serverTable.getSelectionModel().getSelectedItem().toString();
                network.writeCommand(new FileRequest(file));
            }
        } catch (IOException e) {
            alertWindow("???? ?????????????? ?????????????? ????????.");
            System.err.println("???? ?????????????? ?????????????? ????????.");
            e.printStackTrace();
        }
    }

    public void buttonUploadAction() {
        if (clientTable.getSelectionModel().getSelectedItem() == null) {
            alertWindow("???????????????? ???????? ?????????????? ???????????? ?????????????????? ???? ????????????");
        } else if (clientTable.getSelectionModel().getSelectedItem().isDir()) {
            alertWindow("???????????? ?????????????????? ??????????. ???????????????? ????????");
        } else {
            try {
                String file = clientTable.getSelectionModel().getSelectedItem().getFileName();
                network.writeCommand(new FileMessage(currentDir.resolve(file)));
            } catch (IOException e) {
                alertWindow("???? ?????????????? ?????????????????? ????????.");
                System.err.println("???? ?????????????? ?????????????????? ????????.");
                e.printStackTrace();
            }
        }
    }

    private void alertWindow(String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING, contentText, ButtonType.OK);
        alert.showAndWait();
    }


    public void buttonClientPathUp() {
        if (currentDir.normalize().equals(Path.of(String.valueOf(homeDir)))) {
            alertWindow("???????????? ?????????????????? ???????? ???? ????????????????????");
        } else {
            currentDir = currentDir.resolve("..");
            getFileClient(currentDir);
        }
    }

    public void buttonServerPathUp() {
        try {
            String path = "..";
            network.writeCommand(new PathUpRequest(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void authButton() {
        if (loginAuthField.getText().isEmpty() || passwordAuthField.getText().isEmpty()) {
            alertWindow("?????????????? ?????????? ?? ????????????");
        } else {
            try {
                network.writeCommand(new UserInfo(loginAuthField.getText().trim(), passwordAuthField.getText().trim()));
            } catch (IOException e) {
                System.err.println("???? ?????????????? ?????????????????? ?????????? ?? ????????????");
                e.printStackTrace();
            }
        }
    }

    public void regButton() {
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                FXMLLoader fileManagerWindow = new FXMLLoader(ClientApplication.class.getResource("registration.fxml"));
                Scene fileManager = new Scene(fileManagerWindow.load());
                stage.setTitle("File manager");
                stage.setScene(fileManager);
                stage.show();
            } catch (IOException e) {
                System.err.println("???? ?????????????? ?????????????? ???????? ????????????????????");
                e.printStackTrace();
            }
        });
    }

    public void addDirClient() {
        openNewDirToolBarClient.setVisible(true);
    }

    public void addDirServer() {
        openNewDirToolBarServer.setVisible(true);
    }

    public void createDirClient() {
        try {
            if (newDirNameFieldClient.getText().isEmpty()) {
                alertWindow("?????????????? ???????????????? ??????????");
            } else {
                Files.createDirectories(currentDir.resolve(Path.of(newDirNameFieldServer.getText().trim())));
                getFileClient(currentDir);
                openNewDirToolBarClient.setVisible(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createDirServer() {
        try {
            if (newDirNameFieldServer.getText().isEmpty()) {
                alertWindow("?????????????? ???????????????? ??????????");
            } else {
                network.writeCommand(new CreateNewDir(newDirNameFieldServer.getText().trim()));
                openNewDirToolBarServer.setVisible(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancelToolBar() {
        newDirNameFieldClient.clear();
        newDirNameFieldServer.clear();
        openNewDirToolBarServer.setVisible(false);
        openNewDirToolBarClient.setVisible(false);
    }
}