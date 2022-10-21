/**
 * @(#)HttpServerHandler.java, 9月 28, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.RpcHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
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
public class HttpServerHandler extends SimpleChannelInboundHandler {

    private RpcHandlerManager rpcHandlerManager;

    public HttpServerHandler(RpcHandlerManager rpcHandlerManager) {
        this.rpcHandlerManager = rpcHandlerManager;
    }

    private RpcHandler handler;

    private Method method;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String s = request.headers().get("rpcName");

            String uri = request.uri();
            String[] paths = uri.split("/");
            handler = rpcHandlerManager.getHandler(paths[0]);
            method = rpcHandlerManager.getMethod(paths[1]);
            log.info("httpServerHandler rpcName = {}, Uri = {}", s, uri);
        }
        if (msg instanceof HttpContent) {

            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            ByteBuf byteBuf;
            if (handler != null && method != null) {
                Object obj = method.invoke(handler, buf.toString(CharsetUtil.UTF_8));
                log.info("httpServerHandler run = {}", obj);
                byteBuf = Unpooled.copiedBuffer(obj.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                byteBuf = Unpooled.copiedBuffer(content.content());
            }


            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
            response.headers().add(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().add(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());

            ctx.writeAndFlush(response);

        }
    }
}
