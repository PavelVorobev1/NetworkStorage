package com.vorobev.client.application;

import com.vorobev.cloud.CloudMessage;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.net.Socket;

public class Network {
    private ObjectDecoderInputStream inputStream;
    private ObjectEncoderOutputStream outputStream;

    public Network(int port){

        try {
            Socket socket = new Socket("localhost",port);
            outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Не удалось подключится к серверу.", ButtonType.OK);
            alert.showAndWait();
            System.err.println("Не удалось подключится к серверу");
            e.printStackTrace();
        }
    }

    public CloudMessage read() throws IOException, ClassNotFoundException {
        return (CloudMessage) inputStream.readObject();
    }




    public void writeCommand(CloudMessage message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
    }
    
    
}
