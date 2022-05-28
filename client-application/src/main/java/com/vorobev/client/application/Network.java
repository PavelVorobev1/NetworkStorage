package com.vorobev.client.application;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    
    public Network(int port){
        Socket socket = null;
        try {
            socket = new Socket("localhost",port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Не удалось подключится к серверу.", ButtonType.OK);
            alert.showAndWait();
            System.err.println("Не удалось подключится к серверу");
            e.printStackTrace();
        }
    }

    public String readCommand() throws IOException {
        return inputStream.readUTF();
    }

    public int readInt() throws IOException {
        return inputStream.readInt();
    }

    public long readLong() throws IOException {
        return inputStream.readLong();
    }

    public void writeCommand(String message) throws IOException {
        outputStream.writeUTF(message);
        outputStream.flush();
    }
    
    
}
