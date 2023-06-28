/**
 * @(#)RpcClientChannelHandler.java, 6月 27, 2023.
 * <p>
 * Copyright 2023 yuanfudao.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.client;

import com.cpf.nettyrpc.common.JsonUtils;
import com.cpf.nettyrpc.common.RpcResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author jiyingdabj
 */
@ChannelHandler.Sharable
@Slf4j
public class RpcClientChannelHandler extends SimpleChannelInboundHandler<String> {

    private ConcurrentHashMap<String, CountDownLatch> requestCountDownLatchMap;

    private ConcurrentHashMap<String, RpcResponse> requestFeatureMap;

    public RpcClientChannelHandler(ConcurrentHashMap<String, CountDownLatch> requestCountDownLatchMap, ConcurrentHashMap<String, RpcResponse> requestFeatureMap) {
        this.requestCountDownLatchMap = requestCountDownLatchMap;
        this.requestFeatureMap = requestFeatureMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        log.info("RpcClientChannelHandler {}", msg);
        RpcResponse response = JsonUtils.readValue(msg, new TypeReference<RpcResponse>() {});
        if (response != null) {
            String requestId = response.getRequestId();
            if (requestCountDownLatchMap.containsKey(requestId)) {
                requestFeatureMap.put(requestId, response);
                requestCountDownLatchMap.get(requestId).countDown();
                log.info("channelRead0 {}", msg);
            } else {
                log.warn("channelRead0-feature is closed {}", msg);
            }
        }
    }
}
