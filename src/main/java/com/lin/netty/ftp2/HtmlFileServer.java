package com.lin.netty.ftp2;

import com.lin.netty.ftp2.engine.HttpEngine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.net.URLDecoder;

/**
 *
 * @author Lin
 */
public class HtmlFileServer {


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
                        ch.pipeline().addLast(new HttpObjectAggregator(655300000));
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


    static class HttpHandler extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof FullHttpRequest){
                FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
                HttpEngine.execute(ctx,fullHttpRequest);
            }else {
                ctx.pipeline().fireChannelRead(msg);
            }
        }
    }

}
