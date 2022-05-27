package com.vorobev.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ApplicationHandler implements Runnable {
    private DataInputStream input;
    private DataOutputStream output;

    public ApplicationHandler(Socket socket) {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Клиенту не удалось подключится к серверу");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String command = input.readUTF();
                System.out.println("Command:" + command);
                output.writeUTF(command);
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
