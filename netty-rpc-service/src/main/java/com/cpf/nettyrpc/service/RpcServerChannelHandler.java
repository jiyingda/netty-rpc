package com.cpf.nettyrpc.service;

import com.cpf.nettyrpc.common.JsonUtils;
import com.cpf.nettyrpc.common.RpcHandler;
import com.cpf.nettyrpc.common.RpcRequest;
import com.cpf.nettyrpc.common.RpcResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * @author jiyingdabj
 */
@ChannelHandler.Sharable
@Slf4j
public class RpcServerChannelHandler extends SimpleChannelInboundHandler<String> {

    @Autowired
    private RpcHandlerManager rpcHandlerManager;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        RpcRequest rpcRequest = JsonUtils.readValue(msg, new TypeReference<RpcRequest>() {});
        if (rpcRequest == null) {
            return;
        }
        RpcHandler handler = rpcHandlerManager.getHandler(rpcRequest.getHandler());
        Method method = rpcHandlerManager.getMethod(rpcRequest.getHandler(), rpcRequest.getMethod());
        log.info("httpServerHandler rpcName = {}, method = {}", rpcRequest.getHandler(), rpcRequest.getMethod());
        if (handler != null && method != null) {
            RpcResponse response = RpcHandlerProxy.invoke(handler, method, rpcRequest);
            log.info("httpServerHandler method-invoke = {}", response);
            ctx.writeAndFlush(JsonUtils.writeValue(response));
        } else {
            RpcResponse response = new RpcResponse();
            response.setRequestId(rpcRequest.getRequestId());
            response.setSuccess(false);
            response.setException("method not found");
            ctx.writeAndFlush(response);
        }

    }
}
