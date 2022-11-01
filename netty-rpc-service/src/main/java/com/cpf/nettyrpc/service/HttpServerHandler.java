/**
 * @(#)HttpServerHandler.java, 9月 28, 2022.
 * <p>
 * Copyright 2022 . All rights reserved.
 *  PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * @author jiyingdabj
 */
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private RpcHandlerManager rpcHandlerManager;

    public HttpServerHandler(RpcHandlerManager rpcHandlerManager) {
        this.rpcHandlerManager = rpcHandlerManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        String uri = msg.uri();
        String[] paths = uri.split("/");
        RpcHandler handler = rpcHandlerManager.getHandler(paths[0]);
        Method method = rpcHandlerManager.getMethod(paths[1]);
        log.info("httpServerHandler rpcName = {}, method = {}, Uri = {}", paths[0], paths[1], uri);

        ByteBuf buf = msg.content();
        ByteBuf byteBuf;
        if (handler != null && method != null) {
            Object obj = RpcServiceProxy.invoke(handler, method, buf.toString(CharsetUtil.UTF_8));
            log.info("httpServerHandler method-invoke = {}", obj);
            byteBuf = Unpooled.copiedBuffer(obj.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            byteBuf = Unpooled.copiedBuffer(msg.content());
        }


        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        response.headers().add(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());

        ctx.writeAndFlush(response);
    }
}
