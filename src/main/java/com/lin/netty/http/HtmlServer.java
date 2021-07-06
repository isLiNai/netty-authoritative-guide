package com.lin.netty.http;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import com.lin.netty.ftp.FtpServer;
import com.lin.netty.ftp.model.FtpMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * html 服务器 示例
 *
 * @author lin
 * @version 1.0
 * @date 2021/7/2 15:08
 */
public class HtmlServer {

    static String host = null;

    static {
        try {
            host = "http://"+ InetAddress.getLocalHost().getHostAddress()+":8080/";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        startUp(8080);
    }

    private static void startUp(int port){
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // http 协议解码器
                        ch.pipeline().addLast(new HttpServerCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(1048576));
                        ch.pipeline().addLast(new HttpHandler());
                    }
                });
        try {
            ChannelFuture sync = bootstrap.bind(port).sync();
            System.out.println("服务启动成功");
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            eventLoopGroup.shutdownGracefully();
        }
    }

    @ChannelHandler.Sharable
    static class HttpHandler extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof FullHttpRequest){
                FullHttpRequest req = (FullHttpRequest)msg;
                String url = req.uri();
                String filePath = mappingFilePath(url);
                File file = new File(filePath);
                if(!file.exists()){
                    FullHttpResponse fullHttpResponse = processFullHttpResponse(HttpResponseStatus.valueOf(404),null);
                    ctx.channel().writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
                    return;
                }
                byte[] contentBytes = HtmlUtil.readFile(file);
                FullHttpResponse fullHttpResponse = processFullHttpResponse(HttpResponseStatus.valueOf(200),contentBytes);
                ctx.channel().writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
            }
        }

        private FullHttpResponse processFullHttpResponse(HttpResponseStatus status,  byte[] contentBytes) {
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            fullHttpResponse.headers().add("Content-Type","text/html;charset=utf-8");
            fullHttpResponse.headers().add("Content-Language","zh-cn");
            if(contentBytes!=null){
                fullHttpResponse.headers().add("Content-Length",contentBytes.length);
                ByteBuf byteBuf = Unpooled.copiedBuffer(contentBytes);
                fullHttpResponse.content().writeBytes(byteBuf);
            }
            return fullHttpResponse;
        }
    }

    /**
     * url 与 项目资源文件映射
     * @param url
     * @return
     */
    private static String mappingFilePath(String url) {
        url = ClassLoader.getSystemResource("").getPath() + url.substring(1);
        return url;
    }


}
