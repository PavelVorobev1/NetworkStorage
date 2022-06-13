package com.vorobev.client.application;

import com.vorobev.cloud.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
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
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientController implements Initializable {

    @FXML
    public TableView<FileInfo> clientTable;
    @FXML
    public TableView<FileInfo> serverTable;
    @FXML
    public Button uploadButton;
    @FXML
    public Button downloadButton;
    @FXML
    public HBox mainHBox;
    @FXML
    public TextField loginAuthField;
    @FXML
    public AnchorPane authPane;
    @FXML
    public PasswordField passwordAuthField;
    @FXML
    public Button logAuthButton;
    @FXML
    public Button registrationButton;


    private Path currentDir = Path.of("client-files");
    private final Path homeDir = Path.of("client-files");

    private Network network;

    public void appendMessage(CloudMessage message) throws IOException {
        network.writeCommand(message);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new Network(8189);
        createListClient();
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
            authPane.setVisible(!authStatus);
            mainHBox.setVisible(authStatus);
            network.writeCommand(new AuthStatusClass(true));
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    createListServer();
                }
            });
            while (true) {
                CloudMessage command = network.read();
                if (command instanceof ListFiles) {
                    ListFiles listFiles = (ListFiles) command;
                    serverTable.getItems().clear();
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            alertWindow(warning.getWarning());
                        }
                    });
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

        TableColumn<FileInfo, Long> fileSizeColumnServer = new TableColumn<>("Размер файла");
        fileSizeColumnServer.setCellValueFactory(new PropertyValueFactory<>("sizeFile"));
        fileSizeColumnServer.setPrefWidth(100);

        fileSizeColumnServer.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
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
            };
        });

        serverTable.getColumns().addAll(filenameColumnServer, fileSizeColumnServer);

    }

    private void getFileServer(ArrayList<FileInfo> list) {
        serverTable.getItems().clear();
        serverTable.getItems().addAll(list);
        serverTable.getItems().sort(new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                return Long.valueOf(o1.getSizeFile() - o2.getSizeFile()).intValue();
            }
        });
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
            };
        });
        clientTable.getColumns().addAll(filenameColumnClient, fileSizeColumnClient);
        getFileClient(currentDir);
    }

    private void getFileClient(Path path) {
        try {
            clientTable.getItems().clear();
            clientTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientTable.getItems().sort(new Comparator<FileInfo>() {
                @Override
                public int compare(FileInfo o1, FileInfo o2) {
                    return Long.valueOf(o1.getSizeFile() - o2.getSizeFile()).intValue();
                }
            });
        } catch (IOException e) {
            alertWindow("Не удалось считать файлы");
            e.printStackTrace();
        }
    }

    public void buttonDownloadAction(ActionEvent actionEvent) {
        try {
            if (serverTable.getSelectionModel().getSelectedItem() == null) {
                alertWindow("Выберите файл который хотите скачать с сервера.");
            } else {
                String file = serverTable.getSelectionModel().getSelectedItem().toString();
                network.writeCommand(new FileRequest(file));
            }
        } catch (IOException e) {
            alertWindow("Не удалось скачать файл.");
            System.err.println("Не удалось скачать файл.");
            e.printStackTrace();
        }
    }

    public void buttonUploadAction(ActionEvent actionEvent) {
        if (clientTable.getSelectionModel().getSelectedItem() == null) {
            alertWindow("Выберите файл который хотите отправить на сервер");
        } else if (clientTable.getSelectionModel().getSelectedItem().isDir()) {
            alertWindow("Нельзя отправить папку. Выберите файл");
        } else {
            try {
                String file = clientTable.getSelectionModel().getSelectedItem().getFileName();
                network.writeCommand(new FileMessage(currentDir.resolve(file)));
            } catch (IOException e) {
                alertWindow("Не удалось отправить файл.");
                System.err.println("Не удалось отправить файл.");
                e.printStackTrace();
            }
        }
    }

    private void alertWindow(String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING, contentText, ButtonType.OK);
        alert.showAndWait();
    }

    public void buttonClientPathIn(ActionEvent actionEvent) {
        if (!clientTable.getSelectionModel().getSelectedItem().isDir()) {
            alertWindow("Нельзя открыть файл. Выберите папку");
        } else {
            currentDir = currentDir.resolve(clientTable.getSelectionModel().getSelectedItem().getFileName());
            getFileClient(currentDir);
        }
    }

    public void buttonClientPathUp(ActionEvent actionEvent) {
        if (currentDir.normalize().equals(Path.of(String.valueOf(homeDir)))) {
            alertWindow("Нельзя подняться выше по директории");
        } else {
            currentDir = currentDir.resolve("..");
            getFileClient(currentDir);
        }
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
        try {
            String path = serverTable.getSelectionModel().getSelectedItem().toString();
            network.writeCommand(new PathInRequest(path));
        } catch (IOException e) {
            alertWindow("Нужно выбрать папку");
            System.err.println("Нужно выбрать папку");
            e.printStackTrace();
        }
    }

    public void authButton(ActionEvent actionEvent) {
        if (loginAuthField.getText().isEmpty() || passwordAuthField.getText().isEmpty()) {
            alertWindow("Введите логин и пароль");
        } else {
            try {
                network.writeCommand(new UserInfo(loginAuthField.getText().trim(), passwordAuthField.getText().trim()));
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
                    stage.setResizable(false);
                    stage.setScene(regScene);
                    stage.show();
                } catch (IOException e) {
                    System.err.println("Не удалось открыть окно регистраци");
                    e.printStackTrace();
                }

            }
        });
    }
}