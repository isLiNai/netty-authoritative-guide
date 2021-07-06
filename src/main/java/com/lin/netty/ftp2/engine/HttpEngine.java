package com.lin.netty.ftp2.engine;

import com.lin.netty.ftp2.engine.constant.ReqUrlMappingConstant;
import com.lin.netty.ftp2.engine.impl.GetTreeImpl;
import com.lin.netty.ftp2.engine.impl.UploadFileImpl;
import com.lin.netty.http.HtmlUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lin
 */
public class HttpEngine {
    private static Map<String, MappingHandler> mappingMap = new HashMap<>();

    /**
     * 初始化 需要特殊处理的 url请求
     */
    static {
        mappingMap.put(ReqUrlMappingConstant.URL_GET_FILES,new GetTreeImpl());
        mappingMap.put(ReqUrlMappingConstant.URL_UPLOAD_FILE,new UploadFileImpl());
    }

    public static void execute(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        String url = URLDecoder.decode(fullHttpRequest.uri(), "UTF-8");
        MappingHandler mappingHandler = mappingMap.get(getUrl(url));
        if(mappingHandler !=null){
            mappingHandler.execute(ctx,fullHttpRequest,url);
        }

        // 无需特殊处理 则映射静态资源
        String filePath = mappingFilePath(getUrl(url));
        File file = new File(filePath);
        FullHttpResponse fullHttpResponse = null;
        if(!file.exists()){
            fullHttpResponse = processFullHttpResponse(HttpResponseStatus.valueOf(404),null);
        }else{
            byte[] contentBytes = HtmlUtil.readFile(file);
            fullHttpResponse = processFullHttpResponse(HttpResponseStatus.valueOf(200),contentBytes);
        }
        ctx.channel().writeAndFlush(fullHttpResponse);
    }

    private static String getUrl(String url) {
        int i = url.lastIndexOf("?");
        if(i>0){
            String t = url.substring(0,i);
            return t;
        }
        return url;
    }


    private static FullHttpResponse processFullHttpResponse(HttpResponseStatus status,  byte[] contentBytes) {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        fullHttpResponse.headers().add("Content-Type","text/html;charset=utf-8");
        fullHttpResponse.headers().add("Content-Language","zh-cn");
        if(contentBytes!=null){
            fullHttpResponse.headers().add("Content-Length",contentBytes.length);
            ByteBuf byteBuf = Unpooled.copiedBuffer(contentBytes);
            fullHttpResponse.content().writeBytes(byteBuf);
        }
        return fullHttpResponse;
    }

    /**
     * url 与 项目资源文件映射
     * @param url
     * @return
     */
    private static String mappingFilePath(String url) {
        url = ClassLoader.getSystemResource("").getPath() + url.substring(1);
        return url;
    }

}
