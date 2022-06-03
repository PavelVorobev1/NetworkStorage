package com.vorobev.nio_server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NioServer {
    private ServerSocketChannel server;
    private Selector selector;
    private final InetSocketAddress port = new InetSocketAddress(8189);
    private Path pathDir = Path.of("server.home");

    public NioServer() {
        try {
            server = ServerSocketChannel.open();
            selector = Selector.open();
            server.bind(port);
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.err.println("Failed to start the server");
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (server.isOpen()) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        handleAccept();
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRead(SelectionKey selectionKey) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        StringBuilder stringBuilder = new StringBuilder();
        String text;
        try {
            while (channel.isOpen()) {
                int read = channel.read(buffer);
                if (read < 0) {
                    channel.close();
                    return;
                }
                if (read == 0) {
                    break;
                }
                buffer.flip();
                while (buffer.hasRemaining()) {
                    stringBuilder.append((char) buffer.get());
                }
                buffer.clear();
            }
            text = stringBuilder.toString();
            if (text.equals("ls\r\n")) {
                stringBuilder.delete(0, stringBuilder.length());
                showDir(stringBuilder);
            }
            if (text.startsWith("cd")) {
                try {
                    stringBuilder.delete(0, stringBuilder.length());
                    Path path = Path.of(pathDir.toString());
                    text = text.substring(2,text.length() - 2);
                    pathDir = path.resolve(text.trim());
                    showDir(stringBuilder);
                } catch (NullPointerException e){
                    e.printStackTrace();
                    channel.write(ByteBuffer.wrap("Неверный путь к файлу".getBytes(StandardCharsets.UTF_8)));
                }
            }
            if (text.startsWith("cat")) {
                text = text.substring(3,text.length() - 2);
                List<String> textTheFile = Files.readAllLines(pathDir.resolve(text.trim()));
                for (String line : textTheFile) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
            }
            stringBuilder.append(">>");
            byte[] message = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
            channel.write(ByteBuffer.wrap(message));
        } catch (IOException e) {
            System.err.println("Error reading the message");
            channel.write(ByteBuffer.wrap("Error reading the message".getBytes(StandardCharsets.UTF_8)));
            e.printStackTrace();
        }
    }

    private void showDir(StringBuilder stringBuilder) {
        File fileDir = new File(pathDir.toString());
        for (File file : fileDir.listFiles()) {
            if (file.isDirectory()) {
                System.out.println(file + " [DIR]\n");
                stringBuilder.append(file.getName() + "[DIR]");
                stringBuilder.append(System.lineSeparator());
            } else {
                System.out.println(file);
                stringBuilder.append(file.getName());
                stringBuilder.append(System.lineSeparator());
            }
        }
    }


    private void handleAccept() {
        try {
            SocketChannel channel = server.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            channel.write(ByteBuffer.wrap("Welcome ->>\n".getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
