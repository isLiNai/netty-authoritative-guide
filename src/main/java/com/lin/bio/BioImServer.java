package com.lin.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author lin
 * @version 1.0
 * @date 2021/6/30 12:48
 */
public class BioImServer {
    private static Map<String,Socket> session = new HashMap<>();

    public static void main(String[] args) {
        startUp(8080);

    }

    private static void startUp(int port){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true){
                // 1. 侦听要与此套接字建立的新连接, 该方法会阻塞(及阻塞主线程)，直至有新连接介入
                Socket socket = serverSocket.accept();
                System.out.println("有连接接入:"+socket.hashCode());
                session.put(socket.hashCode()+"",socket);
                new Thread(new ImRunnable(socket)).start();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  im 聊天 发送消息格式为 fromId@toId@message
     */
    static class ImRunnable implements Runnable{
        private Socket socket;

        public ImRunnable(Socket socket){
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
                    System.out.println("【"+ Thread.currentThread().getId() + "】" + " : "+s);

                    if(s.indexOf('@')!=-1){
                        String[] split = s.split("@");
                        String fromId = split[0];
                        String toId = split[1];
                        String message = split[2];
                        message = "【" + fromId + "】 和你说 :" +message + "\n  回应一下吧：";
                        Socket socket = session.get(toId);
                        socket.getOutputStream().write(message.getBytes("UTF-8"));
                    }

                }
            }catch (Exception e){
               e.printStackTrace();
            }finally {
                if(socket!=null){
                    try {
                        socket.close();
                        session.remove(socket.hashCode());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    socket = null;
                }
            }
        }
    }
}
