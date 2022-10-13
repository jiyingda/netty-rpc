/**
 * @(#)RpcServiceAspect.java, 9月 30, 2022.
 * <p>
 * Copyright 2022 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client.aspect;

import com.cpf.nettyrpc.client.ClientChannelHandler;
import com.cpf.nettyrpc.client.RpcClientService;
import com.cpf.nettyrpc.client.annotation.RpcMethod;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * @author jiyingdabj
 */
@Slf4j
@Order(0)
@Aspect
@Component
public class RpcServiceAspect {

    @Around("@annotation(rpcMethod)")
    public Object proceed(ProceedingJoinPoint pjp, RpcMethod rpcMethod) throws Throwable {
        log.info("RpcServiceAspect#rpcMethod = {}", rpcMethod.name());
        Object result = pjp.proceed();
        sendMessage(rpcMethod.name());
        return result;
    }

    @Autowired
    private ClientChannelHandler clientChannelHandler;

    @Autowired
    private RpcClientService rpcClientService;

    private void sendMessage(String rpcMethodName) throws URISyntaxException {
        rpcClientService.init();
        URI uri = new URI("http://127.0.0.1:8082");
        String msg = "======> Are you ok? <======";
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                uri.toASCIIString(), Unpooled.wrappedBuffer(msg.getBytes(StandardCharsets.UTF_8)));

        // 构建http请求
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        request.headers().set("rpcName", rpcMethodName);
        // 发送http请求
        ChannelFuture future = clientChannelHandler.ctx.channel().writeAndFlush(request);
    }
}