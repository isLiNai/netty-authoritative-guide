package com.lin.netty.ftp2.engine.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.lin.netty.ftp2.engine.entity.ZNode;
import com.lin.netty.ftp2.engine.utils.AscUtil;
import com.lin.netty.ftp2.engine.utils.UrlUtil;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GetTreeImpl extends MappingAbs {

    static String host = null;


    static {
//        try {
            host = "http://"+ "192.168.30.204" +":8080/";
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void execute(ChannelHandlerContext ctx,FullHttpRequest fullHttpRequest,String url) {
        String parameter = UrlUtil.getUrlParameter(url, "id");
        String filePath = AscUtil.decrypt(parameter);
        File file = new File(filePath);
        if(file.isDirectory()){
            // 列出所有文件
            List<ZNode> zNodes = diskMappingTree(filePath);
            String json = JSON.toJSONString(zNodes);
            FullHttpResponse fullHttpResponse = processFullHttpResponse(HttpResponseStatus.valueOf(200), json.getBytes());
            ctx.channel().writeAndFlush(fullHttpResponse);
        }else{
            // 文件下载
            respFileSteam(ctx,file);
        }

    }

    /**
     * 列出所有文件
     * @param url
     * @return
     */
    private List<ZNode> diskMappingTree(String url){
        File[] files = FileUtil.ls(url);
        List<ZNode> ls = Arrays.stream(files).filter(v-> !v.isHidden()).sorted((f0,f1)->
            (int) (f0.length() - f1.length())
        ).map(v->{
            ZNode zNode = new ZNode();
            zNode.setName(v.getName());
            zNode.setOpen(false);
            String absolutePath = v.getAbsolutePath();
            zNode.setId( AscUtil.encrypt(absolutePath));
            String parent = v.getParent();
            zNode.setPid(AscUtil.encrypt(parent));
            if(v.isDirectory()){
                zNode.setIsParent(true);
            }else{
                zNode.setIsParent(false);
            }
            return zNode;
        }).collect(Collectors.toList());
        return ls;
    }

    /**
     * 响应http文件流, 下载
     *
     * https://zhuanlan.zhihu.com/p/152071762
     */
    private static void respFileSteam(ChannelHandlerContext ctx,File file) {
        System.out.println("文件下载");
        try {

            //随机读取文件
            final RandomAccessFile raf = new RandomAccessFile(file, "r");
            long fileLength = raf.length();
            //定义response对象
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            //设置请求头部
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream; charset=UTF-8");
            response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + URLEncoder.encode(file.getName(), "UTF-8") + "\";");
            ctx.write(response);
            //设置事件通知对象
            ChannelFuture sendFileFuture = ctx
                    .write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                //文件传输完成执行监听器
                @Override
                public void operationComplete(ChannelProgressiveFuture future)
                        throws Exception {
                    System.out.println("transfer complete.");
                }
                //文件传输进度监听器
                @Override
                public void operationProgressed(ChannelProgressiveFuture future,
                                                long progress, long total) throws Exception {
                    if (total < 0) {
                        System.out.println("Transfer progress: " + progress);
                    } else {
                        // 创建一个数值格式化对象
                        NumberFormat numberFormat = NumberFormat.getInstance();
                        // 设置精确到小数点后2位
                        numberFormat.setMaximumFractionDigits(2);
                        String result = numberFormat.format((float) progress / (float) total * 100);
                        System.out.println("Transfer progress: " + result + "%");
                    }
                }
            });
            //刷新缓冲区数据，文件结束标志符
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            FullHttpResponse fullHttpResponse = processFullHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
            ctx.channel().writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
        } catch (IOException e) {
            e.printStackTrace();
            FullHttpResponse fullHttpResponse = processFullHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, null);
            ctx.channel().writeAndFlush(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }





}
