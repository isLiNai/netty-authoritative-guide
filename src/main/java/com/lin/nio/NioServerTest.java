package com.lin.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * TODO
 *
 * @author lin
 * @version 1.0
 * @date 2021/7/7 15:14
 */
public class NioServerTest {

    public static void main(String[] args) throws IOException {
        start(8080);
    }

    private static void start(int port) throws IOException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > 0){

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if(selectionKey.isAcceptable()){
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        System.out.println("accept");
                        socketChannel.register(selector,SelectionKey.OP_READ);
                    }
                    if (selectionKey.isReadable()){
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int len = 0;
                        while((len = channel.read(byteBuffer)) > 0){
                            byteBuffer.flip();
                            System.out.println(new java.lang.String(byteBuffer.array(),0,len));
                            byteBuffer.clear();
                        }
                    }
                }
                iterator.remove();
            }
        }
    }
}
