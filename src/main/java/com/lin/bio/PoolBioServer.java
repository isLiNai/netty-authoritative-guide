package com.lin.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * TODO
 *
 * @author lin
 * @version 1.0
 * @date 2021/6/30 14:00
 */
public class PoolBioServer {
    private static ExecutorService executorService = Executors.newFixedThreadPool(3);


    public static void main(String[] args) {
        startUp(8080);

    }

    private static void startUp(int port){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true){
                // 1. 侦听要与此套接字建立的新连接, 该方法会阻塞(及阻塞主线程)，直至有新连接介入
                Socket socket = serverSocket.accept();
                System.out.println("有连接接入");
                executorService.submit(new ServerDateRunnable(socket));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static class ServerDateRunnable implements Runnable{
        private Socket socket;

        public ServerDateRunnable(Socket socket){
            this.socket = socket;
        }
        @Override
        public void run() {
            try(
                    InputStream socketInputStream = socket.getInputStream();
                    OutputStream socketOutputStream = socket.getOutputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
            ){
                // 读取数据时 此方法会阻塞（底层阻塞），直到输入数据可用、检测到流结束或抛出异常为止。详情可看 InputStream#read 方法
                while (true){
                    String s = bufferedReader.readLine();
                    if("getServerDate".equals(s)){
                        System.out.println("【"+ Thread.currentThread().getId() + "】" + " : "+s);
                        s = "server resp date: " + new Date() + "\n";
                        socketOutputStream.write(s.getBytes("UTF-8"));
                    }
                }
            }catch (Exception e){
                try {
                    if(socket!=null){
                        socket.close();
                        socket = null;
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }


}
