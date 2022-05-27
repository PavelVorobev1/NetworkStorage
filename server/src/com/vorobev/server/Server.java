package com.vorobev.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8189)){
            System.out.println("Server started");
            while (true){
                Socket socket = server.accept();
                ApplicationHandler handler = new ApplicationHandler(socket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("Не удалось запустить сервер.");
            e.printStackTrace();
        }
    }
}
