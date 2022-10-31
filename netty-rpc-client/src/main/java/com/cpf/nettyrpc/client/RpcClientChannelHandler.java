/**
 * @(#)ChannelHandler.java, 10月 12, 2022.
 * <p>
 * Copyright 2022 . All rights reserved.
 *  PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * @author jiyingdabj
 */
@Slf4j
public class RpcClientChannelHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private ChannelHandlerContext ctx;

    public boolean channelIsReady() {
        return ctx != null;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        log.info("ClientChannelHandler#channelActive");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse msg) throws Exception {
        msg.headers().get(HttpHeaderNames.CONTENT_TYPE);
        ByteBuf buf = msg.content();
        log.info("channelRead0 {}", buf.toString(io.netty.util.CharsetUtil.UTF_8));
    }

    public String sendMessage(String rpcMethodName, String args) throws URISyntaxException {
        URI uri = new URI("http://127.0.0.1:8082");
        String msg = args;
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes(StandardCharsets.UTF_8)));

        // 构建http请求
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        request.headers().set("rpcName", rpcMethodName);
        // 发送http请求
        ChannelFuture future = ctx.channel().writeAndFlush(request);
        return "";
    }
}
