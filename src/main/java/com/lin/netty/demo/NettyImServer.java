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
import io.netty.util.AttributeKey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Lin
 */
public class NettyImServer {

    private static Map<String,Channel> channelMap = new HashMap<>();

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
                        ch.pipeline().addLast(new LoginInBoundHandler());
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

    static class LoginInBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
            // 获取可读的字节长度
            int readableLength = byteBuf.readableBytes();
            // 初始化一个内存 bytes 数组，长度等于缓冲区的可读长度
            byte[] bytes = new byte[readableLength];
            // 读取至 内存数组
            byteBuf.readBytes(bytes);
            String str = new String(bytes, "utf-8");
            if(str != null && str.contains("login@")){
                String[] split = str.split("login@");
                String userId = split[1];
                userId = userId.replaceAll("\r|\n", "");
                // channel 与 userId 关联（即 userId ==> socket）
                ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);
                // 保存user 与 channel 的信息
                channelMap.put(userId,ctx.channel());
                // 登录成功后，将登录的 handler 替换成为 发送消息的 handler
                ctx.channel().pipeline().replace(this,"messageHandler", new MessageInBoundHandler());
            }
        }
     
    }

    static class MessageInBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
            // 获取可读的字节长度
            int readableLength = byteBuf.readableBytes();
            // 初始化一个内存 bytes 数组，长度等于缓冲区的可读长度
            byte[] bytes = new byte[readableLength];
            // 读取至 内存数组
            byteBuf.readBytes(bytes);
            String str = new String(bytes, "utf-8");
            if(str != null){
                String[] split = str.split("@");
                String toUserId = split[0];
                String message = split[1];
                // channel 与 userId 关联（即 userId ==> socket）
                Channel channel = channelMap.get(toUserId);
                String userId = (String) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
                message = "【" + userId + "】 和你说："+ message;
                ByteBuf sendByteBuf = Unpooled.copiedBuffer(message.getBytes());
                channel.writeAndFlush(sendByteBuf);
            }
        }
    }

    
}
