package com.vorobev.netty.handler;


import com.vorobev.cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;
    private Path homeDir = Path.of("server.home");

    public CloudFileHandler() {
        currentDir = Path.of("server.home");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest) {
            FileRequest fileRequest = (FileRequest) cloudMessage;
            if (Files.isDirectory(currentDir.resolve(Path.of(fileRequest.getName())))) {
                ctx.writeAndFlush(new WarningServerClass("/this dir"));
            } else {
                ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
            }
        } else if (cloudMessage instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) cloudMessage;
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(currentDir));

        } else if (cloudMessage instanceof PathUpRequest) {
            if (currentDir.normalize().equals(Path.of(String.valueOf(homeDir)))) {
                ctx.writeAndFlush(new WarningServerClass("/out directory"));
            } else {
                currentDir = currentDir.resolve("..");
                ctx.writeAndFlush(new ListFiles(currentDir));
            }
        } else if (cloudMessage instanceof PathInRequest) {
            PathInRequest pathInRequest = (PathInRequest) cloudMessage;
            if (!Files.isDirectory(currentDir.resolve(Path.of(pathInRequest.getPath())))) {
                ctx.writeAndFlush(new WarningServerClass("/not dir"));
            } else {
                currentDir = currentDir.resolve(Path.of(pathInRequest.getPath()));
                ctx.writeAndFlush(new ListFiles(currentDir));
            }
        } else if(cloudMessage instanceof UserInfo){
            ctx.writeAndFlush(new AuthStatusClass(true));
        } else if(cloudMessage instanceof AuthStatusClass){
            ctx.writeAndFlush(new ListFiles(currentDir));
        }
    }
}