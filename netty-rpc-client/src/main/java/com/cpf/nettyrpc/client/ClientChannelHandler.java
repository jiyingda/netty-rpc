/**
 * @(#)ClientChannelHandler.java, 1月 16, 2024.
 * <p>
 * Copyright 2024 chapaof.com. All rights reserved.
 * chapaof.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client;

import com.cpf.nettyrpc.common.JsonUtils;
import com.cpf.nettyrpc.common.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiyingda
 */
@Slf4j
@ChannelHandler.Sharable
public class ClientChannelHandler extends SimpleChannelInboundHandler<RpcResponse> {

    public static final Map<String, Promise<Object>> requestFeatureMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {
        // RpcResponse response = JsonUtils.readValue(msg, new TypeReference<RpcResponse>() {});
        if (response != null) {
            String requestId = response.getRequestId();
            if (requestFeatureMap.containsKey(requestId)) {
                Promise<Object> p = requestFeatureMap.get(requestId);
                if (response.isSuccess()) {
                    p.setSuccess(response.getContent());
                } else {
                    p.setFailure(new Throwable(response.getException()));
                }
                log.info("channelRead0 {}", JsonUtils.writeValue(response));
            } else {
                log.warn("channelRead0-feature is closed {}", JsonUtils.writeValue(response));
            }
        }
    }
}