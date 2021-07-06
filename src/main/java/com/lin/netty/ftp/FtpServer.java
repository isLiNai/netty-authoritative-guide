package com.lin.netty.ftp;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import com.lin.netty.ftp.model.FtpMessage;
import com.lin.netty.ftp2.engine.constant.DeskMappingConstant;
import com.lin.netty.http.HtmlUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ftp 文件服务示例
 *
 * @author lin
 * @version 1.0
 * @date 2021/7/2 9:32
 */
public class FtpServer {

    private static String ROOT_DIRECTORY_PATH = "D:";

    static final String CONTEXT_TYPE = "Context-type";

    static final String CONTENT_LENGTH = "Content-Length";

    static final String HTML_TYPE = "text/html;charset=UTF-8";

    static HttpVersion VERSION = HttpVersion.HTTP_1_1;

    static HttpResponseStatus SUCCESS_STATUS = HttpResponseStatus.valueOf(200);

    static String host = null;

    static {
        try {
            host = "http://"+InetAddress.getLocalHost().getHostAddress()+":8080/";
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
                        ch.pipeline().addLast(new HttpObjectAggregator(1058));
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

    static class HttpHandler extends SimpleChannelInboundHandler{
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof FullHttpRequest){
                FullHttpRequest req = (FullHttpRequest)msg;
                // check url
                String url = req.uri();
                if(!url.contains("/favicon.ico")){
                    String filePath = mappingFilePath(url);
                    // 如果url请求的是目录，返回目录结构
                    File file = new File(filePath);

                    if(!file.isDirectory()){
                        FullHttpResponse response = respFileSteam(file);
                        ctx.channel().writeAndFlush(response);
                    }else{
                        FullHttpResponse response = respFiles(file);
                        ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    }
                }
            }
        }
    }

    /**
     * url 与 文件系统映射
     * @param url
     * @return
     */
    private static String mappingFilePath(String url) {
        // 如果不是根目录 则需要 url base64 解码
        if (!"/".equals(url)){
            url = "/" + Base64.decodeStr(url.substring(1));
        }
        url = ROOT_DIRECTORY_PATH + url;
        return url;
    }

    /**
     * 响应http文件流, 下载
     */
    private static FullHttpResponse respFileSteam(File file) {
        System.out.println("文件下载");
        FileInputStream fileInputStream = null;
        try {
            FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(VERSION, SUCCESS_STATUS);
            fullHttpResponse.headers().add(CONTEXT_TYPE, "application/octet-stream");
            // 解决中文乱码问题
            fullHttpResponse.headers().add("Content-Disposition", "attachment;filename*=utf-8'zh_cn'" + URLEncoder.encode(file.getName(), "UTF-8"));
            fullHttpResponse.headers().add(CONTENT_LENGTH, file.length());
            fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int i = -1;
            while ((i = fileInputStream.read(bytes)) != -1){
                fullHttpResponse.content().writeBytes(bytes);
            }
            return fullHttpResponse;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 响应文件列表
     * @param file
     * @return
     */
    private static FullHttpResponse respFiles(File file) {
        List<FtpMessage> ftpMessages = readFtpFiles(file);
        // template html
        String html = htmlGenerate(ftpMessages);
        ByteBuf content = Unpooled.copiedBuffer(html.getBytes());
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(VERSION, SUCCESS_STATUS, content);
        fullHttpResponse.headers().add(CONTEXT_TYPE, HTML_TYPE);
        return fullHttpResponse;
    }

    public static String htmlGenerate(List<FtpMessage> ftpMessages) {
        Context context = new Context();
        context.setVariable("ftpMessages",ftpMessages);
        String htmlStr = HtmlUtil.readHtmlStr("templates/ftpMessage.html");
        TemplateEngine engine = new TemplateEngine();
        String htmlGenerate = engine.process(htmlStr, context);
        return htmlGenerate;
    }

    private static List<FtpMessage> readFtpFiles(File file){
        // 获取文件夹下文件列表
        File[] files = FileUtil.ls(file.getAbsolutePath());
        List<FtpMessage> ftpMessages = Arrays.stream(files).map(v->{
            FtpMessage ftpMessage = new FtpMessage();
            ftpMessage.setName(v.getName());
            String absolutePath = v.getAbsolutePath();
            String replace = absolutePath.replace(ROOT_DIRECTORY_PATH + "\\", "");
            String encode = Base64.encode(replace);
            ftpMessage.setAbsoluteUrl(host + encode);
            return ftpMessage;
        }).collect(Collectors.toList());
        return ftpMessages;
    }



}
