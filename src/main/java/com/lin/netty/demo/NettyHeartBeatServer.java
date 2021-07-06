package com.lin.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 心跳检测 example
 * @author Lin
 */
public class NettyHeartBeatServer {

    public static void main(String[] args) {
        startUp(8080);
    }

    private static void startUp(int port) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        serverBootstrap.group(eventLoopGroup)
                // 标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
                .option(ChannelOption.SO_BACKLOG,1024)
                // 设置keepLive
                .childOption(ChannelOption.SO_KEEPALIVE,Boolean.TRUE)
                .channel(NioServerSocketChannel.class)
                // 初始化连接（socket）通道 流水线处理类
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 心跳检测
                        ch.pipeline().addLast(new IdleStateHandler(10,0,0, TimeUnit.SECONDS));
                        ch.pipeline().addLast(new HeartBeatServerHandler());
                    }
                });
        try {
            // 阻塞等待同步绑定完端口
            ChannelFuture sync = serverBootstrap.bind(port).sync();
            System.out.println("启动成功");
            // 阻塞等待 channel 关闭
            sync.channel().closeFuture().sync();
            System.out.println("test");
        } catch (InterruptedException e) {
            eventLoopGroup.shutdownGracefully();
        }
    }


    /**
     *  心跳处理类，投入真实使用 超过规定时间可以关闭通道
     */
    static class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent){
                IdleStateEvent event = (IdleStateEvent)evt;
                if (event.state()== IdleState.READER_IDLE){
                    ByteBuf byteBuf = Unpooled.copiedBuffer("你已经10秒钟没有收到消息了\r\n".getBytes());
                    ctx.channel().writeAndFlush(byteBuf);
                }
            }else {
                super.userEventTriggered(ctx,evt);
            }
        }
    }


}
