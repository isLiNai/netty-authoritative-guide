package com.lin.netty.ftp2.engine.impl;

import com.lin.netty.ftp2.engine.MappingHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * @author Lin
 */
public abstract class MappingAbs implements MappingHandler {

    @Override
    public abstract void execute(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest,String url);

    static FullHttpResponse processFullHttpResponse(HttpResponseStatus status, byte[] contentBytes) {
        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.TEXT_HTML+";charset=utf-8");
        fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LANGUAGE,"zh-cn");
        if(contentBytes!=null){
            fullHttpResponse.headers().add(HttpHeaderNames.CONTENT_LENGTH,contentBytes.length);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(contentBytes);
            fullHttpResponse.content().writeBytes(byteBuf);
        }
        return fullHttpResponse;
    }
}
