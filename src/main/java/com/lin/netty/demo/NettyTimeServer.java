package com.lin.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.util.Date;

/**
 * netty 时间服务器实践
 *
 * @author lin
 * @version 1.0
 * @date 2021/6/30 17:19
 */
public class NettyTimeServer {


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
                        // 将时间处理类 添加至流水线
                        ch.pipeline().addLast(new DateInBoundHanlder());
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
     * 该方式目前存在 拆包 粘包的问题
     *
     * SimpleChannelInboundHandler 实现了将 byteBuf 释放引用的方法
     */
    static class DateInBoundHanlder extends SimpleChannelInboundHandler<ByteBuf>{

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
            // 获取可读的字节长度
            int readableLength = byteBuf.readableBytes();
            // 初始化一个内存 bytes 数组，长度等于缓冲区的可读长度
            byte[] bytes = new byte[readableLength];
            // 读取至 内存数组
            byteBuf.readBytes(bytes);
            String str = new String(bytes, "utf-8");
            if(str != null && str.contains("getServerDate")){
                Date date = new Date();
                ByteBuf outByteBuf = Unpooled.copiedBuffer(date.toString().getBytes());
                // 从上下文获取 通道并写出 时间数据
                ctx.channel().writeAndFlush(outByteBuf);
            }
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("连接已离开");
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("有连接加入");
            super.channelRegistered(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("通道有异常");
            super.exceptionCaught(ctx, cause);
        }
    }
    
}
