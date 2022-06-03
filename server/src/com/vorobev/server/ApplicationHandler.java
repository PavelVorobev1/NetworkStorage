package com.vorobev.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;

public class ApplicationHandler implements Runnable {

    private final String serverFilesDir = "server-files";

    private DataInputStream input;
    private DataOutputStream output;

    public ApplicationHandler(Socket socket) {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            System.out.println("Клиент подключился к серверу");
            sendListOfFiles();
        } catch (IOException e) {
            System.err.println("Клиенту не удалось подключится к серверу");
            e.printStackTrace();
        }
    }

    public void run() {
        byte[] buf = new byte[256];
        try {
            while (true) {
                String command = input.readUTF();
                System.out.println("Command: " + command);
                if (command.equals("/upload-file")) {
                    String fileName = input.readUTF();
                    long len = input.readLong();
                    File file = Path.of(serverFilesDir).resolve(fileName).toFile();
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        for (int i = 0; i < (len + 255) / 256; i++) {
                            int read = input.read(buf);
                            fos.write(buf, 0 , read);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sendListOfFiles();
                }
            }
        } catch (Exception e) {
            System.err.println("Потеря соеденения");
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
            System.err.println("Не удалось отправить информацию о файлах");
            e.printStackTrace();
        }
    }




    private int getFilesInt(File dir){
        String[] list = dir.list();
        return list.length;
    }

}
