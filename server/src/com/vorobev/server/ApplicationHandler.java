package com.vorobev.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationHandler implements Runnable {

    private final String serverFilesDir = "server-files";
    
    private DataInputStream input;
    private DataOutputStream output;

    public ApplicationHandler(Socket socket) {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            sendListOfFiles();
        } catch (IOException e) {
            System.err.println("Клиенту не удалось подключится к серверу");
            e.printStackTrace();
        }
    }


    private void sendListOfFiles(){
        try {
            output.writeUTF("/list-server-files");

            File dir = new File(serverFilesDir);
            output.writeInt(getFilesInt(dir));
            for (File file : dir.listFiles() ) {
                if(file.isFile()){
                    output.writeUTF(file.getName());
                    output.writeLong(file.length());
                    output.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private int getFilesInt (File dir){
        String[] list = dir.list();
        return list.length;
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
