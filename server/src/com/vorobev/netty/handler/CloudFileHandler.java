package com.vorobev.netty.handler;


import com.vorobev.cloud.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


import java.nio.file.Files;
import java.nio.file.Path;

public class CloudFileHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;

    public CloudFileHandler() {
        currentDir = Path.of("server.home");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new ListFiles(currentDir));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        if (cloudMessage instanceof FileRequest) {
            FileRequest fileRequest = (FileRequest) cloudMessage;
            ctx.writeAndFlush(new FileMessage(currentDir.resolve(fileRequest.getName())));
        } else if (cloudMessage instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) cloudMessage;
            Files.write(currentDir.resolve(fileMessage.getName()), fileMessage.getData());
            ctx.writeAndFlush(new ListFiles(currentDir));

        } else if (cloudMessage instanceof PathUpRequest) {
            currentDir = currentDir.resolve("..");
            ctx.writeAndFlush(new ListFiles(currentDir));

        } else if (cloudMessage instanceof PathInRequest) {
            PathInRequest pathInRequest = (PathInRequest) cloudMessage;
            currentDir = currentDir.resolve(Path.of(pathInRequest.getPath()));
            ctx.writeAndFlush(new ListFiles(currentDir));
        }
    }
}