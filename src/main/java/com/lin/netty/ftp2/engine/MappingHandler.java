package com.lin.netty.ftp2.engine;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 *
 * url 映射引擎
 *
 * @author Lin
 */
public interface MappingHandler {

    /**
     * @return
     */
    void execute(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest,String url);
    
}
