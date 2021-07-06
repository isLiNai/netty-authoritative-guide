package com.lin.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.util.List;

/**
 * 指定分割符分割
 *
 * @author lin
 * @version 1.0
 * @date 2021/7/1 11:31
 */
public class NettyDelimiterServer {

    public static void main(String[] args) {
        startUp(8080);
    }

    private static void startUp(int port){
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 指定分隔符
                        ByteBuf delimiter = Unpooled.copiedBuffer("@".getBytes());
                        // 解决粘包问题， 通过指定分隔符作为界限,  1024: 在规定长度下为读取到 分割符号，则报错
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new MessageInboundHandler());
                    }
                });

        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("服务启动成功");
            future.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            eventLoopGroup.shutdownGracefully();
        }
    }

    // String 编码器
    static class StringDecoder extends ByteToMessageDecoder{
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            out.add(new String(bytes));
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("报错了");
        }
    }

    static class MessageInboundHandler extends SimpleChannelInboundHandler<String> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println(msg);
        }
    }

}
