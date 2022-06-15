package com.vorobev.netty.handler;


import com.vorobev.cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Path;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    DbHandler db = new DbHandler();
    private Path currentDir;
    private Path homeDir = Path.of("server.home");

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        db.run();
    }

    public CloudFileHandler() {
        currentDir = homeDir;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest) {
            FileRequest fileRequest = (FileRequest) cloudMessage;
            if (Files.isDirectory(currentDir.resolve(Path.of(fileRequest.getName())))) {
                ctx.writeAndFlush(new WarningServerClass("Нельзя скачать папку. Выберите файл"));
            } else {
                ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
            }
        } else if (cloudMessage instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) cloudMessage;
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(currentDir));

        } else if (cloudMessage instanceof PathUpRequest) {
            if (currentDir.normalize().equals(Path.of(String.valueOf(homeDir)))) {
                ctx.writeAndFlush(new WarningServerClass("Нельзя подняться выше по директории."));
            } else {
                currentDir = currentDir.resolve("..");
                ctx.writeAndFlush(new ListFiles(currentDir));
            }
        } else if (cloudMessage instanceof PathInRequest) {
            PathInRequest pathInRequest = (PathInRequest) cloudMessage;
            if (!Files.isDirectory(currentDir.resolve(Path.of(pathInRequest.getPath())))) {
                ctx.writeAndFlush(new WarningServerClass("Нельзя открыть файл. Выберите папку"));
            } else {
                currentDir = currentDir.resolve(Path.of(pathInRequest.getPath()));
                ctx.writeAndFlush(new ListFiles(currentDir));
            }
        } else if (cloudMessage instanceof UserInfo) {
            UserInfo userInfo = (UserInfo) cloudMessage;
            if (db.findUserForAuth(userInfo.getLogin(), userInfo.getPassword())) {
                homeDir = homeDir.resolve(userInfo.getLogin());
                currentDir = homeDir;
                ctx.writeAndFlush(new AuthStatusClass(true));
            } else {
                ctx.writeAndFlush(new WarningServerClass("Пользователь не найден"));
            }
        } else if (cloudMessage instanceof RegUser) {
            RegUser regUser = (RegUser) cloudMessage;
            if (db.findUser(regUser.getRegLogin())) {
                ctx.writeAndFlush(new WarningServerClass("Такой пользователь уже существует"));
            } else {
                db.addUser(regUser.getRegLogin(), regUser.getRegPassword());
                Files.createDirectories(Path.of("server.home").resolve(regUser.getRegLogin()));
                ctx.writeAndFlush(new AuthStatusClass(true));
            }
        } else if (cloudMessage instanceof AuthStatusClass) {
            ctx.writeAndFlush(new ListFiles(currentDir));
        } else if(cloudMessage instanceof CreateNewDir){
            CreateNewDir newDir = (CreateNewDir) cloudMessage;
            Files.createDirectories(currentDir.resolve(newDir.getDirName()));
            ctx.writeAndFlush(new ListFiles(currentDir));
        }
    }
}